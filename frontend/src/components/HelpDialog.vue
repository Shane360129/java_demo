<script setup>
import { onMounted, onBeforeUnmount } from 'vue'

const props = defineProps({ visible: Boolean })
const emit = defineEmits(['close'])

const shortcuts = [
  { keys: ['N'], desc: '聚焦至新增任務的標題' },
  { keys: ['/'], desc: '聚焦搜尋框' },
  { keys: ['Esc'], desc: '取消編輯／清除搜尋／關閉對話框' },
  { keys: ['1'], desc: '切換到「全部」' },
  { keys: ['2'], desc: '切換到「待處理」' },
  { keys: ['3'], desc: '切換到「已完成」' },
  { keys: ['V'], desc: '切換列表 / 行事曆檢視' },
  { keys: ['T'], desc: '切換深色模式' },
  { keys: ['?'], desc: '顯示這個說明' },
]

function onKey(e) {
  if (props.visible && e.key === 'Escape') emit('close')
}
onMounted(() => window.addEventListener('keydown', onKey))
onBeforeUnmount(() => window.removeEventListener('keydown', onKey))
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible" class="modal-overlay" @click.self="emit('close')">
        <div class="modal help-modal" role="dialog" aria-modal="true" aria-label="鍵盤快捷鍵">
          <h3>⌨️ 鍵盤快捷鍵</h3>
          <ul class="shortcut-list">
            <li v-for="s in shortcuts" :key="s.desc">
              <span class="shortcut-keys">
                <kbd v-for="k in s.keys" :key="k">{{ k }}</kbd>
              </span>
              <span class="shortcut-desc">{{ s.desc }}</span>
            </li>
          </ul>
          <p class="help-tip">點任務上的圓圈可循環切換狀態；點任務本體進入編輯。</p>
          <div class="modal-actions">
            <button type="button" @click="emit('close')">知道了</button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>
