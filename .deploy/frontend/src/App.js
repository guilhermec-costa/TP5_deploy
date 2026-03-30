import React, { useState } from 'react';
import ItemList from './components/ItemList';
import ItemForm from './components/ItemForm';
import ItemModal from './components/ItemModal';
import ConfirmDialog from './components/ConfirmDialog';
import { useItems } from './hooks';
import './App.css';

function App() {
  const { items, loading, error, createItem, updateItem, deleteItem, clearError } = useItems();
  const [showForm, setShowForm] = useState(false);
  const [editingItem, setEditingItem] = useState(null);
  const [deleteConfirm, setDeleteConfirm] = useState(null);

  const handleCreate = async (itemData) => {
    await createItem(itemData);
    setShowForm(false);
  };

  const handleUpdate = async (itemData) => {
    await updateItem(editingItem.id, itemData);
    setEditingItem(null);
  };

  const handleDelete = async () => {
    await deleteItem(deleteConfirm.id);
    setDeleteConfirm(null);
  };

  return (
    <div className="app">
      <header className="header">
        <h1>CRUD System</h1>
        <button className="btn-primary" onClick={() => setShowForm(true)}>
          + New Item
        </button>
      </header>

      {error && (
        <div className="error-banner">
          <span>Error: {error}</span>
          <button onClick={clearError}>×</button>
        </div>
      )}

      {loading ? (
        <div className="loading">Loading...</div>
      ) : (
        <ItemList
          items={items}
          onEdit={setEditingItem}
          onDelete={setDeleteConfirm}
        />
      )}

      {showForm && (
        <ItemModal title="Create Item" onClose={() => setShowForm(false)}>
          <ItemForm onSubmit={handleCreate} onCancel={() => setShowForm(false)} />
        </ItemModal>
      )}

      {editingItem && (
        <ItemModal title="Edit Item" onClose={() => setEditingItem(null)}>
          <ItemForm
            item={editingItem}
            onSubmit={handleUpdate}
            onCancel={() => setEditingItem(null)}
          />
        </ItemModal>
      )}

      {deleteConfirm && (
        <ConfirmDialog
          title="Confirm Delete"
          message={`Are you sure you want to delete "${deleteConfirm.name}"?`}
          onConfirm={handleDelete}
          onCancel={() => setDeleteConfirm(null)}
        />
      )}
    </div>
  );
}

export default App;
