// SSE로 센서 데이터를 실시간 수신해 대시보드 수치, 차트, 이벤트 시각 효과를 갱신한다.

const MAX_POINTS = 30; // 차트에 표시할 최근 데이터 개수

// Chart.js는 canvas에 직접 텍스트를 그리므로 CSS font-family를 상속받지 않는다.
// 기본값(Helvetica/Arial)에는 한글 글리프가 없어 브라우저가 저품질 폴백 폰트로 그려
// legend/축 라벨만 화면의 다른 텍스트보다 눌리고 흐릿하게 보였다. 명시적으로 지정한다.
Chart.defaults.font.family = "'Pretendard', 'Malgun Gothic', sans-serif";

const stage = document.getElementById("stage");
const bellEl = document.getElementById("alert-bell");
const bellBadgeEl = document.getElementById("alert-bell-badge");
const alertPanelEl = document.getElementById("alert-panel");

const statEls = {
  temperature: document.getElementById("stat-temperature"),
  humidity: document.getElementById("stat-humidity"),
  soilMoisture: document.getElementById("stat-soilMoisture"),
  co2: document.getElementById("stat-co2"),
  light: document.getElementById("stat-light"),
};

// 색/애니메이션만으로는 "지금 무슨 이벤트가 발생했는지" 알기 어렵다는 피드백에 따라
// 이벤트마다 아이콘 + 이름 + 해결 명령을 명시하고, 알림 패널에서 바로 명령을 보낼 수 있게 한다.
const EVENT_META = {
  temperatureWarning: {
    icon: "🔥",
    label: "온실 과열",
    command: "환풍기 켜줘",
    detail: (d) => `${d.temperature.toFixed(1)}도 (35도 이상)`,
  },
  soilMoistureWarning: {
    icon: "🌵",
    label: "토양수분 부족",
    command: "스프링클러 켜줘",
    detail: (d) => `${d.soilMoisture.toFixed(1)}% (30% 이하)`,
  },
  humidityWarning: {
    icon: "💧",
    label: "내부 과습",
    command: "제습기 켜줘",
    detail: (d) => `${d.humidity.toFixed(1)}% (85% 이상)`,
  },
  lightWarning: {
    icon: "🌑",
    label: "일조량 부족",
    command: "보광등 켜줘",
    detail: (d) => `${d.light.toFixed(0)}lux (200lux 미만)`,
  },
  co2Warning: {
    icon: "🫧",
    label: "CO2 부족",
    command: "탄산가스 켜줘",
    detail: (d) => `${d.co2.toFixed(0)}ppm (350ppm 미만)`,
  },
};

// 직전 tick의 경고 상태 - 새로 시작된 경고에만 벨을 흔들기 위해 기억해둔다
let previousWarnings = {};
let latestSensorData = null;

// 패널을 열어둔 동안에는 목록이 갱신돼도 매번 다시 렌더링해 최신 상태를 보여준다
function renderAlertPanel() {
  const activeKeys = Object.keys(EVENT_META).filter((key) => previousWarnings[key]);

  if (activeKeys.length === 0) {
    alertPanelEl.innerHTML = '<div class="empty">현재 활성화된 이벤트가 없습니다.</div>';
    return;
  }

  alertPanelEl.innerHTML = activeKeys
    .map((key) => {
      const meta = EVENT_META[key];
      return `
        <div class="alert-panel-item">
          <span class="icon">${meta.icon}</span>
          <span class="alert-panel-item-body">
            <span class="label">${meta.label}</span>
            <span class="detail">${meta.detail(latestSensorData)}</span>
          </span>
          <button type="button" class="alert-panel-action" data-command="${meta.command}">정상화</button>
        </div>
      `;
    })
    .join("");
}

// 버튼을 누르면 직접 타이핑하지 않아도 같은 명령을 챗봇에 대신 보내준다
alertPanelEl.addEventListener("click", (event) => {
  const button = event.target.closest(".alert-panel-action");
  if (!button || typeof window.sendChatCommand !== "function") {
    return;
  }
  button.disabled = true;
  button.textContent = "요청 중…";
  window.sendChatCommand(button.dataset.command);
});

function updateAlerts(data) {
  latestSensorData = data;

  const currentWarnings = {};
  for (const key of Object.keys(EVENT_META)) {
    currentWarnings[key] = Boolean(data[key]);
  }

  const activeCount = Object.values(currentWarnings).filter(Boolean).length;
  let hasNewWarning = false;
  for (const key of Object.keys(EVENT_META)) {
    if (currentWarnings[key] && !(previousWarnings[key] ?? false)) {
      hasNewWarning = true;
    }
  }
  previousWarnings = currentWarnings;

  // 배지 개수만 갱신 - 화면에 계속 뭔가 뜨는 대신 벨을 눌러야 목록이 보인다
  if (activeCount > 0) {
    bellBadgeEl.hidden = false;
    bellBadgeEl.textContent = String(activeCount);
  } else {
    bellBadgeEl.hidden = true;
  }

  // 새 이벤트가 시작된 순간만 벨을 살짝 흔들어 존재를 알린다 (모달/토스트 없이)
  if (hasNewWarning) {
    bellEl.classList.remove("ring");
    void bellEl.offsetWidth; // 애니메이션 재시작을 위해 강제로 리플로우
    bellEl.classList.add("ring");
  }

  if (!alertPanelEl.hidden) {
    renderAlertPanel();
  }
}

bellEl.addEventListener("click", () => {
  alertPanelEl.hidden = !alertPanelEl.hidden;
  if (!alertPanelEl.hidden) {
    renderAlertPanel();
  }
});

// 5개 센서를 구분하는 카테고리 색상 (dataviz 팔레트, 다크 서피스 기준 검증됨)
const SERIES_COLORS = {
  온도: "#3987e5",
  습도: "#d95926",
  토양수분: "#199e70",
  CO2: "#c98500",
  조도: "#d55181",
};

function withOpacity(hex, alpha) {
  const r = parseInt(hex.slice(1, 3), 16);
  const g = parseInt(hex.slice(3, 5), 16);
  const b = parseInt(hex.slice(5, 7), 16);
  return `rgba(${r}, ${g}, ${b}, ${alpha})`;
}

function makeDataset(label) {
  const color = SERIES_COLORS[label];
  return {
    label,
    data: [],
    borderColor: color,
    backgroundColor: withOpacity(color, 0.1),
    borderWidth: 2,
    borderCapStyle: "round",
    borderJoinStyle: "round",
    tension: 0.35,
    fill: false,
    pointRadius: 0,
    pointHoverRadius: 4,
    pointHoverBackgroundColor: color,
    pointHoverBorderColor: "#12161b",
    pointHoverBorderWidth: 2,
  };
}

const chart = new Chart(document.getElementById("sensor-chart"), {
  type: "line",
  data: {
    labels: [],
    datasets: ["온도", "습도", "토양수분", "CO2", "조도"].map(makeDataset),
  },
  options: {
    responsive: true,
    maintainAspectRatio: false,
    animation: false,
    interaction: { mode: "nearest", intersect: false },
    layout: { padding: { top: 4 } },
    scales: {
      x: {
        grid: { display: false },
        border: { display: false },
        ticks: { color: "#898781", font: { size: 12, family: "'Pretendard', 'Malgun Gothic', sans-serif" }, maxRotation: 0 },
      },
      y: {
        grid: { color: "#2c2c2a" },
        border: { display: false },
        ticks: { color: "#898781", font: { size: 12, family: "'Pretendard', 'Malgun Gothic', sans-serif" } },
      },
    },
    plugins: {
      legend: {
        position: "top",
        align: "start",
        labels: {
          color: "#c3c2b7",
          font: { size: 13, family: "'Pretendard', 'Malgun Gothic', sans-serif" },
          usePointStyle: true,
          pointStyle: "circle",
          boxWidth: 8,
          boxHeight: 8,
          padding: 14,
          textAlign: "left",
        },
      },
      tooltip: {
        backgroundColor: "#22282f",
        titleColor: "#e6e8eb",
        bodyColor: "#e6e8eb",
        titleFont: { family: "'Pretendard', 'Malgun Gothic', sans-serif" },
        bodyFont: { family: "'Pretendard', 'Malgun Gothic', sans-serif" },
        borderColor: "#383835",
        borderWidth: 1,
        padding: 10,
        boxPadding: 4,
        usePointStyle: true,
      },
    },
  },
});

// canvas 텍스트는 웹폰트 로드 완료 시점에 자동으로 다시 그려지지 않으므로
// Pretendard 로딩이 첫 렌더보다 늦게 끝나면 폴백 폰트로 남을 수 있다 - 로드 완료 후 한 번 갱신
document.fonts.ready.then(() => chart.update());

function updateStats(data) {
  statEls.temperature.textContent = `${data.temperature.toFixed(1)}도`;
  statEls.humidity.textContent = `${data.humidity.toFixed(1)}%`;
  statEls.soilMoisture.textContent = `${data.soilMoisture.toFixed(1)}%`;
  statEls.co2.textContent = `${data.co2.toFixed(0)}ppm`;
  statEls.light.textContent = `${data.light.toFixed(0)}lux`;

  // CO2 부족 경고: 폰트 색상만 변경
  statEls.co2.classList.toggle("warning", data.co2Warning);
}

function updateStageEffects(data) {
  stage.classList.toggle("warning-soil", data.soilMoistureWarning);
  stage.classList.toggle("warning-temp", data.temperatureWarning);
  stage.classList.toggle("warning-humidity", data.humidityWarning);
  stage.classList.toggle("warning-light", data.lightWarning);
}

function updateChart(data) {
  const label = new Date(data.createdAt).toLocaleTimeString("ko-KR");

  chart.data.labels.push(label);
  chart.data.datasets[0].data.push(data.temperature);
  chart.data.datasets[1].data.push(data.humidity);
  chart.data.datasets[2].data.push(data.soilMoisture);
  chart.data.datasets[3].data.push(data.co2);
  chart.data.datasets[4].data.push(data.light);

  if (chart.data.labels.length > MAX_POINTS) {
    chart.data.labels.shift();
    chart.data.datasets.forEach((dataset) => dataset.data.shift());
  }

  chart.update();
}

const eventSource = new EventSource("/api/sensor/stream");

eventSource.addEventListener("sensor-data", (event) => {
  const data = JSON.parse(event.data);
  updateStats(data);
  updateStageEffects(data);
  updateAlerts(data);
  updateChart(data);
});
