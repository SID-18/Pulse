package com.pulse.ai.exception;

public class AiServiceUnavailableException extends RuntimeException {

    public AiServiceUnavailableException(Throwable cause) {
        super("Local AI service is unavailable. Start Ollama and try again.", cause);
    }
}
