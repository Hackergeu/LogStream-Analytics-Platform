import { useState } from 'react';
import SearchBar from './components/SearchBar';
import VolumeChart from './components/VolumeChart';
import ResultsTable from './components/ResultsTable';
import LogGenerator from './components/LogGenerator';
import AlertsPanel from './components/AlertsPanel';
import './App.css';

function App() {
  const [results, setResults] = useState([]);
  const [timeSeries, setTimeSeries] = useState({});
  const [loading, setLoading] = useState(false);

  return (
    <div className="app">
      <header className="topbar">
        <h1>LogStream</h1>
        <SearchBar
          setResults={setResults}
          setTimeSeries={setTimeSeries}
          setLoading={setLoading}
        />
      </header>

      <main className="content">
          <LogGenerator />
          <AlertsPanel />
        <section className="chart-section">
          <h2>Log Volume</h2>
          <VolumeChart timeSeries={timeSeries} />
        </section>

        <section className="results-section">
          <h2>Results {loading && '(loading...)'}</h2>
          <ResultsTable results={results} />
        </section>
      </main>
    </div>
  );
}

export default App;