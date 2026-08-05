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

const chart = new Chart(document.getElementById("sensor-chart"), {
  type: "line",
  data: {
    labels: [],
    datasets: [
      { label: "온도", data: [], borderColor: "#ff6b6b", tension: 0.3 },
      { label: "습도", data: [], borderColor: "#4dabf7", tension: 0.3 },
      { label: "토양수분", data: [], borderColor: "#a9723d", tension: 0.3 },
      { label: "CO2", data: [], borderColor: "#94d82d", tension: 0.3 },
      { label: "조도", data: [], borderColor: "#ffd43b", tension: 0.3 },
    ],
  },
  options: {
    responsive: true,
    maintainAspectRatio: false,
    animation: false,
    scales: {
      x: { ticks: { color: "#9aa4af" } },
      y: { ticks: { color: "#9aa4af" } },
    },
    plugins: {
      legend: { labels: { color: "#e6e8eb" } },
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
