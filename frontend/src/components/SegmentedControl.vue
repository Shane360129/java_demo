<script setup>
defineProps({
  modelValue: { required: true },
  options: { type: Array, required: true },
  ariaLabel: { type: String, default: '' },
  size: { type: String, default: 'md' },
})
defineEmits(['update:modelValue'])
</script>

<template>
  <div :class="['segmented', `segmented-${size}`]" role="radiogroup" :aria-label="ariaLabel">
    <button
      v-for="opt in options"
      :key="opt.value"
      type="button"
      role="radio"
      :aria-checked="modelValue === opt.value"
      :title="opt.title || opt.label"
      :class="['seg-btn', { active: modelValue === opt.value }]"
      @click="$emit('update:modelValue', opt.value)"
    >
      {{ opt.label }}
    </button>
  </div>
</template>
