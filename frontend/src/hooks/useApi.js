import { useState, useCallback } from 'react';

async function handleResponse(response) {
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.error || `HTTP error ${response.status}`);
  }
  return response.json();
}

export function useApi() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const request = useCallback(async (url, options = {}) => {
    setLoading(true);
    setError(null);

    try {
      const response = await fetch(url, {
        headers: {
          'Content-Type': 'application/json',
          ...options.headers
        },
        ...options
      });
      return await handleResponse(response);
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const get = useCallback((url) => request(url), [request]);

  const post = useCallback((url, data) => request(url, {
    method: 'POST',
    body: JSON.stringify(data)
  }), [request]);

  const put = useCallback((url, data) => request(url, {
    method: 'PUT',
    body: JSON.stringify(data)
  }), [request]);

  const del = useCallback((url) => request(url, {
    method: 'DELETE'
  }), [request]);

  const clearError = useCallback(() => setError(null), []);

  return { loading, error, get, post, put, del, clearError };
}
