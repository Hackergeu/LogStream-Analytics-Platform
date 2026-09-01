
import { useEffect, useRef, useState } from 'react';

const WS_URL = 'ws://localhost:8080/ws/logs';
const MAX_LIVE_LOGS = 20;

function LiveTail() {
  const [liveLogs, setLiveLogs] = useState([]);
  const [connected, setConnected] = useState(false);
  const wsRef = useRef(null);

  useEffect(() => {
    const ws = new WebSocket(WS_URL);
    wsRef.current = ws;

    ws.onopen = () => setConnected(true);
    ws.onclose = () => setConnected(false);
    ws.onerror = (err) => console.error('WebSocket error:', err);

    ws.onmessage = (event) => {
      const log = JSON.parse(event.data);
      setLiveLogs((prev) => [log, ...prev].slice(0, MAX_LIVE_LOGS));
    };

    return () => ws.close();
  }, []);

  return (
    <section className="livetail-section">
      <h2>
        Live Tail{' '}
        <span className={`status-dot ${connected ? 'connected' : ''}`}></span>
        {connected ? 'connected' : 'disconnected'}
      </h2>

      <div className="livetail-list">
        {liveLogs.length === 0 && (
          <p className="empty-state">Waiting for new logs...</p>
        )}

        {liveLogs.map((log, idx) => (
          <div
            key={idx}
            className={`livetail-item level-${log.level?.toLowerCase()}`}
          >
            <span className="livetail-time">
              {new Date(Number(log.timestamp)).toLocaleTimeString()}
            </span>

            <span className="livetail-level">{log.level}</span>

            <span className="livetail-service">{log.service}</span>

            <span className="livetail-message">{log.message}</span>
          </div>
        ))}
      </div>
    </section>
  );
}

export default LiveTail;

