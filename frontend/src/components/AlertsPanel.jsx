import { useEffect, useState } from 'react';

const API_BASE = 'http://localhost:8080/api/alerts';

function AlertsPanel() {
  const [alerts, setAlerts] = useState([]);

  useEffect(() => {
    const fetchAlerts = async () => {
      try {
        const res = await fetch(`${API_BASE}/triggered`);
        const data = await res.json();
        setAlerts(data);
      } catch (err) {
        console.error('Failed to fetch alerts:', err);
      }
    };

    fetchAlerts();
    const interval = setInterval(fetchAlerts, 10000); // poll every 10s
    return () => clearInterval(interval);
  }, []);

  if (alerts.length === 0) return null; // nothing to show, stay out of the way

  return (
    <section className="alerts-panel">
      <h2>🚨 Active Alerts</h2>
      {alerts.map((alert, idx) => (
        <div key={idx} className="alert-item">
          <strong>{alert.ruleName}</strong> — {alert.matchCount} matches
          (threshold: {alert.threshold}) for <code>{alert.query}</code>
          <span className="alert-time">
            {new Date(alert.triggeredAt).toLocaleTimeString()}
          </span>
        </div>
      ))}
    </section>
  );
}

export default AlertsPanel;