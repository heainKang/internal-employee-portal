<template>
  <AppLayout>
    <div>
      <div class="flex items-center justify-between mb-6">
        <h2 class="text-xl font-bold text-gray-900">직원 목록
          <span class="text-sm font-normal text-gray-400 ml-2">총 {{ totalElements }}명</span>
        </h2>
        <router-link to="/admin/employees/create"
          class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg transition-colors">
          + 직원 생성
        </router-link>
      </div>

      <!-- 정렬 옵션 -->
      <div class="flex gap-3 mb-4">
        <select v-model="sortBy" @change="fetchEmployees" class="select">
          <option value="createdAt">등록일</option>
          <option value="firstName">이름</option>
          <option value="department">부서</option>
          <option value="hireDate">입사일</option>
        </select>
        <select v-model="direction" @change="fetchEmployees" class="select">
          <option value="desc">내림차순</option>
          <option value="asc">오름차순</option>
        </select>
      </div>

      <div v-if="loading" class="text-sm text-gray-400">불러오는 중...</div>

      <div v-else class="bg-white rounded-xl border border-gray-200 overflow-hidden">
        <table class="w-full text-sm">
          <thead class="bg-gray-50 border-b border-gray-200">
            <tr>
              <th class="th">사번</th>
              <th class="th">이름</th>
              <th class="th">이메일</th>
              <th class="th">부서</th>
              <th class="th">직책</th>
              <th class="th">권한</th>
              <th class="th">상태</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
            <tr v-for="emp in employees" :key="emp.id"
              @click="$router.push(`/admin/employees/${emp.id}`)"
              class="hover:bg-gray-50 cursor-pointer transition-colors">
              <td class="td text-gray-500">{{ emp.employeeId }}</td>
              <td class="td font-medium text-gray-900">{{ emp.firstName }} {{ emp.lastName }}</td>
              <td class="td text-gray-600">{{ emp.email }}</td>
              <td class="td text-gray-600">{{ emp.department || '-' }}</td>
              <td class="td text-gray-600">{{ emp.position || '-' }}</td>
              <td class="td">
                <span :class="emp.role === 'ROLE_ADMIN' ? 'badge-blue' : 'badge-gray'">
                  {{ emp.role === 'ROLE_ADMIN' ? '관리자' : '직원' }}
                </span>
              </td>
              <td class="td">
                <span :class="emp.status === 'ACTIVE' ? 'badge-green' : 'badge-red'">
                  {{ emp.status === 'ACTIVE' ? '재직' : '퇴사' }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
        <p v-if="employees.length === 0" class="text-center text-sm text-gray-400 py-10">직원이 없습니다.</p>
      </div>

      <!-- 페이지네이션 -->
      <div v-if="totalPages > 1" class="flex items-center justify-center gap-2 mt-4">
        <button @click="goPage(currentPage - 1)" :disabled="currentPage === 0"
          class="px-3 py-1.5 text-sm border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed">
          이전
        </button>
        <button v-for="p in pageNumbers" :key="p"
          @click="goPage(p)"
          :class="['px-3 py-1.5 text-sm rounded-md border transition-colors',
            p === currentPage
              ? 'bg-blue-600 text-white border-blue-600'
              : 'border-gray-300 hover:bg-gray-50']">
          {{ p + 1 }}
        </button>
        <button @click="goPage(currentPage + 1)" :disabled="currentPage >= totalPages - 1"
          class="px-3 py-1.5 text-sm border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed">
          다음
        </button>
      </div>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import AppLayout from '../../components/AppLayout.vue'
import api from '../../api/axios'

const employees = ref([])
const loading = ref(true)
const currentPage = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)
const sortBy = ref('createdAt')
const direction = ref('desc')
const PAGE_SIZE = 20

const pageNumbers = computed(() => {
  const start = Math.max(0, currentPage.value - 2)
  const end = Math.min(totalPages.value - 1, currentPage.value + 2)
  return Array.from({ length: end - start + 1 }, (_, i) => start + i)
})

async function fetchEmployees() {
  loading.value = true
  try {
    const res = await api.get('/admin/employees', {
      params: { page: currentPage.value, size: PAGE_SIZE, sortBy: sortBy.value, direction: direction.value }
    })
    const page = res.data.data
    employees.value = page.content
    totalPages.value = page.totalPages
    totalElements.value = page.totalElements
  } finally {
    loading.value = false
  }
}

function goPage(page) {
  if (page < 0 || page >= totalPages.value) return
  currentPage.value = page
  fetchEmployees()
}

onMounted(fetchEmployees)
</script>

<style scoped>
.th { @apply px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wide; }
.td { @apply px-4 py-3; }
.select { @apply px-3 py-1.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white; }
.badge-green { @apply inline-block px-2 py-0.5 text-xs font-medium bg-green-100 text-green-700 rounded-full; }
.badge-red { @apply inline-block px-2 py-0.5 text-xs font-medium bg-red-100 text-red-700 rounded-full; }
.badge-blue { @apply inline-block px-2 py-0.5 text-xs font-medium bg-blue-100 text-blue-700 rounded-full; }
.badge-gray { @apply inline-block px-2 py-0.5 text-xs font-medium bg-gray-100 text-gray-600 rounded-full; }
</style>
