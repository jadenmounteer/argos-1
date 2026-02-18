import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import {
  render,
  screen,
  fireEvent,
  act,
  cleanup,
} from "@testing-library/react";
import { __setPorcupineMock } from "@picovoice/porcupine-react";
import { VocalProvider, useVocal } from "./VocalContext.tsx";

function Consumer() {
  const { listeningMode, porcupineLoaded } = useVocal();
  return (
    <span data-testid="consumer">
      {listeningMode ? "on" : "off"}-{porcupineLoaded ? "loaded" : "not-loaded"}
    </span>
  );
}

function createMockResult(transcript: string): SpeechRecognitionResult {
  return {
    length: 1,
    isFinal: true,
    item: (i: number) =>
      i === 0
        ? { transcript, confidence: 1 }
        : { transcript: "", confidence: 0 },
    0: { transcript, confidence: 1 },
  } as unknown as SpeechRecognitionResult;
}

function createMockResultList(
  transcripts: string[],
): SpeechRecognitionResultList {
  const results = transcripts.map(createMockResult);
  return {
    length: results.length,
    item: (i: number) => results[i]!,
    ...Object.fromEntries(results.map((r, i) => [i, r])),
  } as unknown as SpeechRecognitionResultList;
}

function createMockRecognitionEvent(
  resultIndex: number,
  transcripts: string[],
): SpeechRecognitionEvent {
  return {
    resultIndex,
    results: createMockResultList(transcripts),
  } as unknown as SpeechRecognitionEvent;
}

describe("VocalProvider", () => {
  let mockRecognitionInstance: {
    onresult: ((e: SpeechRecognitionEvent) => void) | null;
    start: () => void;
    stop: () => void;
    continuous?: boolean;
    interimResults?: boolean;
  } | null = null;

  beforeEach(() => {
    vi.useFakeTimers();
    mockRecognitionInstance = null;
    function MockSpeechRecognition() {
      const instance: typeof mockRecognitionInstance = {
        onresult: null,
        start: vi.fn(() => {
          mockRecognitionInstance = instance;
        }),
        stop: vi.fn(),
        continuous: true,
        interimResults: true,
      };
      return instance;
    }
    vi.stubGlobal(
      "SpeechRecognition",
      vi.fn().mockImplementation(MockSpeechRecognition),
    );
    vi.stubGlobal(
      "webkitSpeechRecognition",
      vi.fn().mockImplementation(MockSpeechRecognition),
    );
  });

  afterEach(() => {
    cleanup();
    vi.useRealTimers();
    mockRecognitionInstance = null;
  });

  it("renders children and provides context", () => {
    render(
      <VocalProvider onCommandReady={() => {}}>
        <Consumer />
      </VocalProvider>,
    );
    expect(screen.getByTestId("consumer")).toBeInTheDocument();
    expect(screen.getByTestId("consumer").textContent).toBe("off-not-loaded");
  });

  it("triggerChime and inhibit are callable and do not throw", () => {
    render(
      <VocalProvider onCommandReady={() => {}}>
        <VocalTestButtons />
      </VocalProvider>,
    );
    expect(() =>
      fireEvent.click(screen.getAllByTestId("trigger-chime")[0]!),
    ).not.toThrow();
    expect(() =>
      fireEvent.click(screen.getAllByTestId("inhibit-true")[0]!),
    ).not.toThrow();
    expect(() =>
      fireEvent.click(screen.getAllByTestId("inhibit-false")[0]!),
    ).not.toThrow();
  });

  it("silence detection: after 1.5s without result, calls onCommandReady and reverts to passive", async () => {
    const onCommandReady = vi.fn();
    render(
      <VocalProvider onCommandReady={onCommandReady}>
        <VocalTestButtons />
      </VocalProvider>,
    );
    act(() => {
      fireEvent.click(screen.getAllByTestId("toggle-listening")[0]!);
    });
    __setPorcupineMock(true, { label: "argos-one", index: 0 });
    act(() => {
      fireEvent.click(screen.getAllByTestId("toggle-listening")[0]!);
    });
    await act(async () => {
      await Promise.resolve();
    });
    const rec = mockRecognitionInstance;
    expect(rec).not.toBeNull();
    act(() => {
      rec!.onresult!(createMockRecognitionEvent(0, ["hello world"]));
    });
    act(() => {
      vi.advanceTimersByTime(1500);
    });
    expect(onCommandReady).toHaveBeenCalledWith("hello world");
    expect(screen.getAllByTestId("is-active")[0]!.textContent).toBe("false");
  });

  it("does not strip wake phrase from transcript", async () => {
    const onCommandReady = vi.fn();
    render(
      <VocalProvider onCommandReady={onCommandReady}>
        <VocalTestButtons />
      </VocalProvider>,
    );
    act(() => {
      fireEvent.click(screen.getAllByTestId("toggle-listening")[0]!);
    });
    __setPorcupineMock(true, { label: "argos-one", index: 0 });
    act(() => {
      fireEvent.click(screen.getAllByTestId("toggle-listening")[0]!);
    });
    await act(async () => {
      await Promise.resolve();
    });
    const rec = mockRecognitionInstance;
    expect(rec).not.toBeNull();
    act(() => {
      rec!.onresult!(
        createMockRecognitionEvent(0, ["Argos one what is the weather"]),
      );
    });
    act(() => {
      vi.advanceTimersByTime(1500);
    });
    expect(onCommandReady).toHaveBeenCalledWith(
      "Argos one what is the weather",
    );
  });

  it("gatekeeper: when inhibited, does not call onCommandReady on result", async () => {
    const onCommandReady = vi.fn();
    render(
      <VocalProvider onCommandReady={onCommandReady}>
        <VocalTestButtons />
      </VocalProvider>,
    );
    act(() => {
      fireEvent.click(screen.getAllByTestId("toggle-listening")[0]!);
    });
    __setPorcupineMock(true, { label: "argos-one", index: 0 });
    act(() => {
      fireEvent.click(screen.getAllByTestId("toggle-listening")[0]!);
    });
    await act(async () => {
      await Promise.resolve();
    });
    act(() => {
      fireEvent.click(screen.getAllByTestId("inhibit-true")[0]!);
    });
    const rec = mockRecognitionInstance;
    expect(rec).not.toBeNull();
    act(() => {
      rec!.onresult!(createMockRecognitionEvent(0, ["ignored"]));
    });
    act(() => {
      vi.advanceTimersByTime(1500);
    });
    expect(onCommandReady).not.toHaveBeenCalled();
  });
});

function VocalTestButtons() {
  const v = useVocal();
  return (
    <>
      <Consumer />
      <span data-testid="is-active">{String(v.isActive)}</span>
      <button
        data-testid="toggle-listening"
        onClick={v.toggleListeningMode}
        type="button"
      >
        Toggle
      </button>
      <button
        data-testid="trigger-chime"
        onClick={() => v.triggerChime()}
        type="button"
      >
        Chime
      </button>
      <button
        data-testid="inhibit-true"
        onClick={() => v.inhibit(true)}
        type="button"
      >
        Inhibit
      </button>
      <button
        data-testid="inhibit-false"
        onClick={() => v.inhibit(false)}
        type="button"
      >
        Uninhibit
      </button>
    </>
  );
}
