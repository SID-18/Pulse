package com.pulse.ai.exception;

public class RagServiceUnavailableException extends RuntimeException {

    public RagServiceUnavailableException(Throwable cause) {
        super("Local RAG service is unavailable. Start the AI service, ChromaDB, and Ollama, then try again.", cause);
    }
}
