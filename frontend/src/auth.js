const TOKEN_KEY = 'logstream_token';
const USERNAME_KEY = 'username';

export function saveToken(token) {
  sessionStorage.setItem(TOKEN_KEY, token);
}

export function getToken() {
  return sessionStorage.getItem(TOKEN_KEY);
}

export function clearToken() {
  sessionStorage.removeItem(TOKEN_KEY);
  sessionStorage.removeItem(USERNAME_KEY);
}

export function saveUsername(username) {
  sessionStorage.setItem(USERNAME_KEY, username);
}

export function getUsername() {
  return sessionStorage.getItem(USERNAME_KEY) || 'User';
}

export function authHeaders() {
  const token = getToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
}