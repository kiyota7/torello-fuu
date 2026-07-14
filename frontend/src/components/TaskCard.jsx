function TaskCard({ card, onClick }) {
  return (
    <div className="card" onClick={() => onClick(card)}>
      <div className="card-text">{card.text}</div>
      <div className="card-meta">
        <span className={`priority priority-${card.priority}`}>{card.priority}</span>
        <span className="due-date">{card.dueDate ?? '期限日未設定'}</span>
      </div>
    </div>
  );
}

export default TaskCard;
