// 우측 챗봇 UI: 메시지 렌더링 + /api/chat 호출

const messagesEl = document.getElementById("chat-messages");
const formEl = document.getElementById("chat-form");
const inputEl = document.getElementById("chat-input");

function escapeHtml(text) {
  return text.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}

// 봇 응답에 섞여 오는 가벼운 마크다운(**볼드**, "- 목록")만 안전하게 HTML로 변환한다.
// 먼저 전체를 escape한 뒤 우리가 만든 태그만 다시 심으므로 응답 텍스트가 임의 HTML/스크립트로
// 해석되지 않는다.
function renderMarkdownLite(text) {
  const escaped = escapeHtml(text);
  const bolded = escaped.replace(/\*\*(.+?)\*\*/g, "<strong>$1</strong>");

  let html = "";
  let inList = false;

  for (const rawLine of bolded.split("\n")) {
    const line = rawLine.trim();
    const isListItem = line.startsWith("- ") || line.startsWith("* ");

    if (isListItem) {
      if (!inList) {
        html += "<ul>";
        inList = true;
      }
      html += `<li>${line.slice(2)}</li>`;
      continue;
    }

    if (inList) {
      html += "</ul>";
      inList = false;
    }
    if (line) {
      html += `<p>${line}</p>`;
    }
  }
  if (inList) {
    html += "</ul>";
  }
  return html;
}

function appendMessage(role, text) {
  const bubble = document.createElement("div");
  bubble.className = `chat-message ${role}`;
  if (role === "bot") {
    bubble.innerHTML = renderMarkdownLite(text);
  } else {
    bubble.textContent = text;
  }
  messagesEl.appendChild(bubble);
  messagesEl.scrollTop = messagesEl.scrollHeight;
}

// 봇 초기 인사 (문단 단위로 줄바꿈, 실제 줄바꿈은 CSS word-break: keep-all이 단어 단위로 처리)
appendMessage(
  "bot",
  [
    "안녕하세요! 최준영입니다.",
    "스마트팜 통합 솔루션 아키텍처를 참고해 실시간 데이터 처리 역량을 보여드리기 위해 만든 AI 모니터링 시스템입니다.",
    "궁금한 점이나 제어하고 싶은 장치가 있으면 말씀해주세요!",
  ].join("\n\n")
);

formEl.addEventListener("submit", async (event) => {
  event.preventDefault();

  const message = inputEl.value.trim();
  if (!message) {
    return;
  }

  appendMessage("user", message);
  inputEl.value = "";

  try {
    const response = await fetch("/api/chat", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ message }),
    });
    const data = await response.json();
    appendMessage("bot", data.reply);
  } catch (error) {
    appendMessage("bot", "응답을 받아오지 못했습니다. 잠시 후 다시 시도해주세요.");
  }
});
