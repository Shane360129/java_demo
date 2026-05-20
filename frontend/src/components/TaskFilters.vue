<script setup>
import { useTemplateRef } from 'vue'
import SegmentedControl from './SegmentedControl.vue'

defineProps({
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

const searchInput = useTemplateRef('searchInput')

const tabs = [
  { value: 'all', label: '全部', key: 'total', shortcut: '1' },
  { value: 'active', label: '待處理', key: 'active', shortcut: '2' },
  { value: 'done', label: '已完成', key: 'done', shortcut: '3' },
]

const sortOptions = [
  { value: 'created', label: '⏱ 最新', title: '依建立時間排序（新→舊）' },
  { value: 'dueDate', label: '📅 期限', title: '依截止日排序' },
  { value: 'priority', label: '⚡ 優先', title: '依優先級排序' },
]

defineExpose({
  focusSearch() {
    searchInput.value?.focus()
    searchInput.value?.select()
  },
})
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
        :title="`快捷鍵：${tab.shortcut}`"
        @click="emit('update:filter', tab.value)"
      >
        {{ tab.label }}
        <span class="tab-badge">{{ stats[tab.key] }}</span>
      </button>
    </div>

    <div class="search-wrap">
      <span class="search-icon" aria-hidden="true">🔍</span>
      <input
        ref="searchInput"
        type="search"
        placeholder="搜尋（按 / 聚焦）"
        :value="search"
        class="search-input"
        @input="emit('update:search', $event.target.value)"
      />
    </div>

    <div class="meta-row">
      <span class="meta-label">排序</span>
      <SegmentedControl
        :model-value="sortBy"
        :options="sortOptions"
        aria-label="排序方式"
        class="sort-segmented"
        @update:model-value="emit('update:sortBy', $event)"
      />
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
