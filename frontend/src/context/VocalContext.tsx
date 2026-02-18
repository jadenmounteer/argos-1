/* eslint-disable react-refresh/only-export-components -- context exports Provider and hook */
import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useRef,
  useState,
  type ReactNode,
} from "react";
import { usePorcupine } from "@picovoice/porcupine-react";
import {
  playWakeWordDetected,
  playComputerOn,
  playComputerOff,
} from "../audio/audioBridge";

const SILENCE_MS = 1500;
const WAKE_KEYWORD_PATH = "/models/argos-one_en_wasm_v4_0_0.ppn";
const PORCUPINE_MODEL_PATH = "/models/porcupine_params.pv";

export type VocalContextValue = {
  listeningMode: boolean;
  toggleListeningMode: () => void;
  isActive: boolean;
  promptText: string;
  setPromptText: (value: string) => void;
  porcupineLoaded: boolean;
  /** When listening is on but wake word isn't ready: reason to show in UI (e.g. missing key). */
  wakeWordUnavailableReason: string | null;
  triggerChime: () => void;
  inhibit: (value: boolean) => void;
};

const VocalContext = createContext<VocalContextValue | null>(null);

function getSpeechRecognition(): (new () => SpeechRecognition) | null {
  if (typeof window === "undefined") return null;
  const Win = window as unknown as {
    SpeechRecognition?: new () => SpeechRecognition;
    webkitSpeechRecognition?: new () => SpeechRecognition;
  };
  return Win.SpeechRecognition ?? Win.webkitSpeechRecognition ?? null;
}

export function VocalProvider({
  children,
  onCommandReady,
}: {
  children: ReactNode;
  onCommandReady: (transcript: string) => void;
}) {
  const [listeningMode, setListeningMode] = useState(false);
  const [isActive, setIsActive] = useState(false);
  const [promptText, setPromptTextState] = useState("");
  const isInhibitedRef = useRef(false);
  const silenceTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const recognitionRef = useRef<SpeechRecognition | null>(null);
  const transcriptAccumulatorRef = useRef("");
  const lastAddedResultIndexRef = useRef(-1);
  const onCommandReadyRef = useRef(onCommandReady);
  const prevListeningModeRef = useRef<boolean | undefined>(undefined);
  useEffect(() => {
    onCommandReadyRef.current = onCommandReady;
  }, [onCommandReady]);

  const triggerChime = useCallback(() => {
    playWakeWordDetected();
  }, []);

  const inhibit = useCallback((value: boolean) => {
    isInhibitedRef.current = value;
  }, []);

  const setPromptText = useCallback((value: string) => {
    setPromptTextState(value);
  }, []);

  const finalizeAndRevert = useCallback(() => {
    if (silenceTimerRef.current) {
      clearTimeout(silenceTimerRef.current);
      silenceTimerRef.current = null;
    }
    const rec = recognitionRef.current;
    if (rec) {
      try {
        rec.stop();
      } catch {
        // ignore
      }
      recognitionRef.current = null;
    }
    const text = transcriptAccumulatorRef.current.trim();
    if (text) onCommandReadyRef.current(text);
    transcriptAccumulatorRef.current = "";
    lastAddedResultIndexRef.current = -1;
    setIsActive(false);
  }, []);

  const startRecognition = useCallback(() => {
    const SpeechRecognition = getSpeechRecognition();
    if (!SpeechRecognition) {
      console.warn("[Vocal] SpeechRecognition not supported in this browser");
      return;
    }
    transcriptAccumulatorRef.current = "";
    lastAddedResultIndexRef.current = -1;
    const rec = new SpeechRecognition();
    rec.continuous = true;
    rec.interimResults = true;
    rec.lang = "en-US";
    rec.onresult = (event: SpeechRecognitionEvent) => {
      if (isInhibitedRef.current) return;
      if (silenceTimerRef.current) {
        clearTimeout(silenceTimerRef.current);
      }
      const { results } = event;
      let display = "";
      for (let i = 0; i < results.length; i += 1) {
        const result = results[i]!;
        const text = result[0]!.transcript;
        display += text;
        if (result.isFinal && i > lastAddedResultIndexRef.current) {
          transcriptAccumulatorRef.current += text;
          lastAddedResultIndexRef.current = i;
        }
      }
      setPromptTextState(display.trim());
      silenceTimerRef.current = setTimeout(finalizeAndRevert, SILENCE_MS);
    };
    rec.onerror = (event?: Event) => {
      const err = event
        ? (event as unknown as { error?: string }).error
        : undefined;
      console.error("[Vocal] SpeechRecognition error:", err ?? event);
      finalizeAndRevert();
    };
    rec.onend = () => {
      if (recognitionRef.current === rec) {
        recognitionRef.current = null;
      }
    };
    try {
      rec.start();
      recognitionRef.current = rec;
    } catch (err) {
      console.error("[Vocal] SpeechRecognition start failed:", err);
      finalizeAndRevert();
    }
  }, [finalizeAndRevert]);

  const {
    keywordDetection,
    isLoaded: porcupineLoaded,
    error: porcupineError,
    init: porcupineInit,
    start: porcupineStart,
    stop: porcupineStop,
    release: porcupineRelease,
  } = usePorcupine();

  const accessKey =
    typeof import.meta !== "undefined" &&
    import.meta.env?.VITE_PICOVOICE_ACCESS_KEY
      ? String(import.meta.env.VITE_PICOVOICE_ACCESS_KEY).trim()
      : "";
  const initAttemptedRef = useRef(false);

  // On wake word: trigger chime, start SpeechRecognition, go active (defer setState to avoid set-state-in-effect)
  const prevKeywordRef = useRef<typeof keywordDetection>(null);
  useEffect(() => {
    if (
      keywordDetection !== null &&
      keywordDetection !== prevKeywordRef.current &&
      !isActive &&
      porcupineLoaded &&
      !isInhibitedRef.current
    ) {
      prevKeywordRef.current = keywordDetection;
      triggerChime();
      queueMicrotask(() => {
        setPromptTextState("");
        setIsActive(true);
        startRecognition();
      });
    } else if (keywordDetection === null) {
      prevKeywordRef.current = null;
    }
  }, [
    keywordDetection,
    isActive,
    porcupineLoaded,
    triggerChime,
    startRecognition,
  ]);

  const toggleListeningMode = useCallback(() => {
    setListeningMode((prev) => !prev);
  }, []);

  // Init Porcupine when listening mode turns on and we have a key.
  // Do not guard on porcupineError: the hook can start with an error state before we call init,
  // which would prevent init from ever running.
  useEffect(() => {
    if (!listeningMode || porcupineLoaded || initAttemptedRef.current) {
      return;
    }
    if (!accessKey) {
      console.warn(
        "[Vocal] Porcupine init skipped: missing VITE_PICOVOICE_ACCESS_KEY. " +
          "Add it to frontend/.env and restart the dev server. See frontend/.env-example.",
      );
      return;
    }
    initAttemptedRef.current = true;
    porcupineInit(
      accessKey,
      [{ publicPath: WAKE_KEYWORD_PATH, label: "argos-one" }],
      { publicPath: PORCUPINE_MODEL_PATH },
    ).catch((err: unknown) => {
      initAttemptedRef.current = false;
      const msg = err instanceof Error ? err.message : String(err);
      console.error("[Vocal] Porcupine init failed:", msg, err);
    });
  }, [listeningMode, accessKey, porcupineLoaded, porcupineInit]);

  // When listening mode turns off, release Porcupine
  useEffect(() => {
    if (!listeningMode) {
      initAttemptedRef.current = false;
      porcupineRelease?.();
    }
  }, [listeningMode, porcupineRelease]);

  // Start/stop Porcupine based on listening mode and active state
  useEffect(() => {
    if (listeningMode && porcupineLoaded && !isActive) {
      porcupineStart?.();
    } else {
      porcupineStop?.();
    }
  }, [listeningMode, porcupineLoaded, isActive, porcupineStart, porcupineStop]);

  // Channel 3 (Persona): COMPUTER_ON / COMPUTER_OFF when listening mode toggles
  useEffect(() => {
    if (prevListeningModeRef.current !== undefined) {
      if (listeningMode) {
        playComputerOn();
      } else {
        playComputerOff();
      }
    }
    prevListeningModeRef.current = listeningMode;
  }, [listeningMode]);

  const wakeWordUnavailableReason: string | null =
    listeningMode && !porcupineLoaded
      ? !accessKey
        ? "Add VITE_PICOVOICE_ACCESS_KEY to frontend/.env and restart dev server"
        : porcupineError
          ? porcupineError.message || "Wake word unavailable"
          : null
      : null;

  const value: VocalContextValue = {
    listeningMode,
    toggleListeningMode,
    isActive,
    promptText,
    setPromptText,
    porcupineLoaded: porcupineLoaded ?? false,
    wakeWordUnavailableReason,
    triggerChime,
    inhibit,
  };

  return (
    <VocalContext.Provider value={value}>{children}</VocalContext.Provider>
  );
}

export function useVocal(): VocalContextValue {
  const ctx = useContext(VocalContext);
  if (!ctx) {
    throw new Error("useVocal must be used within VocalProvider");
  }
  return ctx;
}
