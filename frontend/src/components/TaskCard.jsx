function TaskCard({ card, onClick, onDelete }) {
  return (
    <div className="card" onClick={() => onClick(card)}>
      <button
        type="button"
        className="card-delete"
        aria-label="カードを削除"
        onClick={(event) => {
          event.stopPropagation();
          onDelete(card);
        }}
      >
        ✕
      </button>
      <div className="card-text">{card.text}</div>
      <div className="card-meta">
        <span className={`priority priority-${card.priority}`}>{card.priority}</span>
        <span className="due-date">{card.dueDate ?? '期限日未設定'}</span>
      </div>
    </div>
  );
}

export default TaskCard;
