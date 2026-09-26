function ResultsTable({ results }) {
  if (results.length === 0) {
    return (
      <div className="empty-state">
        <span className="empty-state-icon">⌁</span>
        <span className="empty-state-text">No results yet — try a search above.</span>
      </div>
    );
  }

  return (
    <table className="results-table">
      <thead>
        <tr>
          <th>Time</th>
          <th>Level</th>
          <th>Service</th>
          <th>Response (ms)</th>
          <th>Message</th>
        </tr>
      </thead>
      <tbody>
        {results.map((log) => (
          <tr key={log.log_id}>
            <td>{new Date(Number(log.timestamp)).toLocaleTimeString()}</td>
            <td>
              <span className={`level-badge ${log.level}`}>{log.level}</span>
            </td>
            <td>{log.service}</td>
            <td>{log.response_time_ms}</td>
            <td>{log.message}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

export default ResultsTable;