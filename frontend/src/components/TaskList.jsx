import TaskCard from './TaskCard';

function TaskList({ list, onCardClick }) {
  return (
    <div className="list">
      <div className="list-title">{list.title}</div>
      <div className="cards">
        {list.cards.map((card) => (
          <TaskCard key={card.id} card={card} onClick={onCardClick} />
        ))}
      </div>
    </div>
  );
}

export default TaskList;
