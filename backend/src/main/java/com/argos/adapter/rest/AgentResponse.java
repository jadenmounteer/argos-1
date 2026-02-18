package com.argos.adapter.rest;

/**
 * Structured response DTO for backend-to-frontend communication.
 * Supports future streaming: thought, action, response (Task 5).
 */
public record AgentResponse(String thought, String action, String response) {
}
