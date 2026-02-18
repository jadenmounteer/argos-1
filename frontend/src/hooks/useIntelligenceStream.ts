/**
 * Hook for streaming ARGOS-1 thought/response chunks from the backend.
 * Task 3 Phase 1+2: POST to /api/v1/command, consume SSE stream, parse event/data and route to thoughtLog/mainResponse.
 */
import { useCallback, useRef, useState } from "react";
import { playCommandSubmitted, playStreamError } from "../audio/audioBridge";

export type StreamChunkType = "thought" | "response";

export type StreamChunk = {
  id: string;
  type: StreamChunkType;
  text: string;
};

export type UseIntelligenceStreamOptions = {
  onResponseSentence?: (sentence: string) => void;
  /** Called once when the stream ends with the full accumulated response text (never thought content). */
  onResponseFinalized?: (text: string) => void;
};

const SENTENCE_REGEX = /[.!?](\s+|$)/g;

function getKernelBaseUrl(): string {
  if (typeof import.meta === "undefined" || !import.meta.env?.VITE_KERNEL_URL) {
    return "";
  }
  return String(import.meta.env.VITE_KERNEL_URL).trim();
}

function flushSentences(
  newText: string,
  sentenceBufferRef: React.MutableRefObject<string>,
  onResponseSentence: ((sentence: string) => void) | undefined,
): void {
  if (!onResponseSentence) return;
  const buf = sentenceBufferRef.current + newText;
  SENTENCE_REGEX.lastIndex = 0;
  let lastEnd = 0;
  let match: RegExpExecArray | null;
  while ((match = SENTENCE_REGEX.exec(buf)) !== null) {
    const sentence = buf.slice(lastEnd, match.index + match[0].length).trim();
    if (sentence) onResponseSentence(sentence);
    lastEnd = match.index + match[0].length;
  }
  sentenceBufferRef.current = buf.slice(lastEnd);
}

export function useIntelligenceStream(options: UseIntelligenceStreamOptions = {}) {
  const { onResponseSentence, onResponseFinalized } = options;
  const [isStreaming, setIsStreaming] = useState(false);
  const [streamError, setStreamError] = useState<string | null>(null);
  const [thoughtLog, setThoughtLog] = useState("");
  const [mainResponse, setMainResponse] = useState("");
  const sentenceBufferRef = useRef("");
  const responseAccumulatorRef = useRef("");
  const chunks: StreamChunk[] = [];

  const sendCommand = useCallback(
    async (text: string) => {
      const baseUrl = getKernelBaseUrl();
      if (!baseUrl) {
        setStreamError("VITE_KERNEL_URL is not set");
        playStreamError();
        return;
      }
      setStreamError(null);
      setIsStreaming(true);
      setThoughtLog("");
      setMainResponse("");
      sentenceBufferRef.current = "";
      responseAccumulatorRef.current = "";
      playCommandSubmitted();

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
          setStreamError(
            `Request failed: ${response.status} ${response.statusText}`,
          );
          playStreamError();
          setIsStreaming(false);
          return;
        }

        const reader = response.body?.getReader();
        if (!reader) {
          setStreamError("Response has no body");
          playStreamError();
          setIsStreaming(false);
          return;
        }

        const decoder = new TextDecoder("utf-8");
        let lineBuffer = "";
        let lastEventType: string | null = null;

        const processLine = (line: string): void => {
          const trimmed = line.trimEnd();
          if (trimmed.startsWith("event:")) {
            lastEventType = trimmed.replace(/^event:\s*/, "").trim();
            return;
          }
          if (trimmed.startsWith("data:")) {
            try {
              const raw = trimmed.replace(/^data:\s*/, "").trim();
              const payload = JSON.parse(raw) as {
                params?: { text?: string };
              };
              const segment = payload.params?.text ?? "";
              if (lastEventType === "thought") {
                setThoughtLog((prev) => prev + segment);
              } else if (lastEventType === "response") {
                responseAccumulatorRef.current += segment;
                setMainResponse((prev) => prev + segment);
                flushSentences(segment, sentenceBufferRef, onResponseSentence);
              }
            } catch {
              // ignore malformed data line
            }
            lastEventType = null;
          }
        };

        try {
          let done = false;
          while (!done) {
            const { value, done: chunkDone } = await reader.read();
            done = chunkDone;
            if (value) {
              lineBuffer += decoder.decode(value, { stream: true });
              const lines = lineBuffer.split("\n");
              const complete = lineBuffer.endsWith("\n")
                ? lines
                : lines.slice(0, -1);
              lineBuffer = lineBuffer.endsWith("\n") ? "" : (lines.at(-1) ?? "");
              for (const line of complete) {
                processLine(line);
              }
            }
          }
          // flush remaining sentence buffer on stream close
          if (sentenceBufferRef.current.trim()) {
            onResponseSentence?.(sentenceBufferRef.current.trim());
          }
          sentenceBufferRef.current = "";
          const finalized = responseAccumulatorRef.current.trim();
          if (finalized) {
            onResponseFinalized?.(finalized);
          }
        } finally {
          reader.releaseLock();
        }
      } catch (err) {
        const message = err instanceof Error ? err.message : String(err);
        setStreamError(message);
        playStreamError();
      } finally {
        setIsStreaming(false);
      }
    },
    [onResponseSentence, onResponseFinalized],
  );

  return {
    chunks,
    sendCommand,
    isStreaming,
    streamError,
    thoughtLog,
    mainResponse,
  };
}
