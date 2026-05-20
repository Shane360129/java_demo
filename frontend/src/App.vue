<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch, useTemplateRef } from 'vue'
import TaskForm from './components/TaskForm.vue'
import TaskList from './components/TaskList.vue'
import TaskFilters from './components/TaskFilters.vue'
import Toast from './components/Toast.vue'
import ConfirmDialog from './components/ConfirmDialog.vue'
import HelpDialog from './components/HelpDialog.vue'
import {
  listTasks,
  createTask,
  updateTask,
  updateTaskStatus,
  deleteTask,
  deleteCompleted,
} from './api/task.js'

/* ---------- Theme ---------- */
const prefersDark = window.matchMedia('(prefers-color-scheme: dark)')
const theme = ref(localStorage.getItem('theme') || (prefersDark.matches ? 'dark' : 'light'))
watch(
  theme,
  (t) => {
    document.documentElement.dataset.theme = t
    localStorage.setItem('theme', t)
  },
  { immediate: true },
)
function toggleTheme() {
  theme.value = theme.value === 'dark' ? 'light' : 'dark'
}

/* ---------- Persisted UI state ---------- */
const filter = ref(localStorage.getItem('filter') || 'all')
const sortBy = ref(localStorage.getItem('sortBy') || 'created')
const search = ref('')
watch(filter, (v) => localStorage.setItem('filter', v))
watch(sortBy, (v) => localStorage.setItem('sortBy', v))

/* ---------- App state ---------- */
const tasks = ref([])
const editing = ref(null)
const loading = ref(true)

const taskFormRef = useTemplateRef('taskFormRef')
const taskFiltersRef = useTemplateRef('taskFiltersRef')

/* ---------- Toast (with optional action button) ---------- */
const toast = ref({ message: '', type: 'info', visible: false, action: '' })
let toastActionCb = null
let toastTimer

function showToast(message, type = 'info', options = {}) {
  toast.value = {
    message,
    type,
    visible: true,
    action: options.action || '',
  }
  toastActionCb = options.onAction || null
  clearTimeout(toastTimer)
  toastTimer = setTimeout(
    () => {
      toast.value = { ...toast.value, visible: false }
    },
    options.duration ?? (options.action ? 6000 : 2500),
  )
}

function dismissToast() {
  clearTimeout(toastTimer)
  toast.value = { ...toast.value, visible: false }
  toastActionCb = null
}

function handleToastAction() {
  const cb = toastActionCb
  dismissToast()
  if (cb) cb()
}

/* ---------- Confirm dialog ---------- */
const confirmState = ref({
  visible: false,
  title: '',
  message: '',
  confirmLabel: '確定',
  danger: false,
})
let confirmResolver = null

function askConfirm(opts) {
  // Auto-cancel any previously pending confirm so we never orphan a promise.
  if (confirmResolver) {
    const stale = confirmResolver
    confirmResolver = null
    stale(false)
  }
  return new Promise((resolve) => {
    confirmState.value = {
      visible: true,
      title: opts.title || '確認',
      message: opts.message || '',
      confirmLabel: opts.confirmLabel || '確定',
      danger: !!opts.danger,
    }
    confirmResolver = resolve
  })
}

function closeConfirm(result) {
  // Capture-and-null before resolving in case the resolver re-enters.
  const resolver = confirmResolver
  confirmResolver = null
  confirmState.value = { ...confirmState.value, visible: false }
  if (resolver) resolver(result)
}

/* ---------- Help dialog ---------- */
const helpVisible = ref(false)

/* ---------- Today & stats ---------- */
const today = computed(() => new Date().toISOString().slice(0, 10))

const stats = computed(() => {
  const total = tasks.value.length
  const done = tasks.value.filter((t) => t.status === 'DONE').length
  const inProgress = tasks.value.filter((t) => t.status === 'IN_PROGRESS').length
  const active = total - done
  const overdue = tasks.value.filter(
    (t) => t.dueDate && t.status !== 'DONE' && t.dueDate < today.value,
  ).length
  const progress = total ? Math.round((done / total) * 100) : 0
  return { total, done, inProgress, active, overdue, progress }
})

/* document.title 顯示待辦數 */
watch(
  () => stats.value.active,
  (n) => {
    document.title = n > 0 ? `(${n}) 任務管理 Demo` : '任務管理 Demo'
  },
)

/* ---------- Filtering / sorting ---------- */
const isFilterActive = computed(
  () => filter.value !== 'all' || search.value.trim().length > 0,
)

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

/* ---------- Actions ---------- */
async function refresh() {
  loading.value = true
  try {
    tasks.value = await listTasks()
  } catch (e) {
    console.error(e)
    showToast('無法載入任務，請確認後端是否啟動', 'error')
  } finally {
    loading.value = false
  }
}

async function handleSubmit(payload) {
  try {
    if (editing.value) {
      const updated = await updateTask(editing.value.id, payload)
      const idx = tasks.value.findIndex((t) => t.id === editing.value.id)
      if (idx !== -1) tasks.value[idx] = updated
      editing.value = null
      showToast('已更新任務', 'success')
    } else {
      const created = await createTask(payload)
      tasks.value.unshift(created)
      showToast('已新增任務', 'success')
    }
  } catch (e) {
    console.error(e)
    showToast('儲存失敗', 'error')
    await refresh()
  }
}

async function handleDelete(id) {
  const task = tasks.value.find((t) => t.id === id)
  if (!task) return
  const ok = await askConfirm({
    title: '刪除任務',
    message: `確定要刪除「${task.title}」？`,
    confirmLabel: '刪除',
    danger: true,
  })
  if (!ok) return
  try {
    await deleteTask(id)
    if (editing.value?.id === id) editing.value = null
    tasks.value = tasks.value.filter((t) => t.id !== id)
    showToast(`已刪除「${task.title}」`, 'success', {
      action: '↶ 復原',
      onAction: async () => {
        try {
          const restored = await createTask({
            title: task.title,
            description: task.description,
            status: task.status,
            priority: task.priority,
            dueDate: task.dueDate,
          })
          tasks.value.unshift(restored)
          showToast('已復原', 'success')
        } catch (e) {
          console.error(e)
          showToast('復原失敗', 'error')
        }
      },
    })
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
  const removed = tasks.value.filter((t) => t.status === 'DONE')
  if (!removed.length) return
  try {
    await deleteCompleted()
    tasks.value = tasks.value.filter((t) => t.status !== 'DONE')
    showToast(`已清除 ${removed.length} 筆已完成`, 'success', {
      action: '↶ 復原',
      onAction: async () => {
        try {
          const recreated = await Promise.all(
            removed.map((t) =>
              createTask({
                title: t.title,
                description: t.description,
                status: t.status,
                priority: t.priority,
                dueDate: t.dueDate,
              }),
            ),
          )
          tasks.value.unshift(...recreated)
          showToast(`已復原 ${recreated.length} 筆`, 'success')
        } catch (e) {
          console.error(e)
          showToast('復原失敗', 'error')
          await refresh()
        }
      },
    })
  } catch (e) {
    console.error(e)
    showToast('清除失敗', 'error')
    await refresh()
  }
}

function startEdit(task) {
  editing.value = { ...task }
  taskFormRef.value?.focusTitle()
}

function cancelEdit() {
  editing.value = null
}

function resetFilter() {
  filter.value = 'all'
  search.value = ''
}

function focusForm() {
  editing.value = null
  taskFormRef.value?.focusTitle()
}

/* ---------- Keyboard shortcuts ---------- */
function isTypingTarget(el) {
  if (!el) return false
  const t = el.tagName
  return t === 'INPUT' || t === 'TEXTAREA' || el.isContentEditable
}

function onGlobalKey(e) {
  // Esc works even while typing — to blur/cancel
  if (e.key === 'Escape') {
    if (helpVisible.value) {
      helpVisible.value = false
      return
    }
    if (confirmState.value.visible) {
      closeConfirm(false)
      return
    }
    if (isTypingTarget(e.target)) {
      if (e.target.tagName !== 'TEXTAREA') e.target.blur()
      return
    }
    if (editing.value) cancelEdit()
    else if (search.value) search.value = ''
    return
  }

  if (isTypingTarget(e.target)) return
  if (e.ctrlKey || e.metaKey || e.altKey) return

  switch (e.key) {
    case 'n':
    case 'N':
      e.preventDefault()
      focusForm()
      break
    case '/':
      e.preventDefault()
      taskFiltersRef.value?.focusSearch()
      break
    case '1':
      filter.value = 'all'
      break
    case '2':
      filter.value = 'active'
      break
    case '3':
      filter.value = 'done'
      break
    case 't':
    case 'T':
      toggleTheme()
      break
    case '?':
      helpVisible.value = !helpVisible.value
      break
  }
}

onMounted(() => {
  window.addEventListener('keydown', onGlobalKey)
  refresh()
})
onBeforeUnmount(() => window.removeEventListener('keydown', onGlobalKey))
</script>

<template>
  <header>
    <div class="header-inner">
      <div class="header-top">
        <h1>📝 任務管理 Demo</h1>
        <div class="header-tools">
          <button
            class="icon-tool"
            type="button"
            :title="`切換${theme === 'dark' ? '淺色' : '深色'}模式（T）`"
            :aria-label="`切換${theme === 'dark' ? '淺色' : '深色'}模式`"
            @click="toggleTheme"
          >
            {{ theme === 'dark' ? '☀️' : '🌙' }}
          </button>
          <button
            class="icon-tool"
            type="button"
            title="鍵盤快捷鍵（?）"
            aria-label="鍵盤快捷鍵說明"
            @click="helpVisible = true"
          >
            ⌨️
          </button>
        </div>
      </div>
      <p class="subtitle">Spring Boot 3 + Vue 3 · 按 <kbd>?</kbd> 看快捷鍵</p>

      <div class="progress-wrap" v-if="stats.total > 0">
        <div class="progress-track">
          <div class="progress-fill" :style="{ width: stats.progress + '%' }"></div>
        </div>
        <span class="progress-label">完成 {{ stats.progress }}%</span>
      </div>

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
      <TaskForm
        ref="taskFormRef"
        :initial="editing"
        @submit="handleSubmit"
        @cancel="cancelEdit"
      />
    </section>
    <section class="card">
      <TaskFilters
        ref="taskFiltersRef"
        v-model:filter="filter"
        v-model:search="search"
        v-model:sortBy="sortBy"
        :stats="stats"
        @clear-completed="handleClearCompleted"
      />
      <TaskList
        :tasks="filteredTasks"
        :loading="loading"
        :filter-active="isFilterActive"
        @edit="startEdit"
        @delete="handleDelete"
        @cycle-status="handleCycleStatus"
        @reset-filter="resetFilter"
        @add-first="focusForm"
      />
    </section>
  </main>

  <Toast
    :message="toast.message"
    :type="toast.type"
    :visible="toast.visible"
    :action="toast.action"
    @action="handleToastAction"
  />
  <ConfirmDialog
    :visible="confirmState.visible"
    :title="confirmState.title"
    :message="confirmState.message"
    :confirm-label="confirmState.confirmLabel"
    :danger="confirmState.danger"
    @confirm="closeConfirm(true)"
    @cancel="closeConfirm(false)"
  />
  <HelpDialog :visible="helpVisible" @close="helpVisible = false" />
</template>
