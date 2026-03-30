import React from 'react';

function ItemList({ items, onEdit, onDelete }) {
  if (items.length === 0) {
    return <div className="empty-state">No items found. Create your first item!</div>;
  }

  return (
    <div className="item-list">
      {items.map(item => (
        <div key={item.id} className="item-card">
          <div className="item-info">
            <h3>{item.name}</h3>
            <p>{item.description}</p>
            <div className="item-meta">
              <span>Category: {item.category || 'N/A'}</span>
              <span>Price: ${item.price?.toFixed(2) || '0.00'}</span>
              <span>Qty: {item.quantity || 0}</span>
            </div>
          </div>
          <div className="item-actions">
            <button className="btn-warning" onClick={() => onEdit(item)}>Edit</button>
            <button className="btn-danger" onClick={() => onDelete(item)}>Delete</button>
          </div>
        </div>
      ))}
    </div>
  );
}

export default ItemList;
