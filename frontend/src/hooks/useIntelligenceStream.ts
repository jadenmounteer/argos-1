/**
 * Placeholder hook for streaming ARGOS-1 thought/response chunks from the backend.
 * Task 3 will wire this to POST /api/v1/command over SSE.
 */
export type StreamChunkType = 'thought' | 'response'

export type StreamChunk = {
  id: string
  type: StreamChunkType
  text: string
}

export function useIntelligenceStream() {
  // For Task 1 this is stubbed; later this will expose a function that
  // initiates the JSON-RPC / SSE call and streams updates.
  const chunks: StreamChunk[] = []

  return {
    chunks,
    sendCommand: (text: string) => {
      void text
      // no-op for now
    },
    isStreaming: false,
  }
}

