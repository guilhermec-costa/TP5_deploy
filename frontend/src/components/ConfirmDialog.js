import React from 'react';
import ItemModal from './ItemModal';

function ConfirmDialog({ title, message, onConfirm, onCancel }) {
  return (
    <ItemModal title={title} onClose={onCancel}>
      <p>{message}</p>
      <div className="modal-actions">
        <button className="btn-danger" onClick={onConfirm}>
          Confirm
        </button>
        <button className="btn-secondary" onClick={onCancel}>
          Cancel
        </button>
      </div>
    </ItemModal>
  );
}

export default ConfirmDialog;
