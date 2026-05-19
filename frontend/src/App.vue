<script setup>
import { ref, onMounted } from 'vue'
import TaskForm from './components/TaskForm.vue'
import TaskList from './components/TaskList.vue'
import { listTasks, createTask, updateTask, deleteTask } from './api/task.js'

const tasks = ref([])
const editing = ref(null)
const error = ref('')

async function refresh() {
  try {
    tasks.value = await listTasks()
    error.value = ''
  } catch (e) {
    error.value = '無法載入任務，請確認後端是否啟動'
  }
}

async function handleSubmit(payload) {
  try {
    if (editing.value) {
      await updateTask(editing.value.id, payload)
      editing.value = null
    } else {
      await createTask(payload)
    }
    await refresh()
  } catch (e) {
    error.value = '儲存失敗'
  }
}

async function handleDelete(id) {
  if (!confirm('確定要刪除這筆任務？')) return
  await deleteTask(id)
  await refresh()
}

function startEdit(task) {
  editing.value = { ...task }
}

function cancelEdit() {
  editing.value = null
}

onMounted(refresh)
</script>

<template>
  <header>
    <h1>任務管理 Demo</h1>
    <p class="subtitle">Spring Boot 3 + Vue 3 · MVC CRUD</p>
  </header>
  <main>
    <section class="card">
      <h2>{{ editing ? '編輯任務' : '新增任務' }}</h2>
      <TaskForm :initial="editing" @submit="handleSubmit" @cancel="cancelEdit" />
    </section>
    <section class="card">
      <h2>任務列表（{{ tasks.length }}）</h2>
      <p v-if="error" class="error">{{ error }}</p>
      <TaskList :tasks="tasks" @edit="startEdit" @delete="handleDelete" />
    </section>
  </main>
</template>
