export async function fetchLists(userId) {
  const response = await fetch(`/api/users/${userId}/lists`);
  if (!response.ok) {
    throw new Error(`リストの取得に失敗しました (status: ${response.status})`);
  }
  return response.json();
}
