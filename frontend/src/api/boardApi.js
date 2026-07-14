export async function fetchLists(userId) {
  const response = await fetch(`/api/users/${userId}/lists`);
  if (!response.ok) {
    throw new Error(`リストの取得に失敗しました (status: ${response.status})`);
  }
  return response.json();
}

export async function updateCard(cardId, { text, priority, dueDate, listId }) {
  const response = await fetch(`/api/cards/${cardId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ text, priority, dueDate, listId }),
  });
  if (!response.ok) {
    throw new Error(`カードの更新に失敗しました (status: ${response.status})`);
  }
  return response.json();
}

export async function deleteCard(cardId) {
  const response = await fetch(`/api/cards/${cardId}`, { method: 'DELETE' });
  if (!response.ok) {
    throw new Error(`カードの削除に失敗しました (status: ${response.status})`);
  }
}
