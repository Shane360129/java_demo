<script setup>
import { ref, computed, onMounted } from 'vue'
import TaskForm from './components/TaskForm.vue'
import TaskList from './components/TaskList.vue'
import TaskFilters from './components/TaskFilters.vue'
import Toast from './components/Toast.vue'
import {
  listTasks,
  createTask,
  updateTask,
  updateTaskStatus,
  deleteTask,
  deleteCompleted,
} from './api/task.js'

const tasks = ref([])
const editing = ref(null)
const filter = ref('all')
const search = ref('')
const sortBy = ref('created')

const toast = ref({ message: '', type: 'info', visible: false })
let toastTimer

function showToast(message, type = 'info') {
  toast.value = { message, type, visible: true }
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => {
    toast.value = { ...toast.value, visible: false }
  }, 2500)
}

const today = computed(() => new Date().toISOString().slice(0, 10))

const stats = computed(() => {
  const total = tasks.value.length
  const done = tasks.value.filter((t) => t.status === 'DONE').length
  const inProgress = tasks.value.filter((t) => t.status === 'IN_PROGRESS').length
  const active = total - done
  const overdue = tasks.value.filter(
    (t) => t.dueDate && t.status !== 'DONE' && t.dueDate < today.value,
  ).length
  return { total, done, inProgress, active, overdue }
})

const filteredTasks = computed(() => {
  let list = tasks.value
  if (filter.value === 'active') list = list.filter((t) => t.status !== 'DONE')
  else if (filter.value === 'done') list = list.filter((t) => t.status === 'DONE')

  const q = search.value.trim().toLowerCase()
  if (q) {
    list = list.filter(
      (t) =>
        t.title.toLowerCase().includes(q) ||
        (t.description && t.description.toLowerCase().includes(q)),
    )
  }

  const sorted = [...list]
  if (sortBy.value === 'dueDate') {
    sorted.sort((a, b) => {
      if (!a.dueDate && !b.dueDate) return 0
      if (!a.dueDate) return 1
      if (!b.dueDate) return -1
      return a.dueDate.localeCompare(b.dueDate)
    })
  } else if (sortBy.value === 'priority') {
    const order = { HIGH: 0, MEDIUM: 1, LOW: 2 }
    sorted.sort((a, b) => order[a.priority] - order[b.priority])
  } else {
    sorted.sort((a, b) => (b.createdAt || '').localeCompare(a.createdAt || ''))
  }
  return sorted
})

async function refresh() {
  try {
    tasks.value = await listTasks()
  } catch (e) {
    console.error(e)
    showToast('無法載入任務，請確認後端是否啟動', 'error')
  }
}

async function handleSubmit(payload) {
  try {
    if (editing.value) {
      await updateTask(editing.value.id, payload)
      editing.value = null
      showToast('已更新任務', 'success')
    } else {
      await createTask(payload)
      showToast('已新增任務', 'success')
    }
    await refresh()
  } catch (e) {
    console.error(e)
    showToast('儲存失敗', 'error')
  }
}

async function handleDelete(id) {
  if (!confirm('確定要刪除這筆任務？此動作無法復原。')) return
  try {
    await deleteTask(id)
    if (editing.value?.id === id) editing.value = null
    tasks.value = tasks.value.filter((t) => t.id !== id)
    showToast('已刪除', 'success')
  } catch (e) {
    console.error(e)
    showToast('刪除失敗', 'error')
    await refresh()
  }
}

const STATUS_NEXT = { TODO: 'IN_PROGRESS', IN_PROGRESS: 'DONE', DONE: 'TODO' }
const STATUS_LABEL = { TODO: '待辦', IN_PROGRESS: '進行中', DONE: '已完成' }

async function handleCycleStatus(task) {
  const next = STATUS_NEXT[task.status]
  try {
    const updated = await updateTaskStatus(task.id, next)
    const idx = tasks.value.findIndex((t) => t.id === task.id)
    if (idx !== -1) tasks.value[idx] = updated
    showToast(`已標記為「${STATUS_LABEL[next]}」`, 'success')
  } catch (e) {
    console.error(e)
    showToast('狀態更新失敗', 'error')
  }
}

async function handleClearCompleted() {
  if (!stats.value.done) return
  if (!confirm(`確定要清除全部 ${stats.value.done} 筆已完成任務？`)) return
  try {
    const { removed } = await deleteCompleted()
    showToast(`已清除 ${removed} 筆`, 'success')
    await refresh()
  } catch (e) {
    console.error(e)
    showToast('清除失敗', 'error')
  }
}

function startEdit(task) {
  editing.value = { ...task }
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function cancelEdit() {
  editing.value = null
}

onMounted(refresh)
</script>

<template>
  <header>
    <div class="header-inner">
      <h1>📝 任務管理 Demo</h1>
      <p class="subtitle">Spring Boot 3 + Vue 3 · 點擊圓圈切換狀態</p>
      <div class="stats-bar">
        <span class="stat">總數 <strong>{{ stats.total }}</strong></span>
        <span class="stat">待處理 <strong>{{ stats.active }}</strong></span>
        <span class="stat in-progress">進行中 <strong>{{ stats.inProgress }}</strong></span>
        <span class="stat done">已完成 <strong>{{ stats.done }}</strong></span>
        <span v-if="stats.overdue" class="stat overdue">逾期 <strong>{{ stats.overdue }}</strong></span>
      </div>
    </div>
  </header>
  <main>
    <section class="card">
      <h2>{{ editing ? '編輯任務' : '新增任務' }}</h2>
      <TaskForm :initial="editing" @submit="handleSubmit" @cancel="cancelEdit" />
    </section>
    <section class="card">
      <TaskFilters
        v-model:filter="filter"
        v-model:search="search"
        v-model:sortBy="sortBy"
        :stats="stats"
        @clear-completed="handleClearCompleted"
      />
      <TaskList
        :tasks="filteredTasks"
        @edit="startEdit"
        @delete="handleDelete"
        @cycle-status="handleCycleStatus"
      />
    </section>
  </main>
  <Toast :message="toast.message" :type="toast.type" :visible="toast.visible" />
</template>
