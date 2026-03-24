  <script setup lang="ts">
  import { RouterLink } from 'vue-router'
  </script>

  <template>
    <div class="flex flex-col items-center justify-center min-h-[60vh] text-center">
      <div class="text-8xl font-bold text-gray-200 mb-4">404</div>
      <h1 class="text-2xl font-semibold text-gray-900 mb-2">页面不存在</h1>
      <p class="text-gray-500 mb-8">您访问的页面不存在或已被删除</p>
      <RouterLink to="/">
        <button class="px-6 py-3 bg-blue-600 text-white rounded-lg font-medium hover:bg-blue-700 transition-colors">
          返回工作台
        </button>
      </RouterLink>
    </div>
  </template>
