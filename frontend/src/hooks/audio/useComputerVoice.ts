/**
 * Computer voice / TTS (Task 7). Uses ElevenLabs API with Aria or Charlotte voice.
 * No-op in SSR or when VITE_ELEVENLABS_API_KEY is not set.
 */
import { useCallback, useRef, useState } from "react";

const ELEVENLABS_VOICES_URL = "https://api.elevenlabs.io/v1/voices";
const PREFERRED_VOICE_NAMES = ["Adela", "Sarah", "Alice"];
const DEFAULT_MODEL_ID = "eleven_multilingual_v2";
const OUTPUT_FORMAT = "mp3_44100_128";
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

let hasWarnedNoApiKey = false;

export function useComputerVoice() {
  const [isSpeaking, setIsSpeaking] = useState(false);
  const voiceIdPromiseRef = useRef<Promise<string | null> | null>(null);
  const currentAudioRef = useRef<HTMLAudioElement | null>(null);
  const currentUrlRef = useRef<string | null>(null);
  const abortControllerRef = useRef<AbortController | null>(null);

  const cancel = useCallback(() => {
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

  return { speak, cancel, isSpeaking };
}
