<template>
  <div class="min-h-screen flex bg-gray-50">

    <!-- 모바일 오버레이 -->
    <Transition name="fade">
      <div v-if="drawerOpen" @click="drawerOpen = false"
        class="fixed inset-0 bg-black/40 z-20 md:hidden" />
    </Transition>

    <!-- 사이드바 / 드로어 -->
    <aside :class="[
      'fixed inset-y-0 left-0 z-30 w-56 bg-white border-r border-gray-200 flex flex-col',
      'transition-transform duration-200 ease-in-out',
      'md:static md:translate-x-0 md:transition-none md:shrink-0',
      drawerOpen ? 'translate-x-0 shadow-xl' : '-translate-x-full',
    ]">
      <div class="px-5 py-4 border-b border-gray-200 flex items-start justify-between gap-2">
        <div class="min-w-0">
          <h1 class="text-base font-bold text-gray-800">직원 포털</h1>
          <p class="text-sm font-medium text-gray-800 mt-2 truncate">
            {{ auth.user?.firstName }} {{ auth.user?.lastName }}
          </p>
          <p class="text-xs text-gray-400 truncate mt-0.5">{{ auth.user?.email }}</p>
          <span :class="auth.isAdmin
            ? 'bg-blue-100 text-blue-700'
            : 'bg-gray-100 text-gray-600'"
            class="inline-block mt-1.5 px-2 py-0.5 text-xs font-medium rounded-full">
            {{ auth.isAdmin ? '관리자' : '직원' }}
          </span>
        </div>
        <!-- 모바일 닫기 버튼 -->
        <button @click="drawerOpen = false"
          class="md:hidden p-1 text-gray-400 hover:text-gray-600 rounded focus:outline-none shrink-0">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <nav class="flex-1 px-3 py-4 space-y-1">
        <template v-if="auth.isAdmin">
          <router-link to="/admin/employees" class="nav-link" @click="drawerOpen = false">직원 목록</router-link>
          <router-link to="/admin/employees/create" class="nav-link" @click="drawerOpen = false">직원 생성</router-link>
          <router-link to="/admin/background-checks" class="nav-link" @click="drawerOpen = false">배경 조회</router-link>
        </template>
        <router-link to="/me" class="nav-link" @click="drawerOpen = false">내 정보</router-link>
      </nav>

      <div class="px-3 py-4 border-t border-gray-200">
        <button @click="handleLogout"
          class="w-full text-left px-3 py-2 text-sm text-red-500 hover:bg-red-50 rounded-md">
          로그아웃
        </button>
      </div>
    </aside>

    <!-- 메인 영역 -->
    <div class="flex-1 flex flex-col min-w-0">
      <!-- 모바일 상단 헤더 -->
      <header class="md:hidden sticky top-0 z-10 bg-white border-b border-gray-200 flex items-center gap-3 px-4 py-3 shrink-0">
        <button @click="drawerOpen = true"
          class="p-1 text-gray-600 hover:text-gray-900 rounded focus:outline-none">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
          </svg>
        </button>
        <span class="text-sm font-bold text-gray-800">직원 포털</span>
      </header>

      <main class="flex-1 p-4 md:p-8 overflow-auto">
        <slot />
      </main>
    </div>

  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()
const drawerOpen = ref(false)

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

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
