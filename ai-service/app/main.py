import os
from typing import Any

import chromadb
import httpx
from fastapi import FastAPI, HTTPException
from pydantic import AliasChoices, BaseModel, Field


app = FastAPI(title="Pulse AI Service", version="1.0.0")

CHROMA_HOST = os.getenv("PULSE_CHROMA_HOST", "localhost")
CHROMA_PORT = int(os.getenv("PULSE_CHROMA_PORT", "8000"))
OLLAMA_BASE_URL = os.getenv("PULSE_OLLAMA_BASE_URL", "http://localhost:11434").rstrip("/")
OLLAMA_MODEL = os.getenv("PULSE_OLLAMA_MODEL", "qwen2.5:1.5b")
EMBEDDING_MODEL = os.getenv("PULSE_OLLAMA_EMBEDDING_MODEL", "embeddinggemma")
COLLECTION_NAME = "resolved_incidents"


class IncidentDocument(BaseModel):
    id: str
    title: str
    description: str | None = None
    severity: str
    status: str
    service_name: str | None = Field(
        default=None,
        validation_alias=AliasChoices("service_name", "serviceName"),
    )
    created_at: Any | None = Field(
        default=None,
        validation_alias=AliasChoices("created_at", "createdAt"),
    )
    resolved_at: Any | None = Field(
        default=None,
        validation_alias=AliasChoices("resolved_at", "resolvedAt"),
    )


class IndexRequest(BaseModel):
    incidents: list[IncidentDocument]


class SimilarIncident(BaseModel):
    id: str
    title: str
    severity: str
    status: str
    service_name: str | None = None
    similarity: float


class AnalysisResponse(BaseModel):
    model: str
    recommendation: str
    similar_incidents: list[SimilarIncident] = Field(default_factory=list)


def collection():
    client = chromadb.HttpClient(host=CHROMA_HOST, port=CHROMA_PORT)
    return client.get_or_create_collection(
        name=COLLECTION_NAME,
        metadata={"hnsw:space": "cosine"},
    )


def document_text(incident: IncidentDocument) -> str:
    return "\n".join(
        [
            f"Title: {incident.title}",
            f"Description: {incident.description or 'No description provided.'}",
            f"Severity: {incident.severity}",
            f"Status: {incident.status}",
            f"Service: {incident.service_name or 'Unassigned'}",
        ]
    )


def metadata_for(incident: IncidentDocument) -> dict[str, str]:
    metadata = {
        "title": incident.title,
        "severity": incident.severity,
        "status": incident.status,
    }
    if incident.service_name:
        metadata["service_name"] = incident.service_name
    return metadata


def factual_summary(incident: IncidentDocument) -> str:
    service = incident.service_name or "No service is assigned"
    description = incident.description or "No description was provided"
    return (
        f"{incident.title} is currently {incident.status} with {incident.severity} severity. "
        f"Service: {service}. Description: {description}."
    )


def relevant_history(similar: list[SimilarIncident]) -> str:
    if not similar:
        return "No comparable historical resolved incident was retrieved."
    return "\n".join(
        f"- {item.title} ({item.severity}, service: {item.service_name or 'Unassigned'}, "
        f"similarity: {item.similarity})"
        for item in similar
    )


async def embed(inputs: list[str]) -> list[list[float]]:
    try:
        async with httpx.AsyncClient(timeout=60.0) as client:
            response = await client.post(
                f"{OLLAMA_BASE_URL}/api/embed",
                json={"model": EMBEDDING_MODEL, "input": inputs},
            )
            response.raise_for_status()
            embeddings = response.json().get("embeddings")
            if not embeddings:
                raise ValueError("Ollama returned no embeddings.")
            return embeddings
    except (httpx.HTTPError, ValueError) as error:
        raise HTTPException(
            status_code=503,
            detail="Local embedding service is unavailable. Start Ollama and pull the embedding model.",
        ) from error


async def generate(prompt: str) -> str:
    try:
        async with httpx.AsyncClient(timeout=120.0) as client:
            response = await client.post(
                f"{OLLAMA_BASE_URL}/api/generate",
                json={
                    "model": OLLAMA_MODEL,
                    "prompt": prompt,
                    "stream": False,
                    "keep_alive": "0",
                    "options": {"num_predict": 160, "temperature": 0.2},
                },
            )
            response.raise_for_status()
            answer = response.json().get("response")
            if not answer:
                raise ValueError("Ollama returned no recommendation.")
            return answer.strip()
    except (httpx.HTTPError, ValueError) as error:
        raise HTTPException(
            status_code=503,
            detail="Local generation service is unavailable. Start Ollama and pull the configured model.",
        ) from error


@app.get("/health")
def health() -> dict[str, str]:
    try:
        collection()
        return {"status": "UP"}
    except Exception as error:
        raise HTTPException(status_code=503, detail="ChromaDB is unavailable.") from error


@app.post("/api/v1/incidents/index")
async def index_incidents(request: IndexRequest) -> dict[str, int]:
    if not request.incidents:
        return {"indexed_count": 0}

    incidents = request.incidents
    embeddings = await embed([document_text(incident) for incident in incidents])
    try:
        collection().upsert(
            ids=[incident.id for incident in incidents],
            documents=[document_text(incident) for incident in incidents],
            embeddings=embeddings,
            metadatas=[metadata_for(incident) for incident in incidents],
        )
        return {"indexed_count": len(incidents)}
    except Exception as error:
        raise HTTPException(status_code=503, detail="ChromaDB is unavailable.") from error


@app.post("/api/v1/incidents/analyze", response_model=AnalysisResponse)
async def analyze_incident(incident: IncidentDocument) -> AnalysisResponse:
    query_embedding = (await embed([document_text(incident)]))[0]
    try:
        matches: dict[str, Any] = collection().query(
            query_embeddings=[query_embedding],
            n_results=3,
            include=["metadatas", "distances"],
        )
    except Exception as error:
        raise HTTPException(status_code=503, detail="ChromaDB is unavailable.") from error

    similar = []
    ids = matches.get("ids", [[]])[0]
    metadatas = matches.get("metadatas", [[]])[0]
    distances = matches.get("distances", [[]])[0]
    for incident_id, metadata, distance in zip(ids, metadatas, distances):
        if incident_id == incident.id:
            continue
        similar.append(
            SimilarIncident(
                id=incident_id,
                title=metadata["title"],
                severity=metadata["severity"],
                status=metadata["status"],
                service_name=metadata.get("service_name"),
                similarity=round(max(0.0, 1.0 - float(distance)), 3),
            )
        )

    summary = factual_summary(incident)
    history = relevant_history(similar)
    prompt = f"""
Return only a numbered list of at most three practical, conditional next steps.
Do not add headings, a summary, history, a root cause, a prior lifecycle status, or a claimed resolution outcome.
Do not state unverified facts. Use only the supplied incident facts and retrieved history.

Current incident:
{summary}

Retrieved resolved incidents:
{history}
""".strip()

    next_steps = await generate(prompt)
    recommendation = "\n\n".join(
        [
            f"Summary\n{summary}",
            f"Relevant history\n{history}",
            f"Recommended next steps\n{next_steps}",
        ]
    )

    return AnalysisResponse(
        model=OLLAMA_MODEL,
        recommendation=recommendation,
        similar_incidents=similar,
    )
