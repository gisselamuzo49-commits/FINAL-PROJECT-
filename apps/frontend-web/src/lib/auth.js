// Shared helpers for reading and decoding the JWT stored in localStorage.

// Returns the raw JWT string, or null when the user is not authenticated.
export function getToken() {
  return localStorage.getItem('token');
}

// Decodes the JWT payload. Returns an empty object when the token is missing
// or malformed so callers can safely read fields without guarding.
export function decodeToken(token = getToken()) {
  if (token && token.split('.').length === 3) {
    try {
      return JSON.parse(atob(token.split('.')[1]));
    } catch (e) {
      console.error('JWT decode error:', e);
    }
  }
  return {};
}

// Extracts the user id from a decoded payload, trying the several claim
// names used across the backend services.
export function getUserId(payload = decodeToken()) {
  return payload.id || payload.userId || payload.sub || payload.studentId;
}

// Extracts the normalized (uppercased) role from a decoded payload.
export function getUserRole(payload = decodeToken()) {
  return (payload.rol || payload.role || payload.authorities || payload.authority || '')
    .toString()
    .toUpperCase();
}

// Extracts a display name from a decoded payload, or null when none is present.
export function getUserName(payload = decodeToken()) {
  return (
    payload.nombre ||
    payload.name ||
    payload.firstName ||
    payload.fullName ||
    payload.username ||
    null
  );
}
