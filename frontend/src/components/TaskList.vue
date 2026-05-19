<script setup>
defineProps({
  tasks: { type: Array, default: () => [] },
})
defineEmits(['edit', 'delete'])

const statusText = {
  TODO: '待辦',
  IN_PROGRESS: '進行中',
  DONE: '已完成',
}
</script>

<template>
  <p v-if="!tasks.length" class="empty">目前沒有任務</p>
  <ul v-else class="task-list">
    <li
      v-for="task in tasks"
      :key="task.id"
      :class="['task', task.status.toLowerCase()]"
    >
      <div class="task-main">
        <h3>{{ task.title }}</h3>
        <p v-if="task.description" class="desc">{{ task.description }}</p>
        <div class="meta">
          <span class="status">{{ statusText[task.status] }}</span>
          <span v-if="task.dueDate">截止 {{ task.dueDate }}</span>
        </div>
      </div>
      <div class="task-actions">
        <button @click="$emit('edit', task)">編輯</button>
        <button class="danger" @click="$emit('delete', task.id)">刪除</button>
      </div>
    </li>
  </ul>
</template>
