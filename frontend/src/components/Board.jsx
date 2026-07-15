import { useEffect, useState } from 'react';
import { fetchLists, deleteCard } from '../api/boardApi';
import TaskList from './TaskList';
import CardDetailModal from './CardDetailModal';

// ログインAPI未実装のため、暫定的にテストユーザーのIDを固定で使用する。
// ログイン機能実装後は認証済みユーザーのIDに置き換える。
const TEMP_USER_ID = 2;

function Board() {
  const [lists, setLists] = useState([]);
  const [status, setStatus] = useState('loading');
  const [errorMessage, setErrorMessage] = useState('');
  const [selectedCard, setSelectedCard] = useState(null);

  const loadLists = () => {
    return fetchLists(TEMP_USER_ID)
      .then((data) => {
        setLists(data);
        setStatus('success');
      })
      .catch((error) => {
        setErrorMessage(error.message);
        setStatus('error');
      });
  };

  useEffect(() => {
    loadLists();
  }, []);

  const handleCardDelete = (card) => {
    deleteCard(card.id, TEMP_USER_ID)
      .then(loadLists)
      .catch((error) => window.alert(error.message));
  };

  if (status === 'loading') {
    return <p className="status-message">読み込み中...</p>;
  }

  if (status === 'error') {
    return <p className="status-message status-error">{errorMessage}</p>;
  }

  return (
    <div id="board">
      {lists.map((list) => (
        <TaskList
          key={list.id}
          list={list}
          onCardClick={setSelectedCard}
          onCardDelete={handleCardDelete}
        />
      ))}
      {selectedCard && (
        <CardDetailModal
          card={selectedCard}
          lists={lists}
          userId={TEMP_USER_ID}
          onClose={() => setSelectedCard(null)}
          onSaved={() => {
            setSelectedCard(null);
            loadLists();
          }}
        />
      )}
    </div>
  );
}

export default Board;
