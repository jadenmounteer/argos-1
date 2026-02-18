/**
 * Hook for streaming ARGOS-1 thought/response chunks from the backend.
 * Task 3 Phase 1: POST to /api/v1/command and consume response as ReadableStream.
 */
import { useCallback, useState } from "react";

export type StreamChunkType = "thought" | "response";

export type StreamChunk = {
  id: string;
  type: StreamChunkType;
  text: string;
};

function getKernelBaseUrl(): string {
  if (typeof import.meta === "undefined" || !import.meta.env?.VITE_KERNEL_URL) {
    return "";
  }
  return String(import.meta.env.VITE_KERNEL_URL).trim();
}

export function useIntelligenceStream() {
  const [isStreaming, setIsStreaming] = useState(false);
  const [streamError, setStreamError] = useState<string | null>(null);
  const chunks: StreamChunk[] = [];

  const sendCommand = useCallback(async (text: string) => {
    const baseUrl = getKernelBaseUrl();
    if (!baseUrl) {
      setStreamError("VITE_KERNEL_URL is not set");
      return;
    }
    setStreamError(null);
    setIsStreaming(true);

    const url = `${baseUrl}/api/v1/command`;
    const body = JSON.stringify({
      jsonrpc: "2.0",
      method: "process_voice",
      params: { input: text },
      id: crypto.randomUUID(),
    });

    try {
      const response = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body,
      });

      if (!response.ok) {
        setStreamError(`Request failed: ${response.status} ${response.statusText}`);
        setIsStreaming(false);
        return;
      }

      const reader = response.body?.getReader();
      if (!reader) {
        setStreamError("Response has no body");
        setIsStreaming(false);
        return;
      }

      const decoder = new TextDecoder("utf-8");
      try {
        let done = false;
        while (!done) {
          const { value, done: chunkDone } = await reader.read();
          done = chunkDone;
          if (value) {
            decoder.decode(value, { stream: true });
          }
        }
      } finally {
        reader.releaseLock();
      }
    } catch (err) {
      const message = err instanceof Error ? err.message : String(err);
      setStreamError(message);
    } finally {
      setIsStreaming(false);
    }
  }, []);

  return {
    chunks,
    sendCommand,
    isStreaming,
    streamError,
  };
}
