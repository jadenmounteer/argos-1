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
/** Matches PR review intent; keep in sync with backend CommandGateway. */
const PR_INTENT_REGEX = /(?:review|analyze|check|scan)\s+(?:pull\s+request|pr)\s+(?:number\s+)?(\d+)/i;

export function AppShell({
  sendCommand,
  thoughtLog = "",
  mainResponse = "",
  isStreaming = false,
  speakingOn = false,
  setSpeakingOn,
  isGrounded = false,
  setIsGrounded,
}: {
  sendCommand: (text: string) => void;
  thoughtLog?: string;
  mainResponse?: string;
  isStreaming?: boolean;
  speakingOn?: boolean;
  setSpeakingOn?: (value: boolean) => void;
  isGrounded?: boolean;
  setIsGrounded?: (value: boolean) => void;
}) {
  const { listeningMode, promptText } = useVocal();
  const prCommandDetected = PR_INTENT_REGEX.test(promptText.trim());

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
            isGrounded={isGrounded}
            setIsGrounded={setIsGrounded ?? (() => {})}
          />
          {prCommandDetected && (
            <div className="tactical-hud-badge" role="status">
              🛰️ COMMAND: PR_REVIEW_ACTIVE
            </div>
          )}
          <PromptArea isGrounded={isGrounded} />
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
