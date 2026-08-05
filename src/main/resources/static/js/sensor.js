// SSE로 센서 데이터를 실시간 수신해 대시보드 수치, 차트, 이벤트 시각 효과를 갱신한다.

const MAX_POINTS = 30; // 차트에 표시할 최근 데이터 개수

const stage = document.getElementById("stage");

const statEls = {
  temperature: document.getElementById("stat-temperature"),
  humidity: document.getElementById("stat-humidity"),
  soilMoisture: document.getElementById("stat-soilMoisture"),
  co2: document.getElementById("stat-co2"),
  light: document.getElementById("stat-light"),
};

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
        ticks: { color: "#898781", font: { size: 11 }, maxRotation: 0 },
      },
      y: {
        grid: { color: "#2c2c2a" },
        border: { display: false },
        ticks: { color: "#898781", font: { size: 11 } },
      },
    },
    plugins: {
      legend: {
        position: "top",
        align: "start",
        labels: {
          color: "#c3c2b7",
          font: { size: 13 },
          usePointStyle: true,
          pointStyle: "circle",
          boxWidth: 8,
          boxHeight: 8,
          padding: 16,
        },
      },
      tooltip: {
        backgroundColor: "#22282f",
        titleColor: "#e6e8eb",
        bodyColor: "#e6e8eb",
        borderColor: "#383835",
        borderWidth: 1,
        padding: 10,
        boxPadding: 4,
        usePointStyle: true,
      },
    },
  },
});

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
  updateChart(data);
});
