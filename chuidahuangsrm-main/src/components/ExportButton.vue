<script setup lang="ts">
import { ref } from 'vue'
import { Download, FileDown, FileText, Printer } from 'lucide-vue-next'
import { exportToCSV, exportToExcel, exportToJSON, printData, type ExportOptions } from '@/utils/export'

interface Props {
  exportOptions: Omit<ExportOptions, 'filename'>
  filename?: string
  buttonClass?: string
  size?: 'sm' | 'md' | 'lg'
}

const props = withDefaults(defineProps<Props>(), {
  filename: 'export',
  buttonClass: '',
  size: 'md'
})

const isOpen = ref(false)

const sizeClasses = {
  sm: 'px-2 py-1 text-xs',
  md: 'px-3 py-1.5 text-sm',
  lg: 'px-4 py-2 text-base'
}

function handleExport(format: 'csv' | 'excel' | 'json' | 'print') {
  const options: ExportOptions = {
    ...props.exportOptions,
    filename: props.filename
  }

  switch (format) {
    case 'csv':
      exportToCSV(options)
      break
    case 'excel':
      exportToExcel(options)
      break
    case 'json':
      exportToJSON(options)
      break
    case 'print':
      printData(options)
      break
  }

  isOpen.value = false
}
</script>

<template>
  <div class="relative">
    <button
      @click="isOpen = !isOpen"
      :class="[sizeClasses[size], buttonClass]"
      class="inline-flex items-center gap-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors"
    >
      <Download class="w-4 h-4" />
      <span>导出</span>
    </button>

    <Teleport to="body">
      <Transition name="dropdown">
        <div
          v-if="isOpen"
          class="fixed z-50 mt-1 bg-white rounded-lg shadow-lg border border-gray-200 py-1 min-w-[160px]"
          style="top: 100%; left: 50%; transform: translateX(-50%);"
          @click.away="isOpen = false"
        >
          <button
            @click="handleExport('csv')"
            class="flex items-center gap-2 w-full px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
          >
            <FileDown class="w-4 h-4" />
            <span>导出为 CSV</span>
          </button>
          <button
            @click="handleExport('excel')"
            class="flex items-center gap-2 w-full px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
          >
            <FileDown class="w-4 h-4" />
            <span>导出为 Excel</span>
          </button>
          <button
            @click="handleExport('json')"
            class="flex items-center gap-2 w-full px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
          >
            <FileText class="w-4 h-4" />
            <span>导出为 JSON</span>
          </button>
          <hr class="my-1 border-gray-200" />
          <button
            @click="handleExport('print')"
            class="flex items-center gap-2 w-full px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
          >
            <Printer class="w-4 h-4" />
            <span>打印</span>
          </button>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped>
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.15s ease;
}
.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translate(-50%, -10px);
}
</style>
