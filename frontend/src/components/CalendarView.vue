<script setup>
import { ref, computed, watch, onMounted } from 'vue'

const props = defineProps({
  tasks: { type: Array, required: true },
  selectedDate: { type: String, default: '' },
})
const emit = defineEmits([
  'update:selectedDate',
  'edit-task',
  'cycle-status',
  'add-on-date',
])

function ymd(d) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${dd}`
}
function parseYmd(s) {
  if (!s) return null
  const [y, m, d] = s.split('-').map(Number)
  if (!y || !m || !d) return null
  return new Date(y, m - 1, d)
}

const now = new Date()
const todayStr = ymd(now)

const cursor = ref({ year: now.getFullYear(), month: now.getMonth() })

onMounted(() => {
  if (!props.selectedDate) emit('update:selectedDate', todayStr)
})

watch(
  () => props.selectedDate,
  (d) => {
    const parsed = parseYmd(d)
    if (parsed) cursor.value = { year: parsed.getFullYear(), month: parsed.getMonth() }
  },
)

const monthLabel = computed(
  () => `${cursor.value.year} 年 ${cursor.value.month + 1} 月`,
)

const tasksByDate = computed(() => {
  const map = new Map()
  for (const t of props.tasks) {
    if (!t.dueDate) continue
    const list = map.get(t.dueDate)
    if (list) list.push(t)
    else map.set(t.dueDate, [t])
  }
  return map
})

const days = computed(() => {
  const { year, month } = cursor.value
  const startDow = new Date(year, month, 1).getDay()
  const daysInMonth = new Date(year, month + 1, 0).getDate()

  const result = []
  for (let i = startDow; i > 0; i--) {
    result.push(buildDay(new Date(year, month, 1 - i), false))
  }
  for (let i = 1; i <= daysInMonth; i++) {
    result.push(buildDay(new Date(year, month, i), true))
  }
  while (result.length < 42) {
    const last = result[result.length - 1].date
    const next = new Date(last)
    next.setDate(next.getDate() + 1)
    result.push(buildDay(next, false))
  }
  return result
})

function buildDay(date, isCurrentMonth) {
  const ds = ymd(date)
  const tasksOnDay = tasksByDate.value.get(ds) || []
  return {
    date,
    ds,
    isCurrentMonth,
    isToday: ds === todayStr,
    isSelected: ds === props.selectedDate,
    isWeekend: date.getDay() === 0 || date.getDay() === 6,
    isOverdue: ds < todayStr && tasksOnDay.some((t) => t.status !== 'DONE'),
    tasks: tasksOnDay,
  }
}

const hasAnyTasks = computed(() => props.tasks.length > 0)

const selectedTasks = computed(() => {
  if (!props.selectedDate) return []
  return props.tasks.filter((t) => t.dueDate === props.selectedDate)
})

function shift(delta) {
  let { year, month } = cursor.value
  month += delta
  while (month < 0) { month += 12; year-- }
  while (month > 11) { month -= 12; year++ }
  cursor.value = { year, month }
}

function goToday() {
  cursor.value = { year: now.getFullYear(), month: now.getMonth() }
  emit('update:selectedDate', todayStr)
}

function selectDay(day) {
  if (!day.isCurrentMonth) {
    cursor.value = { year: day.date.getFullYear(), month: day.date.getMonth() }
  }
  emit('update:selectedDate', day.ds)
}

const weekDays = ['日', '一', '二', '三', '四', '五', '六']
const statusText = { TODO: '待辦', IN_PROGRESS: '進行中', DONE: '已完成' }

function formatSelectedLabel(s) {
  const d = parseYmd(s)
  if (!d) return ''
  const weekday = ['日', '一', '二', '三', '四', '五', '六'][d.getDay()]
  return `${d.getMonth() + 1} / ${d.getDate()} (週${weekday})`
}
</script>

<template>
  <div class="calendar">
    <div class="cal-nav">
      <button type="button" class="cal-nav-btn" aria-label="上個月" @click="shift(-1)">‹</button>
      <h3 class="cal-month">{{ monthLabel }}</h3>
      <button type="button" class="cal-nav-btn" aria-label="下個月" @click="shift(1)">›</button>
      <button type="button" class="cal-today-btn" @click="goToday">今天</button>
    </div>

    <div class="cal-weekdays">
      <span
        v-for="(w, i) in weekDays"
        :key="w"
        :class="['cal-weekday', { weekend: i === 0 || i === 6 }]"
      >{{ w }}</span>
    </div>

    <div class="cal-grid">
      <button
        v-for="day in days"
        :key="day.ds"
        type="button"
        :class="[
          'cal-day',
          {
            'other-month': !day.isCurrentMonth,
            today: day.isToday,
            selected: day.isSelected,
            'has-tasks': day.tasks.length > 0,
            'is-overdue': day.isOverdue,
            weekend: day.isWeekend,
          },
        ]"
        :aria-label="day.ds + (day.tasks.length ? `（${day.tasks.length} 筆任務）` : '')"
        @click="selectDay(day)"
      >
        <span class="day-num">{{ day.date.getDate() }}</span>
        <div v-if="day.tasks.length" class="day-dots">
          <span
            v-for="t in day.tasks.slice(0, 3)"
            :key="t.id"
            :class="['dot', t.priority.toLowerCase(), { done: t.status === 'DONE' }]"
          ></span>
          <span v-if="day.tasks.length > 3" class="day-more">+{{ day.tasks.length - 3 }}</span>
        </div>
      </button>
    </div>

    <div v-if="!hasAnyTasks" class="cal-empty">
      <span class="cal-empty-icon" aria-hidden="true">🗓️</span>
      <span>還沒有任務，新增第一筆之後這裡就會看到日期上的點。</span>
    </div>

    <div class="selected-panel">
      <div class="selected-header">
        <h4>
          <span v-if="selectedDate">📌 {{ formatSelectedLabel(selectedDate) }}</span>
          <span v-else>👇 點上方任一日</span>
          <span v-if="selectedDate === todayStr" class="today-tag">今天</span>
        </h4>
        <button
          v-if="selectedDate"
          type="button"
          class="ghost small add-on-date-btn"
          @click="$emit('add-on-date', selectedDate)"
        >➕ 新增當日任務</button>
      </div>
      <ul v-if="selectedTasks.length" class="selected-tasks">
        <li
          v-for="task in selectedTasks"
          :key="task.id"
          :class="['mini-task', task.status.toLowerCase()]"
        >
          <button
            type="button"
            class="mini-status"
            :title="`狀態：${statusText[task.status]}（點擊切換）`"
            @click="$emit('cycle-status', task)"
          >
            <span v-if="task.status === 'TODO'" aria-hidden="true">○</span>
            <span v-else-if="task.status === 'IN_PROGRESS'" aria-hidden="true">◐</span>
            <span v-else aria-hidden="true">✓</span>
          </button>
          <span class="mini-title" @click="$emit('edit-task', task)">{{ task.title }}</span>
          <span
            :class="['priority-dot', task.priority.toLowerCase()]"
            :title="`優先級：${task.priority}`"
          ></span>
        </li>
      </ul>
      <p v-else-if="selectedDate" class="mini-empty">這天沒有任務</p>
    </div>
  </div>
</template>
