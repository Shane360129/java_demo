<script setup>
defineProps({
  tasks: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  filterActive: { type: Boolean, default: false },
})
defineEmits(['edit', 'delete', 'cycle-status', 'reset-filter', 'add-first'])

const statusText = {
  TODO: '待辦',
  IN_PROGRESS: '進行中',
  DONE: '已完成',
}

const priorityText = {
  HIGH: '高優先',
  MEDIUM: '一般',
  LOW: '低優先',
}

const today = new Date().toISOString().slice(0, 10)
const tomorrow = new Date(Date.now() + 86400000).toISOString().slice(0, 10)

function isOverdue(task) {
  return task.dueDate && task.status !== 'DONE' && task.dueDate < today
}

function isToday(task) {
  return task.dueDate === today && task.status !== 'DONE'
}

function formatDueDate(date) {
  if (date === today) return '今天'
  if (date === tomorrow) return '明天'
  return date.slice(5).replace('-', '/')
}
</script>

<template>
  <div v-if="loading" class="skeleton-list" aria-label="載入中">
    <div v-for="n in 3" :key="n" class="skeleton-task">
      <span class="skeleton-circle"></span>
      <span class="skeleton-lines">
        <span class="skeleton-line w-70"></span>
        <span class="skeleton-line w-40"></span>
      </span>
    </div>
  </div>
  <div v-else-if="!tasks.length" class="empty">
    <span class="empty-icon" aria-hidden="true">{{ filterActive ? '🔎' : '🎉' }}</span>
    <span>{{ filterActive ? '沒有符合條件的任務' : '一筆任務都沒有，開始新增吧！' }}</span>
    <button
      type="button"
      class="empty-cta"
      @click="filterActive ? $emit('reset-filter') : $emit('add-first')"
    >
      {{ filterActive ? '清除篩選與搜尋' : '新增第一筆任務' }}
    </button>
  </div>
  <TransitionGroup v-else name="task" tag="ul" class="task-list">
    <li
      v-for="task in tasks"
      :key="task.id"
      :class="['task', task.status.toLowerCase(), { 'is-overdue': isOverdue(task) }]"
    >
      <button
        :class="['status-btn', task.status.toLowerCase()]"
        type="button"
        :title="`點擊切換狀態（目前：${statusText[task.status]}）`"
        :aria-label="`切換狀態，目前 ${statusText[task.status]}`"
        @click="$emit('cycle-status', task)"
      >
        <svg v-if="task.status === 'TODO'" viewBox="0 0 24 24" width="18" height="18">
          <circle cx="12" cy="12" r="9" fill="none" stroke="currentColor" stroke-width="2"/>
        </svg>
        <svg v-else-if="task.status === 'IN_PROGRESS'" viewBox="0 0 24 24" width="18" height="18">
          <circle cx="12" cy="12" r="9" fill="none" stroke="currentColor" stroke-width="2"/>
          <path d="M12 3 A9 9 0 0 1 12 21 Z" fill="currentColor"/>
        </svg>
        <svg v-else viewBox="0 0 24 24" width="18" height="18">
          <circle cx="12" cy="12" r="9" fill="currentColor"/>
          <path d="M7 12 l4 4 l6 -7" fill="none" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>

      <div class="task-main" @click="$emit('edit', task)">
        <div class="task-header">
          <span
            :class="['priority-dot', task.priority.toLowerCase()]"
            :title="priorityText[task.priority]"
          ></span>
          <h3>{{ task.title }}</h3>
        </div>
        <p v-if="task.description" class="desc">{{ task.description }}</p>
        <div class="meta">
          <span :class="['status-pill', task.status.toLowerCase()]">{{ statusText[task.status] }}</span>
          <span
            v-if="task.dueDate"
            :class="['due', { overdue: isOverdue(task), today: isToday(task) }]"
          >
            <span aria-hidden="true">📅</span>
            {{ formatDueDate(task.dueDate) }}
            <span v-if="isOverdue(task)">（已逾期）</span>
            <span v-else-if="isToday(task)">（今天）</span>
          </span>
        </div>
      </div>

      <div class="task-actions">
        <button
          type="button"
          class="icon-btn"
          title="編輯"
          aria-label="編輯"
          @click.stop="$emit('edit', task)"
        >
          ✏️
        </button>
        <button
          type="button"
          class="icon-btn danger"
          title="刪除"
          aria-label="刪除"
          @click.stop="$emit('delete', task.id)"
        >
          🗑
        </button>
      </div>
    </li>
  </TransitionGroup>
</template>
