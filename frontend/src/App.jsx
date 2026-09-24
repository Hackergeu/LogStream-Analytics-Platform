import { useState, useEffect } from 'react';
import SearchBar from './components/SearchBar';
import VolumeChart from './components/VolumeChart';
import ResultsTable from './components/ResultsTable';
import LogGenerator from './components/LogGenerator';
import AlertsPanel from './components/AlertsPanel';
import LiveTail from './components/LiveTail';
import Login from './components/Login';
import { getToken, clearToken, getUsername } from './auth';
import './App.css';

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(!!getToken());
  const [results, setResults] = useState([]);
  const [timeSeries, setTimeSeries] = useState({});
  const [loading, setLoading] = useState(false);

  const handleLogout = () => {
    clearToken();
    setIsLoggedIn(false);
  };

  if (!isLoggedIn) {
    return <Login onLoginSuccess={() => setIsLoggedIn(true)} />;
  }

  return (
    <div className="app-shell">

      <header className="topbar">
        <div className="topbar-brand">
          <span className="logo-dot" />
          <h1>LogStream</h1>
        </div>
        <div className="topbar-user">
          <span className="topbar-status-dot" />
          Connected
          <span>|</span>
          {getUsername()}
          <button className="logout-btn" onClick={handleLogout}>Logout</button>
        </div>
      </header>


      <div className="dashboard-layout">
        <div className="dashboard-main">
          <SearchBar
            setResults={setResults}
            setTimeSeries={setTimeSeries}
            setLoading={setLoading}
          />

          <LogGenerator />

          <section className="chart-section">
            <h2>Log Volume</h2>
            <VolumeChart timeSeries={timeSeries} />
          </section>

          <section className="results-section">
            <h2>Results {loading && <span className="loading-spinner"></span>}</h2>
            <ResultsTable results={results} />
          </section>
        </div>

        <div className="dashboard-side">
          <AlertsPanel />
          <LiveTail />
        </div>
      </div>
    </div>
  );
}

export default App;