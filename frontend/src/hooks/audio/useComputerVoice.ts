/**
 * Computer voice / TTS (Task 7). Uses ElevenLabs API with Aria or Charlotte voice.
 * No-op in SSR or when VITE_ELEVENLABS_API_KEY is not set.
 */
import { useCallback, useEffect, useRef, useState } from "react";

const ELEVENLABS_VOICES_URL = "https://api.elevenlabs.io/v1/voices";
const PREFERRED_VOICE_NAMES = ["Adela", "Sarah", "Alice"];
const DEFAULT_MODEL_ID = "eleven_multilingual_v2";
const OUTPUT_FORMAT = "mp3_44100_128";
/** Streaming: low-latency model and PCM for Web Audio. */
const STREAM_MODEL_ID = "eleven_flash_v2_5";
const STREAM_OUTPUT_FORMAT = "pcm_44100";
const STREAM_SAMPLE_RATE = 44100;
/** Playback volume 0–1. TTS can be loud; default 0.5. */
const TTS_VOLUME = 0.5;

function getElevenLabsApiKey(): string {
  if (
    typeof import.meta === "undefined" ||
    !import.meta.env?.VITE_ELEVENLABS_API_KEY
  ) {
    return "";
  }
  return String(import.meta.env.VITE_ELEVENLABS_API_KEY).trim();
}

type VoicesResponse = {
  voices?: Array<{ voice_id: string; name: string }>;
};

async function resolveVoiceId(apiKey: string): Promise<string | null> {
  const res = await fetch(ELEVENLABS_VOICES_URL, {
    headers: { "xi-api-key": apiKey },
  });
  if (!res.ok) return null;
  const data = (await res.json()) as VoicesResponse;
  const voices = data.voices ?? [];
  for (const name of PREFERRED_VOICE_NAMES) {
    const v = voices.find((voice) =>
      voice.name.toLowerCase().includes(name.toLowerCase()),
    );
    if (v) return v.voice_id;
  }
  return voices[0]?.voice_id ?? null;
}

type StreamAudioMessage = {
  audio?: string;
  isFinal?: boolean;
};

function base64ToArrayBuffer(base64: string): ArrayBuffer {
  const binary = atob(base64);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i);
  return bytes.buffer;
}

/** Decode 16-bit PCM to Float32 and schedule playback. Returns end time in context time. */
function schedulePcmChunk(
  ctx: AudioContext,
  gainNode: GainNode,
  base64Audio: string,
  nextStartTime: number,
): number {
  const ab = base64ToArrayBuffer(base64Audio);
  const int16 = new Int16Array(ab);
  const float32 = new Float32Array(int16.length);
  for (let i = 0; i < int16.length; i++) float32[i] = int16[i] / 32768;
  const buffer = ctx.createBuffer(1, float32.length, STREAM_SAMPLE_RATE);
  buffer.copyToChannel(float32, 0);
  const source = ctx.createBufferSource();
  source.buffer = buffer;
  source.connect(gainNode);
  source.start(nextStartTime);
  return nextStartTime + buffer.duration;
}

let hasWarnedNoApiKey = false;

export function useComputerVoice() {
  const [isSpeaking, setIsSpeaking] = useState(false);
  const voiceIdPromiseRef = useRef<Promise<string | null> | null>(null);
  const currentAudioRef = useRef<HTMLAudioElement | null>(null);
  const currentUrlRef = useRef<string | null>(null);
  const abortControllerRef = useRef<AbortController | null>(null);
  const wsRef = useRef<WebSocket | null>(null);
  const streamOpenPromiseRef = useRef<Promise<void> | null>(null);
  const audioContextRef = useRef<AudioContext | null>(null);
  const gainNodeRef = useRef<GainNode | null>(null);
  const nextStartTimeRef = useRef(0);
  const streamEndTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const sentenceQueueRef = useRef<string[]>([]);
  const isPlayingFromQueueRef = useRef(false);

  useEffect(() => {
    if (typeof window === "undefined") return;
    const apiKey = getElevenLabsApiKey();
    if (apiKey && !voiceIdPromiseRef.current) {
      voiceIdPromiseRef.current = resolveVoiceId(apiKey);
    }
  }, []);

  const cancel = useCallback(() => {
    if (streamEndTimeoutRef.current) {
      clearTimeout(streamEndTimeoutRef.current);
      streamEndTimeoutRef.current = null;
    }
    streamOpenPromiseRef.current = null;
    if (wsRef.current) {
      try {
        wsRef.current.close();
      } catch {
        // ignore
      }
      wsRef.current = null;
    }
    if (currentAudioRef.current) {
      currentAudioRef.current.pause();
      currentAudioRef.current = null;
    }
    if (currentUrlRef.current) {
      URL.revokeObjectURL(currentUrlRef.current);
      currentUrlRef.current = null;
    }
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
      abortControllerRef.current = null;
    }
    sentenceQueueRef.current = [];
    setIsSpeaking(false);
  }, []);

  const speak = useCallback(
    async (text: string) => {
      if (typeof window === "undefined") return;
      const apiKey = getElevenLabsApiKey();
      if (!apiKey) {
        if (!hasWarnedNoApiKey) {
          hasWarnedNoApiKey = true;
          console.warn(
            "[useComputerVoice] TTS skipped: VITE_ELEVENLABS_API_KEY is not set. " +
              "Add it to frontend/.env (see .env-example) and restart the dev server.",
          );
        }
        return;
      }
      if (!text.trim()) return;

      cancel();

      if (!voiceIdPromiseRef.current) {
        voiceIdPromiseRef.current = resolveVoiceId(apiKey);
      }
      const voiceId = await voiceIdPromiseRef.current;
      if (!voiceId) return;

      const controller = new AbortController();
      abortControllerRef.current = controller;

      const url = `https://api.elevenlabs.io/v1/text-to-speech/${voiceId}?output_format=${OUTPUT_FORMAT}`;
      try {
        const res = await fetch(url, {
          method: "POST",
          signal: controller.signal,
          headers: {
            "xi-api-key": apiKey,
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            text: text.trim(),
            model_id: DEFAULT_MODEL_ID,
            voice_settings: { stability: 0.6, similarity_boost: 0.8 },
          }),
        });

        if (controller.signal.aborted || !res.ok) {
          if (!res.ok && res.status === 401) {
            console.error(
              "[useComputerVoice] 401 Unauthorized: check that VITE_ELEVENLABS_API_KEY in .env is correct and has not expired.",
            );
          }
          setIsSpeaking(false);
          return;
        }

        const buffer = await res.arrayBuffer();
        if (controller.signal.aborted) {
          setIsSpeaking(false);
          return;
        }

        const blob = new Blob([buffer], { type: "audio/mpeg" });
        const objectUrl = URL.createObjectURL(blob);
        currentUrlRef.current = objectUrl;

        const audio = new Audio(objectUrl);
        audio.volume = TTS_VOLUME;
        currentAudioRef.current = audio;

        const cleanup = () => {
          if (currentUrlRef.current) {
            URL.revokeObjectURL(currentUrlRef.current);
            currentUrlRef.current = null;
          }
          currentAudioRef.current = null;
          abortControllerRef.current = null;
          setIsSpeaking(false);
        };

        audio.onended = cleanup;
        audio.onerror = cleanup;

        setIsSpeaking(true);
        await audio.play();
      } catch (err) {
        if ((err as Error).name !== "AbortError") {
          console.error("[useComputerVoice] ElevenLabs TTS failed:", err);
        }
        setIsSpeaking(false);
        abortControllerRef.current = null;
      }
    },
    [cancel],
  );

  const playOneSentence = useCallback(
    async (apiKey: string, voiceId: string, text: string): Promise<void> => {
      const controller = new AbortController();
      abortControllerRef.current = controller;
      try {
        const url = `https://api.elevenlabs.io/v1/text-to-speech/${voiceId}?output_format=${OUTPUT_FORMAT}`;
        const res = await fetch(url, {
          method: "POST",
          signal: controller.signal,
          headers: {
            "xi-api-key": apiKey,
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            text: text.trim(),
            model_id: DEFAULT_MODEL_ID,
            voice_settings: { stability: 0.6, similarity_boost: 0.8 },
          }),
        });
        if (controller.signal.aborted || !res.ok) return;
        const buffer = await res.arrayBuffer();
        if (controller.signal.aborted) return;
        const blob = new Blob([buffer], { type: "audio/mpeg" });
        const objectUrl = URL.createObjectURL(blob);
        currentUrlRef.current = objectUrl;
        const audio = new Audio(objectUrl);
        audio.volume = TTS_VOLUME;
        currentAudioRef.current = audio;
        await new Promise<void>((resolve) => {
          const done = () => {
            if (currentUrlRef.current) {
              URL.revokeObjectURL(currentUrlRef.current);
              currentUrlRef.current = null;
            }
            currentAudioRef.current = null;
            abortControllerRef.current = null;
            resolve();
          };
          audio.onended = done;
          audio.onerror = done;
          audio.play().catch(done);
        });
      } catch (err) {
        if ((err as Error).name !== "AbortError") {
          console.error("[useComputerVoice] ElevenLabs TTS failed:", err);
        }
        abortControllerRef.current = null;
      }
    },
    [],
  );

  const playNextFromQueue = useCallback(() => {
    if (sentenceQueueRef.current.length === 0) {
      isPlayingFromQueueRef.current = false;
      setIsSpeaking(false);
      return;
    }
    const text = sentenceQueueRef.current.shift()!.trim();
    if (!text) {
      playNextFromQueue();
      return;
    }
    setIsSpeaking(true);
    void (async () => {
      const apiKey = getElevenLabsApiKey();
      if (!apiKey) {
        isPlayingFromQueueRef.current = false;
        setIsSpeaking(false);
        return;
      }
      if (!voiceIdPromiseRef.current) {
        voiceIdPromiseRef.current = resolveVoiceId(apiKey);
      }
      const voiceId = await voiceIdPromiseRef.current;
      if (!voiceId) {
        isPlayingFromQueueRef.current = false;
        setIsSpeaking(false);
        return;
      }
      await playOneSentence(apiKey, voiceId, text);
      playNextFromQueue();
    })();
  }, [playOneSentence]);

  const speakSentence = useCallback(
    (sentence: string) => {
      if (typeof window === "undefined") return;
      const apiKey = getElevenLabsApiKey();
      if (!apiKey) return;
      const t = sentence.trim();
      if (!t) return;
      sentenceQueueRef.current.push(t);
      if (!isPlayingFromQueueRef.current) {
        isPlayingFromQueueRef.current = true;
        playNextFromQueue();
      }
    },
    [playNextFromQueue],
  );

  const speakChunk = useCallback(
    (segment: string) => {
      if (typeof window === "undefined") return;
      const apiKey = getElevenLabsApiKey();
      if (!apiKey) return;
      const text = segment.trim();
      if (!text) return;

      const ensureStreamOpen = (): Promise<void> => {
        if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
          return Promise.resolve();
        }
        if (streamOpenPromiseRef.current) {
          return streamOpenPromiseRef.current;
        }
        const openPromise = (async () => {
          if (!voiceIdPromiseRef.current) {
            voiceIdPromiseRef.current = resolveVoiceId(apiKey);
          }
          const voiceId = await voiceIdPromiseRef.current;
          if (!voiceId) return;
          if (wsRef.current?.readyState === WebSocket.OPEN) return;

          const ctx = new AudioContext();
          const gainNode = ctx.createGain();
          gainNode.gain.value = TTS_VOLUME;
          gainNode.connect(ctx.destination);
          audioContextRef.current = ctx;
          gainNodeRef.current = gainNode;
          nextStartTimeRef.current = ctx.currentTime;

          const wsUrl = `wss://api.elevenlabs.io/v1/text-to-speech/${voiceId}/stream-input?model_id=${STREAM_MODEL_ID}&output_format=${STREAM_OUTPUT_FORMAT}`;
          const ws = new WebSocket(wsUrl);
          wsRef.current = ws;

          await new Promise<void>((resolve, reject) => {
            ws.onopen = () => {
              ws.send(
                JSON.stringify({
                  text: " ",
                  xi_api_key: apiKey,
                  voice_settings: { stability: 0.6, similarity_boost: 0.8 },
                }),
              );
              setIsSpeaking(true);
              resolve();
            };
            ws.onerror = () => reject(new Error("WebSocket error"));
          });

          ws.onmessage = async (event) => {
            const ctx = audioContextRef.current;
            const gainNode = gainNodeRef.current;
            if (!ctx || !gainNode) return;
            try {
              if (ctx.state === "suspended") await ctx.resume();
              const msg = JSON.parse(event.data as string) as StreamAudioMessage;
              if (msg.audio) {
                nextStartTimeRef.current = schedulePcmChunk(
                  ctx,
                  gainNode,
                  msg.audio,
                  nextStartTimeRef.current,
                );
              }
              if (msg.isFinal) {
                streamEndTimeoutRef.current = setTimeout(() => {
                  setIsSpeaking(false);
                  streamEndTimeoutRef.current = null;
                }, Math.max(0, (nextStartTimeRef.current - ctx.currentTime) * 1000));
                try {
                  ws.close();
                } catch {
                  // ignore
                }
                wsRef.current = null;
                streamOpenPromiseRef.current = null;
              }
            } catch {
              // ignore parse error
            }
          };

          ws.onerror = () => {
            setIsSpeaking(false);
            wsRef.current = null;
            streamOpenPromiseRef.current = null;
          };

          ws.onclose = () => {
            wsRef.current = null;
            streamOpenPromiseRef.current = null;
          };
        })();
        streamOpenPromiseRef.current = openPromise;
        return openPromise;
      };

      const sendSegment = (s: string) => {
        const ws = wsRef.current;
        if (!ws || ws.readyState !== WebSocket.OPEN) return;
        const toSend = s.endsWith(" ") ? s : `${s} `;
        ws.send(JSON.stringify({ text: toSend }));
      };

      if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
        sendSegment(text);
      } else {
        void ensureStreamOpen().then(() => sendSegment(text));
      }
    },
    [],
  );

  const endStream = useCallback(
    (fullText?: string) => {
      const ws = wsRef.current;
      if (ws?.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({ text: "" }));
        return;
      }
      if (fullText?.trim()) speak(fullText.trim());
    },
    [speak],
  );

  return { speak, speakSentence, cancel, speakChunk, endStream, isSpeaking };
}
