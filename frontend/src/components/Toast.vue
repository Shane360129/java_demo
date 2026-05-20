<script setup>
defineProps({
  message: { type: String, default: '' },
  type: { type: String, default: 'info' },
  visible: { type: Boolean, default: false },
  action: { type: String, default: '' },
})
defineEmits(['action'])
</script>

<template>
  <Transition name="toast">
    <div v-if="visible && message" :class="['toast', type]" role="status" aria-live="polite">
      <span v-if="type === 'success'" class="toast-icon">✓</span>
      <span v-else-if="type === 'error'" class="toast-icon">✕</span>
      <span v-else class="toast-icon">ℹ</span>
      <span class="toast-message">{{ message }}</span>
      <button v-if="action" type="button" class="toast-action" @click="$emit('action')">
        {{ action }}
      </button>
    </div>
  </Transition>
</template>
