function ResultsTable({ results }) {
  if (results.length === 0) {
    return <p className="empty-state">No results yet — try a search above.</p>;
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
          <tr key={log.log_id} className={`level-${log.level?.toLowerCase()}`}>
            <td>{new Date(Number(log.timestamp)).toLocaleTimeString()}</td>
            <td>{log.level}</td>
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