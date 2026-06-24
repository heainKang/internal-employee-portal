<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50">
    <div class="w-full max-w-sm bg-white rounded-xl shadow-sm border border-gray-200 p-8">
      <div class="mb-8 text-center">
        <h1 class="text-xl font-bold text-gray-900">사내 직원 포털</h1>
        <p class="text-sm text-gray-500 mt-1">계정 정보를 입력하세요</p>
      </div>

      <!-- 세션 만료 배너 -->
      <div v-if="sessionExpired"
        class="mb-4 px-3 py-2 bg-amber-50 border border-amber-200 rounded-lg text-sm text-amber-700 text-center">
        세션이 만료되었습니다. 다시 로그인해 주세요.
      </div>

      <form @submit.prevent="handleLogin" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">이메일</label>
          <input v-model="form.email" type="email" required placeholder="email@bit.com"
            :disabled="loading" class="input" />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">비밀번호</label>
          <div class="relative">
            <input v-model="form.password" :type="showPassword ? 'text' : 'password'" required
              placeholder="••••••••" :disabled="loading" class="input pr-10" />
            <button type="button" @click="showPassword = !showPassword" tabindex="-1"
              class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 focus:outline-none">
              <svg v-if="!showPassword" xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none"
                viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
              </svg>
              <svg v-else xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none"
                viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 4.411m0 0L21 21" />
              </svg>
            </button>
          </div>
        </div>

        <!-- 에러 고정 영역 (레이아웃 점프 방지) -->
        <div class="h-5">
          <p v-if="errorMsg" class="text-sm text-red-500 text-center">{{ errorMsg }}</p>
        </div>

        <button type="submit" :disabled="loading"
          class="w-full py-2.5 px-4 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-300 text-white text-sm font-medium rounded-lg transition-colors">
          {{ loading ? '로그인 중...' : '로그인' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

const form = ref({ email: '', password: '' })
const loading = ref(false)
const errorMsg = ref('')
const showPassword = ref(false)
const sessionExpired = ref(false)

onMounted(() => {
  if (sessionStorage.getItem('sessionExpired')) {
    sessionExpired.value = true
    sessionStorage.removeItem('sessionExpired')
  }
})

async function handleLogin() {
  loading.value = true
  errorMsg.value = ''
  sessionExpired.value = false
  try {
    await auth.login(form.value.email, form.value.password)
    router.push(auth.isAdmin ? '/admin/employees' : '/me')
  } catch {
    errorMsg.value = '이메일 또는 비밀번호가 올바르지 않습니다.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.input {
  @apply w-full px-3 py-2 border border-gray-300 rounded-lg text-sm
    focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent
    disabled:bg-gray-50 disabled:text-gray-400 disabled:cursor-not-allowed;
}
</style>
