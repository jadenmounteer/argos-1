import { MainWrapper } from "../MainWrapper";
import { Header } from "../Header";
import { ButtonStrip } from "../ButtonStrip";
import { PromptArea } from "../PromptArea";
import { LogsView } from "../LogsView";
import { LcarsButton } from "@starfleet-technology/lcars-react";
import { useVocal } from "../../context/VocalContext";

/**
 * Root layout: main wrapper (frame with title break), then two columns —
 * left: buttons + prompt, right: logs and responses.
 */
export function AppShell({
  sendCommand,
  thoughtLog = "",
  mainResponse = "",
  isStreaming = false,
  speakingOn = false,
  setSpeakingOn,
}: {
  sendCommand: (text: string) => void;
  thoughtLog?: string;
  mainResponse?: string;
  isStreaming?: boolean;
  speakingOn?: boolean;
  setSpeakingOn?: (value: boolean) => void;
}) {
  const { listeningMode, promptText } = useVocal();

  const handleSubmit = () => {
    sendCommand(promptText.trim());
  };

  return (
    <MainWrapper titleSlot={<Header />} loading={isStreaming}>
      <div className="app-shell__body">
        <aside className="aside-panel">
          <ButtonStrip
            speakingOn={speakingOn}
            setSpeakingOn={setSpeakingOn ?? (() => {})}
          />
          <PromptArea />
          {!listeningMode && (
            <LcarsButton color="primary" onClick={handleSubmit}>
              <p>Execute</p>
            </LcarsButton>
          )}
        </aside>
        <main className="logs-column">
          <LogsView
            thoughtLog={thoughtLog}
            mainResponse={mainResponse}
            isStreaming={isStreaming}
          />
        </main>
      </div>
    </MainWrapper>
  );
}
