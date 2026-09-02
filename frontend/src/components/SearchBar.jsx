import { useState } from 'react';

const API_BASE = 'http://localhost:8081/api/logs';

function SearchBar({ setResults, setTimeSeries, setLoading }) {
  const [query, setQuery] = useState('*:*');

  const runSearch = async () => {
    setLoading(true);
    try {
      const searchRes = await fetch(`${API_BASE}/search?q=${encodeURIComponent(query)}`);
      const searchData = await searchRes.json();
      setResults(searchData);

      const tsRes = await fetch(`${API_BASE}/timeseries`);
      const tsData = await tsRes.json();
      setTimeSeries(tsData);
    } catch (err) {
      console.error('Search failed:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') runSearch();
  };

  return (
    <div className="search-bar">
      <input
        type="text"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        onKeyDown={handleKeyDown}
        placeholder="level:ERROR AND service:billing-api"
      />
      <button onClick={runSearch}>Search</button>
    </div>
  );
}

export default SearchBar;