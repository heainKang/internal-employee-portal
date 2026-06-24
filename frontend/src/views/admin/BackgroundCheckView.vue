<template>
  <AppLayout>
    <div class="max-w-3xl">
      <h2 class="text-xl font-bold text-gray-900 mb-6">배경 조회</h2>

      <!-- 배경 조회 요청 -->
      <div class="bg-white rounded-xl border border-gray-200 p-6 mb-6">
        <h3 class="text-sm font-semibold text-gray-700 mb-4">새 배경 조회 요청</h3>
        <form @submit.prevent="requestCheck" class="flex gap-3">
          <select v-model="selectedEmployeeId" required class="input flex-1">
            <option value="">직원 선택</option>
            <option v-for="emp in employees" :key="emp.employeeId" :value="emp.employeeId">
              {{ emp.firstName }} {{ emp.lastName }} ({{ emp.employeeId }})
            </option>
          </select>
          <button type="submit" :disabled="requesting || !selectedEmployeeId"
            class="px-4 py-2 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-300 text-white text-sm font-medium rounded-lg transition-colors whitespace-nowrap">
            {{ requesting ? '요청 중...' : '조회 요청' }}
          </button>
        </form>

        <!-- 요청 결과 -->
        <div v-if="newResult" class="mt-4 p-4 bg-gray-50 rounded-lg text-sm space-y-1.5">
          <p class="font-semibold text-gray-700 mb-2">요청 결과</p>
          <p><span class="text-gray-500">Check ID:</span> <span class="font-mono text-gray-900">{{ newResult.checkId }}</span></p>
          <p><span class="text-gray-500">직원 ID:</span> {{ newResult.employeeId }}</p>
          <p>
            <span class="text-gray-500">상태:</span>
            <span :class="statusClass(newResult.status)" class="ml-1">{{ statusLabel(newResult.status) }}</span>
          </p>
          <p><span class="text-gray-500">요청일시:</span> {{ formatDate(newResult.createdAt) }}</p>
        </div>
        <p v-if="requestError" class="mt-3 text-sm text-red-500">{{ requestError }}</p>
      </div>

      <!-- 이력 조회 -->
      <div class="bg-white rounded-xl border border-gray-200 p-6">
        <h3 class="text-sm font-semibold text-gray-700 mb-4">직원별 조회 이력</h3>
        <div class="flex gap-3 mb-4">
          <select v-model="historyEmployeeId" class="input flex-1">
            <option value="">직원 선택</option>
            <option v-for="emp in employees" :key="emp.employeeId" :value="emp.employeeId">
              {{ emp.firstName }} {{ emp.lastName }} ({{ emp.employeeId }})
            </option>
          </select>
          <button @click="fetchHistory" :disabled="!historyEmployeeId || historyLoading"
            class="px-4 py-2 border border-gray-300 hover:bg-gray-50 disabled:opacity-40 text-sm font-medium rounded-lg transition-colors whitespace-nowrap">
            조회
          </button>
        </div>

        <div v-if="historyLoading" class="text-sm text-gray-400">불러오는 중...</div>

        <template v-else-if="history.length > 0">
          <div class="overflow-x-auto">
          <table class="w-full text-sm min-w-[520px]">
            <thead class="bg-gray-50">
              <tr>
                <th class="th">Check ID</th>
                <th class="th">상태</th>
                <th class="th">범죄기록</th>
                <th class="th">신용등급</th>
                <th class="th">요청일시</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-100">
              <tr v-for="item in history" :key="item.checkId" class="hover:bg-gray-50 cursor-pointer"
                @click="fetchDetail(item.checkId)">
                <td class="td font-mono text-xs text-gray-600">{{ item.checkId }}</td>
                <td class="td"><span :class="statusClass(item.status)">{{ statusLabel(item.status) }}</span></td>
                <td class="td">{{ item.criminalRecord !== undefined ? (item.criminalRecord ? '있음' : '없음') : '-' }}</td>
                <td class="td">{{ item.creditScore || '-' }}</td>
                <td class="td text-gray-500">{{ formatDate(item.createdAt) }}</td>
              </tr>
            </tbody>
          </table>
          </div>
        </template>
        <p v-else-if="historySearched" class="text-sm text-gray-400 text-center py-6">조회 이력이 없습니다.</p>

        <p v-if="historyError" class="text-sm text-red-500 mt-2">{{ historyError }}</p>

        <!-- 단건 상세 -->
        <div v-if="detail" class="mt-4 p-4 bg-blue-50 rounded-lg text-sm space-y-1.5 border border-blue-100">
          <p class="font-semibold text-blue-700 mb-2">상세 결과 — {{ detail.checkId }}</p>
          <div class="grid grid-cols-2 gap-2">
            <p><span class="text-gray-500">상태:</span> <span :class="statusClass(detail.status)">{{ statusLabel(detail.status) }}</span></p>
            <p><span class="text-gray-500">범죄기록:</span> {{ detail.criminalRecord !== undefined ? (detail.criminalRecord ? '있음' : '없음') : '-' }}</p>
            <p><span class="text-gray-500">학력 인증:</span> {{ detail.educationVerified !== undefined ? (detail.educationVerified ? '확인' : '미확인') : '-' }}</p>
            <p><span class="text-gray-500">경력 인증:</span> {{ detail.employmentVerified !== undefined ? (detail.employmentVerified ? '확인' : '미확인') : '-' }}</p>
            <p><span class="text-gray-500">신용등급:</span> {{ detail.creditScore || '-' }}</p>
            <p><span class="text-gray-500">완료일시:</span> {{ detail.completedAt ? formatDate(detail.completedAt) : '-' }}</p>
          </div>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import AppLayout from '../../components/AppLayout.vue'
import api from '../../api/axios'

const employees = ref([])
const selectedEmployeeId = ref('')
const requesting = ref(false)
const newResult = ref(null)
const requestError = ref('')

const historyEmployeeId = ref('')
const history = ref([])
const historyLoading = ref(false)
const historySearched = ref(false)
const historyError = ref('')
const detail = ref(null)

onMounted(async () => {
  const res = await api.get('/admin/employees', { params: { size: 200, sortBy: 'firstName', direction: 'asc' } })
  employees.value = res.data.data.content.filter(e => e.status === 'ACTIVE')
})

async function requestCheck() {
  requesting.value = true
  newResult.value = null
  requestError.value = ''
  try {
    const res = await api.post('/admin/background-checks', { employeeId: selectedEmployeeId.value })
    newResult.value = res.data.data
  } catch (err) {
    requestError.value = err.response?.data?.message || '요청에 실패했습니다.'
  } finally {
    requesting.value = false
  }
}

async function fetchHistory() {
  historyLoading.value = true
  history.value = []
  detail.value = null
  historyError.value = ''
  historySearched.value = false
  try {
    const res = await api.get('/admin/background-checks', { params: { employeeId: historyEmployeeId.value } })
    history.value = res.data.data?.checks || []
    historySearched.value = true
  } catch (err) {
    historyError.value = err.response?.data?.message || '이력 조회에 실패했습니다.'
    historySearched.value = true
  } finally {
    historyLoading.value = false
  }
}

async function fetchDetail(checkId) {
  try {
    const res = await api.get(`/admin/background-checks/${checkId}`)
    detail.value = res.data.data
  } catch (err) {
    detail.value = null
  }
}

function statusLabel(status) {
  return { pending: '대기중', clear: '이상 없음', flagged: '이슈 있음' }[status] ?? status
}

function statusClass(status) {
  return {
    pending:  'inline-block px-2 py-0.5 text-xs font-medium bg-yellow-100 text-yellow-700 rounded-full',
    clear:    'inline-block px-2 py-0.5 text-xs font-medium bg-green-100 text-green-700 rounded-full',
    flagged:  'inline-block px-2 py-0.5 text-xs font-medium bg-red-100 text-red-700 rounded-full',
  }[status] ?? 'inline-block px-2 py-0.5 text-xs font-medium bg-gray-100 text-gray-600 rounded-full'
}

function formatDate(iso) {
  if (!iso) return '-'
  return new Date(iso).toLocaleString('ko-KR', { timeZone: 'Asia/Seoul' })
}
</script>

<style scoped>
.th { @apply px-4 py-2.5 text-left text-xs font-semibold text-gray-500 uppercase tracking-wide; }
.td { @apply px-4 py-3; }
.input { @apply w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white; }
</style>
