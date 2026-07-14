import { useState } from 'react';
import { updateCard } from '../api/boardApi';

const PRIORITIES = ['高', '中', '低'];

function CardDetailModal({ card, lists, onClose, onSaved }) {
  const [text, setText] = useState(card.text);
  const [priority, setPriority] = useState(card.priority);
  const [dueDate, setDueDate] = useState(card.dueDate ?? '');
  const [listId, setListId] = useState(card.listId);
  const [saving, setSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const handleSave = async () => {
    setSaving(true);
    setErrorMessage('');
    try {
      const updated = await updateCard(card.id, {
        text,
        priority,
        dueDate: dueDate || null,
        listId: Number(listId),
      });
      onSaved(updated);
    } catch (error) {
      setErrorMessage(error.message);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(event) => event.stopPropagation()}>
        <h2 className="modal-title">カード詳細</h2>

        <label className="modal-field">
          内容
          <textarea value={text} onChange={(event) => setText(event.target.value)} rows={4} />
        </label>

        <label className="modal-field">
          優先度
          <select value={priority} onChange={(event) => setPriority(event.target.value)}>
            {PRIORITIES.map((p) => (
              <option key={p} value={p}>{p}</option>
            ))}
          </select>
        </label>

        <label className="modal-field">
          期限日
          <input
            type="date"
            value={dueDate ?? ''}
            onChange={(event) => setDueDate(event.target.value)}
          />
        </label>

        <label className="modal-field">
          ステータス(所属リスト)
          <select value={listId} onChange={(event) => setListId(event.target.value)}>
            {lists.map((list) => (
              <option key={list.id} value={list.id}>{list.title}</option>
            ))}
          </select>
        </label>

        {errorMessage && <p className="status-message status-error">{errorMessage}</p>}

        <div className="modal-actions">
          <button type="button" onClick={onClose} disabled={saving}>キャンセル</button>
          <button type="button" onClick={handleSave} disabled={saving}>
            {saving ? '保存中...' : '保存'}
          </button>
        </div>
      </div>
    </div>
  );
}

export default CardDetailModal;
