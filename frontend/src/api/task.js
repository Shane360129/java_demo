import axios from 'axios'

const api = axios.create({ baseURL: '/api' })

export async function listTasks() {
  const { data } = await api.get('/tasks')
  return data
}

export async function createTask(payload) {
  const { data } = await api.post('/tasks', payload)
  return data
}

export async function updateTask(id, payload) {
  const { data } = await api.put(`/tasks/${id}`, payload)
  return data
}

export async function updateTaskStatus(id, status) {
  const { data } = await api.patch(`/tasks/${id}/status`, { status })
  return data
}

export async function deleteTask(id) {
  await api.delete(`/tasks/${id}`)
}

export async function deleteCompleted() {
  const { data } = await api.delete('/tasks/completed')
  return data
}
