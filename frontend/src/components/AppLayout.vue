<template>
  <div class="min-h-screen flex bg-gray-50">
    <!-- 사이드바 -->
    <aside class="w-56 bg-white border-r border-gray-200 flex flex-col">
      <div class="px-6 py-5 border-b border-gray-200">
        <h1 class="text-base font-bold text-gray-800">직원 포털</h1>
        <p class="text-xs text-gray-500 mt-0.5">{{ auth.user?.firstName }} {{ auth.user?.lastName }}</p>
      </div>

      <nav class="flex-1 px-3 py-4 space-y-1">
        <template v-if="auth.isAdmin">
          <router-link to="/admin/employees" class="nav-link">직원 목록</router-link>
          <router-link to="/admin/employees/create" class="nav-link">직원 생성</router-link>
          <router-link to="/admin/background-checks" class="nav-link">배경 조회</router-link>
        </template>
        <router-link to="/me" class="nav-link">내 정보</router-link>
      </nav>

      <div class="px-3 py-4 border-t border-gray-200">
        <button @click="handleLogout"
          class="w-full text-left px-3 py-2 text-sm text-red-500 hover:bg-red-50 rounded-md">
          로그아웃
        </button>
      </div>
    </aside>

    <!-- 메인 컨텐츠 -->
    <main class="flex-1 p-8 overflow-auto">
      <slot />
    </main>
  </div>
</template>

<script setup>
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.nav-link {
  @apply block px-3 py-2 text-sm text-gray-700 rounded-md hover:bg-gray-100;
}
.router-link-active {
  @apply bg-blue-50 text-blue-700 font-medium;
}
</style>
