import { useState } from 'react';

const API_BASE = 'http://localhost:8081/api/logs';

function LogGenerator() {
  const [level, setLevel] = useState('ERROR');
  const [service, setService] = useState('billing-api');
  const [message, setMessage] = useState('Payment gateway timeout after 30s');
  const [responseTimeMs, setResponseTimeMs] = useState(1200);
  const [status, setStatus] = useState('');

  const sendLog = async () => {
    setStatus('Sending...');
    try {
      const res = await fetch(`${API_BASE}/ingest`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ level, service, message, responseTimeMs: String(responseTimeMs) }),
      });
      const data = await res.json();
      setStatus(data.success ? `Sent (id: ${data.log_id.slice(0, 8)}...)` : 'Failed');
    } catch (err) {
      setStatus('Error: ' + err.message);
    }
  };

  return (
    <section className="generator-section">
      <h2>Send Test Log</h2>
      <div className="generator-form">
        <select value={level} onChange={(e) => setLevel(e.target.value)}>
          <option>INFO</option>
          <option>WARN</option>
          <option>ERROR</option>
        </select>
        <input
          type="text"
          value={service}
          onChange={(e) => setService(e.target.value)}
          placeholder="service name"
        />
        <input
          type="text"
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          placeholder="log message"
          className="message-input"
        />
        <input
          type="number"
          value={responseTimeMs}
          onChange={(e) => setResponseTimeMs(e.target.value)}
          placeholder="response ms"
        />
        <button onClick={sendLog}>Send</button>
      </div>
      {status && <p className="generator-status">{status}</p>}
    </section>
  );
}

export default LogGenerator;