<template>
  <AppLayout>
    <div class="max-w-4xl">
      <!-- 페이지 헤더 -->
      <div class="flex items-center justify-between mb-6">
        <div>
          <h2 class="text-xl font-bold text-gray-900">직원 배경조회 관리</h2>
          <p class="text-xs text-gray-500 mt-0.5">채용 전 배경조회를 요청하고 결과를 확인합니다.</p>
        </div>
        <button @click="openModal" :disabled="!selectedEmployeeId"
          class="px-4 py-2 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-300 text-white text-sm font-medium rounded-lg transition-colors flex items-center gap-1.5">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
          </svg>
          새 배경조회 요청
        </button>
      </div>

      <!-- 직원 선택 -->
      <div class="bg-white rounded-xl border border-gray-200 p-4 mb-4">
        <div class="flex items-center gap-3">
          <label class="text-sm font-medium text-gray-700 whitespace-nowrap">직원 선택</label>
          <select v-model="selectedEmployeeId" @change="onEmployeeChange" class="input max-w-xs">
            <option value="">직원을 선택하세요</option>
            <option v-for="emp in employees" :key="emp.employeeId" :value="emp.employeeId">
              {{ emp.firstName }} {{ emp.lastName }} ({{ emp.employeeId }})
            </option>
          </select>
          <span v-if="selectedEmployee" class="text-sm text-gray-400">
            {{ selectedEmployee.department || '-' }} · {{ selectedEmployee.position || '-' }}
          </span>
        </div>
      </div>

      <!-- 이력 테이블 카드 -->
      <div class="bg-white rounded-xl border border-gray-200">
        <div class="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
          <h3 class="text-sm font-semibold text-gray-700">배경조회 이력</h3>
          <div v-if="hasPending" class="flex items-center gap-1.5 text-xs text-yellow-600 font-medium">
            <svg class="w-3.5 h-3.5 animate-spin" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.4 0 0 5.4 0 12h4z"/>
            </svg>
            대기 중 항목 자동 갱신 중 (30초 간격)
          </div>
        </div>

        <!-- 빈 상태: 직원 미선택 -->
        <div v-if="!selectedEmployeeId" class="py-16 text-center">
          <svg class="w-10 h-10 text-gray-300 mx-auto mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
              d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
          </svg>
          <p class="text-sm text-gray-400">직원을 선택하면 배경조회 이력이 표시됩니다.</p>
        </div>

        <!-- 로딩 -->
        <div v-else-if="loading" class="py-16 text-center text-sm text-gray-400">불러오는 중...</div>

        <!-- 이력 테이블 -->
        <template v-else-if="checks.length > 0">
          <div class="overflow-x-auto">
            <table class="w-full text-sm min-w-[560px]">
              <thead class="bg-gray-50">
                <tr>
                  <th class="th">상태</th>
                  <th class="th">Check ID</th>
                  <th class="th">요청일시</th>
                  <th class="th">완료일시</th>
                  <th class="th text-right">상세</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-100">
                <tr v-for="check in checks" :key="check.checkId"
                  :class="[
                    'transition-colors',
                    check.status !== 'pending'
                      ? 'hover:bg-gray-50 cursor-pointer'
                      : 'cursor-default opacity-80'
                  ]"
                  @click="onCheckClick(check)">
                  <!-- 상태 뱃지 -->
                  <td class="td">
                    <span v-if="check.status === 'pending'"
                      class="inline-flex items-center gap-1.5 px-2 py-0.5 text-xs font-medium bg-yellow-100 text-yellow-700 rounded-full">
                      <svg class="w-3 h-3 animate-spin" fill="none" viewBox="0 0 24 24">
                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.4 0 0 5.4 0 12h4z"/>
                      </svg>
                      조회 중
                    </span>
                    <span v-else :class="statusClass(check.status)">{{ statusLabel(check.status) }}</span>
                  </td>
                  <td class="td font-mono text-xs text-gray-400">{{ check.checkId }}</td>
                  <td class="td text-gray-600">{{ formatDate(check.createdAt) }}</td>
                  <td class="td text-gray-500">{{ check.completedAt ? formatDate(check.completedAt) : '-' }}</td>
                  <td class="td text-right">
                    <span v-if="check.status !== 'pending'" class="text-xs text-blue-500 hover:text-blue-700">
                      상세 보기 →
                    </span>
                    <span v-else class="text-xs text-gray-300">진행 중</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </template>

        <!-- 이력 없음 -->
        <div v-else class="py-16 text-center text-sm text-gray-400">
          배경조회 이력이 없습니다.
        </div>
      </div>
    </div>

    <!-- ── 새 요청 모달 ── -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="showModal"
          class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4"
          @click.self="showModal = false">
          <div class="bg-white rounded-2xl shadow-xl w-full max-w-sm p-6">
            <h3 class="text-base font-semibold text-gray-900 mb-4">새 배경조회 요청</h3>

            <div class="bg-gray-50 rounded-xl p-4 mb-5 space-y-2 text-sm">
              <div class="flex justify-between">
                <span class="text-gray-500">직원</span>
                <span class="font-medium text-gray-900">{{ selectedEmployee?.firstName }} {{ selectedEmployee?.lastName }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-500">사번</span>
                <span class="font-medium text-gray-900">{{ selectedEmployee?.employeeId }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-500">부서</span>
                <span class="text-gray-700">{{ selectedEmployee?.department || '-' }}</span>
              </div>
            </div>

            <p class="text-xs text-gray-400 mb-5 leading-relaxed">
              배경조회를 요청하면 외부 기관에 정보가 전송됩니다.<br>
              결과는 보통 수 분 내 업데이트되며, 조회 중에는 자동으로 상태가 갱신됩니다.
            </p>

            <div class="flex gap-3">
              <button @click="showModal = false"
                class="flex-1 px-4 py-2 border border-gray-300 text-sm font-medium rounded-lg hover:bg-gray-50 transition-colors">
                취소
              </button>
              <button @click="submitRequest" :disabled="requesting"
                class="flex-1 px-4 py-2 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-300 text-white text-sm font-medium rounded-lg transition-colors">
                {{ requesting ? '요청 중...' : '요청 확인' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- ── 상세 리포트 모달 ── -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="detail"
          class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4"
          @click.self="detail = null">
          <div class="bg-white rounded-2xl shadow-xl w-full max-w-lg p-6">
            <!-- 모달 헤더 -->
            <div class="flex items-start justify-between mb-5">
              <div>
                <h3 class="text-base font-semibold text-gray-900">배경조회 상세 리포트</h3>
                <p class="text-xs text-gray-400 font-mono mt-0.5">{{ detail.checkId }}</p>
              </div>
              <button @click="detail = null" class="text-gray-400 hover:text-gray-600 mt-0.5">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                </svg>
              </button>
            </div>

            <!-- 상태 -->
            <div class="flex items-center gap-2 mb-5">
              <span :class="statusClass(detail.status)">{{ statusLabel(detail.status) }}</span>
              <span class="text-xs text-gray-400">완료일시: {{ detail.completedAt ? formatDate(detail.completedAt) : '-' }}</span>
            </div>

            <!-- 로딩 -->
            <div v-if="detailLoading" class="py-8 text-center text-sm text-gray-400">불러오는 중...</div>

            <!-- 결과 카드 -->
            <div v-else class="grid grid-cols-2 gap-3">
              <div :class="['rounded-xl p-4 border', detail.criminalRecord ? 'bg-red-50 border-red-200' : 'bg-green-50 border-green-200']">
                <p class="text-xs font-medium text-gray-500 mb-2">범죄 이력</p>
                <p :class="['text-lg font-bold', detail.criminalRecord ? 'text-red-600' : 'text-green-600']">
                  {{ detail.criminalRecord !== undefined ? (detail.criminalRecord ? '있음' : '없음') : '-' }}
                </p>
              </div>

              <div :class="['rounded-xl p-4 border', detail.educationVerified === false ? 'bg-yellow-50 border-yellow-200' : 'bg-green-50 border-green-200']">
                <p class="text-xs font-medium text-gray-500 mb-2">학력 검증</p>
                <p :class="['text-lg font-bold', detail.educationVerified === false ? 'text-yellow-600' : 'text-green-600']">
                  {{ detail.educationVerified !== undefined ? (detail.educationVerified ? '확인됨' : '미확인') : '-' }}
                </p>
              </div>

              <div :class="['rounded-xl p-4 border', detail.employmentVerified === false ? 'bg-yellow-50 border-yellow-200' : 'bg-green-50 border-green-200']">
                <p class="text-xs font-medium text-gray-500 mb-2">경력 검증</p>
                <p :class="['text-lg font-bold', detail.employmentVerified === false ? 'text-yellow-600' : 'text-green-600']">
                  {{ detail.employmentVerified !== undefined ? (detail.employmentVerified ? '확인됨' : '미확인') : '-' }}
                </p>
              </div>

              <div class="rounded-xl p-4 border bg-blue-50 border-blue-200">
                <p class="text-xs font-medium text-gray-500 mb-2">신용 점수</p>
                <p class="text-lg font-bold text-blue-600">{{ detail.creditScore ?? '-' }}</p>
              </div>
            </div>

            <div class="mt-4 pt-4 border-t border-gray-100 text-xs text-gray-400">
              요청일시: {{ formatDate(detail.createdAt) }}
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- ── Toast ── -->
    <Teleport to="body">
      <Transition name="toast">
        <div v-if="toast"
          class="fixed bottom-6 right-6 z-[60] bg-gray-900 text-white text-sm px-4 py-3 rounded-xl shadow-lg max-w-sm leading-relaxed">
          {{ toast }}
        </div>
      </Transition>
    </Teleport>
  </AppLayout>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import AppLayout from '../../components/AppLayout.vue'
import api from '../../api/axios'

// ── 상태 ──────────────────────────────────────────────────────────
const employees = ref([])
const selectedEmployeeId = ref('')
const checks = ref([])
const loading = ref(false)
const detail = ref(null)
const detailLoading = ref(false)
const showModal = ref(false)
const requesting = ref(false)
const toast = ref('')
let toastTimer = null
let pollTimer = null

// ── Computed ──────────────────────────────────────────────────────
const selectedEmployee = computed(() =>
  employees.value.find(e => e.employeeId === selectedEmployeeId.value)
)

const hasPending = computed(() =>
  checks.value.some(c => c.status === 'pending')
)

// ── 초기화 ────────────────────────────────────────────────────────
onMounted(async () => {
  try {
    const res = await api.get('/admin/employees', {
      params: { size: 200, sortBy: 'firstName', direction: 'asc' }
    })
    employees.value = res.data.data.content.filter(e => e.status === 'ACTIVE')
  } catch {
    showToast('직원 목록을 불러오지 못했습니다.')
  }
})

onUnmounted(() => clearPoll())

// ── 직원 변경 ─────────────────────────────────────────────────────
function onEmployeeChange() {
  checks.value = []
  detail.value = null
  clearPoll()
  if (selectedEmployeeId.value) fetchHistory()
}

// ── 이력 조회 (GET ?employeeId) ───────────────────────────────────
async function fetchHistory() {
  loading.value = true
  try {
    const res = await api.get('/admin/background-checks', {
      params: { employeeId: selectedEmployeeId.value }
    })
    checks.value = res.data.data?.checks || []
    if (hasPending.value) startPoll()
  } catch (err) {
    showToast(formatApiError(err, '이력 조회에 실패했습니다.'))
  } finally {
    loading.value = false
  }
}

// ── Polling: 30초마다 pending 항목 상태 갱신 ──────────────────────
function startPoll() {
  clearPoll()
  pollTimer = setInterval(pollPending, 30000)
}

function clearPoll() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

async function pollPending() {
  const pending = checks.value.filter(c => c.status === 'pending')
  if (pending.length === 0) { clearPoll(); return }

  for (const check of pending) {
    try {
      const res = await api.get(`/admin/background-checks/${check.checkId}`)
      const updated = res.data.data
      const idx = checks.value.findIndex(c => c.checkId === check.checkId)
      if (idx !== -1) checks.value[idx] = { ...checks.value[idx], ...updated }
    } catch {
      // 폴링 실패는 조용히 처리 — 다음 사이클에 재시도
    }
  }

  if (!hasPending.value) clearPoll()
}

// ── 새 요청 모달 ──────────────────────────────────────────────────
function openModal() {
  if (!selectedEmployeeId.value) return
  showModal.value = true
}

async function submitRequest() {
  requesting.value = true
  try {
    const res = await api.post('/admin/background-checks', {
      employeeId: selectedEmployeeId.value
    })
    const newCheck = res.data.data
    checks.value = [newCheck, ...checks.value]
    showModal.value = false
    if (hasPending.value) startPoll()
  } catch (err) {
    showToast(formatApiError(err, '요청에 실패했습니다.'))
  } finally {
    requesting.value = false
  }
}

// ── 행 클릭: 상세 조회 ────────────────────────────────────────────
async function onCheckClick(check) {
  if (check.status === 'pending') {
    showToast('현재 조사가 진행 중입니다. 완료 후 상세 정보를 확인할 수 있습니다.')
    return
  }

  // 먼저 기본 정보로 모달 열고, 비동기로 상세 로드
  detail.value = { checkId: check.checkId, status: check.status, createdAt: check.createdAt, completedAt: check.completedAt }
  detailLoading.value = true

  try {
    const res = await api.get(`/admin/background-checks/${check.checkId}`)
    detail.value = res.data.data
  } catch (err) {
    detail.value = null
    showToast(formatApiError(err, '서버와 통신이 원활하지 않습니다. 잠시 후 다시 시도해 주세요.'))
  } finally {
    detailLoading.value = false
  }
}

// ── Toast ─────────────────────────────────────────────────────────
function showToast(msg) {
  toast.value = msg
  if (toastTimer) clearTimeout(toastTimer)
  toastTimer = setTimeout(() => { toast.value = '' }, 4000)
}

// ── 에러 포매터 ───────────────────────────────────────────────────
const ERROR_SUFFIX = {
  X001: '서비스 불가',
  X002: '타임아웃',
  X003: '500 서버 오류',
  X004: '결과 없음',
  X005: 'CB OPEN',
}

function formatApiError(err, fallback) {
  const code = err.response?.data?.code
  const msg  = err.response?.data?.message || fallback
  const suffix = ERROR_SUFFIX[code]
  return suffix ? `${msg} (${suffix})` : msg
}

// ── UI 헬퍼 ──────────────────────────────────────────────────────
function statusLabel(status) {
  return { pending: '조회 중', clear: '이상 없음', flagged: '이슈 있음' }[status] ?? status
}

function statusClass(status) {
  return {
    pending: 'inline-block px-2 py-0.5 text-xs font-medium bg-yellow-100 text-yellow-700 rounded-full',
    clear:   'inline-block px-2 py-0.5 text-xs font-medium bg-green-100 text-green-700 rounded-full',
    flagged: 'inline-block px-2 py-0.5 text-xs font-medium bg-red-100 text-red-700 rounded-full',
  }[status] ?? 'inline-block px-2 py-0.5 text-xs font-medium bg-gray-100 text-gray-600 rounded-full'
}

function formatDate(iso) {
  if (!iso) return '-'
  return new Date(iso).toLocaleString('ko-KR', { timeZone: 'Asia/Seoul' })
}
</script>

<style scoped>
.th { @apply px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wide; }
.td { @apply px-4 py-3; }
.input { @apply w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

.toast-enter-active, .toast-leave-active { transition: all 0.3s ease; }
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translateY(12px); }
</style>
