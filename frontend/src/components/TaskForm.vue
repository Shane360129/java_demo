<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  initial: { type: Object, default: null },
})
const emit = defineEmits(['submit', 'cancel'])

function emptyForm() {
  return { title: '', description: '', status: 'TODO', dueDate: '' }
}

const form = ref(emptyForm())

watch(
  () => props.initial,
  (val) => {
    form.value = val
      ? {
          title: val.title ?? '',
          description: val.description ?? '',
          status: val.status ?? 'TODO',
          dueDate: val.dueDate ?? '',
        }
      : emptyForm()
  },
)

function onSubmit() {
  if (!form.value.title.trim()) return
  emit('submit', {
    title: form.value.title,
    description: form.value.description || null,
    status: form.value.status,
    dueDate: form.value.dueDate || null,
  })
  if (!props.initial) form.value = emptyForm()
}
</script>

<template>
  <form @submit.prevent="onSubmit">
    <label>
      標題
      <input v-model="form.title" required maxlength="200" placeholder="例如：寄出履歷" />
    </label>
    <label>
      描述
      <textarea v-model="form.description" maxlength="1000" rows="3" />
    </label>
    <div class="row">
      <label>
        狀態
        <select v-model="form.status">
          <option value="TODO">待辦</option>
          <option value="IN_PROGRESS">進行中</option>
          <option value="DONE">已完成</option>
        </select>
      </label>
      <label>
        截止日
        <input type="date" v-model="form.dueDate" />
      </label>
    </div>
    <div class="actions">
      <button type="submit">{{ initial ? '更新' : '新增' }}</button>
      <button v-if="initial" type="button" class="ghost" @click="$emit('cancel')">取消</button>
    </div>
  </form>
</template>
