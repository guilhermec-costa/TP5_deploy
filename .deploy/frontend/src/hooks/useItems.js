import { useState, useEffect, useCallback } from 'react';
import { useApi } from './useApi';
import { API_URL } from '../config/api';

export function useItems() {
  const [items, setItems] = useState([]);
  const { loading, error, get, post, put, del, clearError } = useApi();

  const fetchItems = useCallback(async () => {
    try {
      const data = await get(API_URL);
      setItems(data);
    } catch {
    }
  }, [get]);

  useEffect(() => {
    fetchItems();
  }, [fetchItems]);

  const createItem = useCallback(async (itemData) => {
    await post(API_URL, itemData);
    await fetchItems();
  }, [post, fetchItems]);

  const updateItem = useCallback(async (id, itemData) => {
    await put(`${API_URL}/${id}`, itemData);
    await fetchItems();
  }, [put, fetchItems]);

  const deleteItem = useCallback(async (id) => {
    await del(`${API_URL}/${id}`);
    await fetchItems();
  }, [del, fetchItems]);

  return {
    items,
    loading,
    error,
    fetchItems,
    createItem,
    updateItem,
    deleteItem,
    clearError
  };
}
