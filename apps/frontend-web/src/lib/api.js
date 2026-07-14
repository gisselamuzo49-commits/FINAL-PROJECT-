// Base URL for the API gateway. Falls back to the current host when the
// VITE_API_BASE_URL environment variable is not defined (e.g. local/dev).
export const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || `http://${window.location.hostname}`;
