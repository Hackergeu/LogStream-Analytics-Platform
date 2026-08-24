import ReactECharts from 'echarts-for-react';

function VolumeChart({ timeSeries }) {
  const entries = Object.entries(timeSeries).sort((a, b) => a[0] - b[0]);
  const times = entries.map(([ts]) => new Date(Number(ts)).toLocaleTimeString());
  const counts = entries.map(([, count]) => count);

  const option = {
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: times },
    yAxis: { type: 'value' },
    series: [
      {
        data: counts,
        type: 'bar',
        itemStyle: { color: '#4f8cff' },
      },
    ],
    tooltip: { trigger: 'axis' },
  };

  return <ReactECharts option={option} style={{ height: '250px', width: '100%' }} />;
}

export default VolumeChart;