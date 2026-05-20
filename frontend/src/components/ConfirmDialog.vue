<script setup>
import { watch, onMounted, onBeforeUnmount, useTemplateRef } from 'vue'

const props = defineProps({
  visible: Boolean,
  title: { type: String, default: '確認' },
  message: { type: String, default: '' },
  confirmLabel: { type: String, default: '確定' },
  cancelLabel: { type: String, default: '取消' },
  danger: Boolean,
})
const emit = defineEmits(['confirm', 'cancel'])

const confirmBtn = useTemplateRef('confirmBtn')

function onKey(e) {
  if (!props.visible) return
  if (e.key === 'Escape') {
    e.preventDefault()
    emit('cancel')
  } else if (e.key === 'Enter' && !['INPUT', 'TEXTAREA'].includes(e.target?.tagName)) {
    e.preventDefault()
    emit('confirm')
  }
}

onMounted(() => window.addEventListener('keydown', onKey))
onBeforeUnmount(() => window.removeEventListener('keydown', onKey))

watch(
  () => props.visible,
  (v) => {
    if (v) {
      document.body.style.overflow = 'hidden'
      setTimeout(() => confirmBtn.value?.focus(), 60)
    } else {
      document.body.style.overflow = ''
    }
  },
)
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible" class="modal-overlay" @click.self="emit('cancel')">
        <div class="modal" role="dialog" aria-modal="true" :aria-label="title">
          <h3>{{ title }}</h3>
          <p v-if="message" class="modal-message">{{ message }}</p>
          <div class="modal-actions">
            <button type="button" class="ghost" @click="emit('cancel')">{{ cancelLabel }}</button>
            <button
              ref="confirmBtn"
              type="button"
              :class="danger ? 'danger' : ''"
              @click="emit('confirm')"
            >
              {{ confirmLabel }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>
