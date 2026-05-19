<script setup>
const props = defineProps({
  filter: { type: String, required: true },
  search: { type: String, required: true },
  sortBy: { type: String, required: true },
  stats: { type: Object, required: true },
})
const emit = defineEmits([
  'update:filter',
  'update:search',
  'update:sortBy',
  'clear-completed',
])

const tabs = [
  { value: 'all', label: '全部', key: 'total' },
  { value: 'active', label: '待處理', key: 'active' },
  { value: 'done', label: '已完成', key: 'done' },
]
</script>

<template>
  <div class="filters">
    <div class="tabs" role="tablist">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        type="button"
        role="tab"
        :aria-selected="filter === tab.value"
        :class="['tab', { active: filter === tab.value }]"
        @click="emit('update:filter', tab.value)"
      >
        {{ tab.label }}
        <span class="tab-badge">{{ stats[tab.key] }}</span>
      </button>
    </div>
    <div class="filter-row">
      <div class="search-wrap">
        <span class="search-icon" aria-hidden="true">🔍</span>
        <input
          type="search"
          placeholder="搜尋標題或描述…"
          :value="search"
          class="search-input"
          @input="emit('update:search', $event.target.value)"
        />
      </div>
      <select
        :value="sortBy"
        class="sort-select"
        aria-label="排序方式"
        @change="emit('update:sortBy', $event.target.value)"
      >
        <option value="created">⏱ 最新建立</option>
        <option value="dueDate">📅 截止日期</option>
        <option value="priority">⚡ 優先級</option>
      </select>
      <button
        v-if="stats.done > 0"
        type="button"
        class="ghost small clear-btn"
        title="清除所有已完成任務"
        @click="emit('clear-completed')"
      >
        🧹 清除已完成
      </button>
    </div>
  </div>
</template>
