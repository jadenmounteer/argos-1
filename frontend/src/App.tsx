import { useCallback, useState } from "react";
import { VocalProvider } from "./context/VocalContext";
import { useComputerVoice } from "./hooks/audio/useComputerVoice";
import { useIntelligenceStream } from "./hooks/useIntelligenceStream";
import { AppShell } from "./components/layout/AppShell";

function AppWithVocal() {
  const [isGrounded, setIsGrounded] = useState(false);
  const { speakSentence, cancel, isSpeaking, speakingOn, setSpeakingOn } =
    useComputerVoice();
  const {
    sendCommand: streamSendCommand,
    thoughtLog,
    mainResponse,
    isStreaming,
  } = useIntelligenceStream({
    onResponseSentence: (sentence) => speakSentence(sentence),
    isGrounded,
  });
  const sendCommand = useCallback(
    (text: string) => {
      cancel();
      streamSendCommand(text);
    },
    [cancel, streamSendCommand],
  );
  return (
    <VocalProvider
      onCommandReady={sendCommand}
      inhibitWhileSpeaking={isSpeaking}
    >
      <AppShell
        sendCommand={sendCommand}
        thoughtLog={thoughtLog}
        mainResponse={mainResponse}
        isStreaming={isStreaming}
        speakingOn={speakingOn}
        setSpeakingOn={setSpeakingOn}
        isGrounded={isGrounded}
        setIsGrounded={setIsGrounded}
      />
    </VocalProvider>
  );
}

export default function App() {
  return <AppWithVocal />;
}
