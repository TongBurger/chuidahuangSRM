  import { createApp } from 'vue'
  import { createPinia } from 'pinia'
  import App from './App.vue'
  import router from './router'
import { useAppStore } from './stores/useAppStore'
  import './assets/main.css'

  const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
  app.use(router)

const appStore = useAppStore(pinia)
appStore.initializePreferences()
appStore.initializeAuth()

  app.mount('#app')
