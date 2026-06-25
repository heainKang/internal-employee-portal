<template>
  <AppLayout>
    <div class="max-w-4xl">
      <h2 class="text-xl font-bold text-gray-900 mb-6">배경 조회</h2>

      <!-- 직원 선택 + 새 요청 버튼 -->
      <div class="flex gap-3 mb-6">
        <select v-model="selectedEmployeeId" class="input flex-1">
          <option value="">직원을 선택하세요</option>
          <option v-for="emp in employees" :key="emp.employeeId" :value="emp.employeeId">
            {{ emp.firstName }} {{ emp.lastName }} ({{ emp.employeeId }})
            {{ emp.department ? '· ' + emp.department : '' }}
          </option>
        </select>
        <button @click="showRequestModal = true" :disabled="!selectedEmployeeId"
          class="px-4 py-2 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-300 text-white text-sm font-medium rounded-lg transition-colors whitespace-nowrap">
          + 새 배경조회 요청
        </button>
      </div>

      <!-- 이력 테이블 -->
      <div class="bg-white rounded-xl border border-gray-200 overflow-hidden">
        <div v-if="!selectedEmployeeId" class="text-center text-sm text-gray-400 py-14">
          직원을 선택하면 조회 이력이 표시됩니다.
        </div>
        <div v-else-if="historyLoading" class="text-center text-sm text-gray-400 py-14">
          불러오는 중...
        </div>
        <div v-else-if="history.length === 0" class="text-center text-sm text-gray-400 py-14">
          조회 이력이 없습니다.
        </div>
        <div v-else class="overflow-x-auto">
          <table class="w-full text-sm min-w-[520px]">
            <thead class="bg-gray-50 border-b border-gray-200">
              <tr>
                <th class="th">상태</th>
                <th class="th">요청일시</th>
                <th class="th">완료일시</th>
                <th class="th text-center">상세</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-100">
              <tr v-for="item in history" :key="item.checkId"
                @click="openDetail(item)"
                :class="item.status !== 'pending'
                  ? 'hover:bg-gray-50 cursor-pointer'
                  : 'cursor-default'"
                class="transition-colors">
                <td class="td">
                  <span v-if="item.status === 'pending'"
                    class="inline-flex items-center gap-1.5 px-2 py-0.5 text-xs font-medium bg-yellow-100 text-yellow-700 rounded-full">
                    <span class="w-3 h-3 border-2 border-yellow-500 border-t-transparent rounded-full animate-spin shrink-0"></span>
                    조회 중
                  </span>
                  <span v-else-if="item.status === 'clear'"
                    class="inline-block px-2 py-0.5 text-xs font-medium bg-green-100 text-green-700 rounded-full">
                    이상 없음
                  </span>
                  <span v-else-if="item.status === 'flagged'"
                    class="inline-block px-2 py-0.5 text-xs font-medium bg-red-100 text-red-700 rounded-full">
                    이슈 있음
                  </span>
                  <span v-else class="text-gray-400 text-xs">{{ item.status }}</span>
                </td>
                <td class="td text-gray-600 text-xs">{{ formatDate(item.createdAt) }}</td>
                <td class="td text-gray-600 text-xs">{{ item.completedAt ? formatDate(item.completedAt) : '-' }}</td>
                <td class="td text-center">
                  <span v-if="item.status === 'pending'" class="text-xs text-gray-400">진행 중</span>
                  <span v-else class="text-xs font-medium text-blue-600">보기 →</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 폴링 상태 안내 -->
      <p v-if="hasPending && selectedEmployeeId" class="mt-2 text-xs text-gray-400 text-right">
        ↻ 조회 중인 항목이 있어 30초마다 자동 갱신됩니다.
      </p>
    </div>

    <!-- ── Toast ─────────────────────────────────────────────────────────── -->
    <div class="fixed bottom-4 right-4 z-50 space-y-2 w-80 pointer-events-none">
      <TransitionGroup name="toast">
        <div v-for="t in toasts" :key="t.id"
          class="flex items-start gap-2 px-4 py-3 rounded-lg shadow-lg text-sm text-white pointer-events-auto"
          :class="{
            'bg-red-500':   t.type === 'error',
            'bg-blue-500':  t.type === 'info',
            'bg-green-500': t.type === 'success',
          }">
          <span class="flex-1 leading-snug">{{ t.message }}</span>
          <button @click="removeToast(t.id)"
            class="opacity-70 hover:opacity-100 text-base leading-none mt-0.5 shrink-0">✕</button>
        </div>
      </TransitionGroup>
    </div>

    <!-- ── 새 요청 모달 ──────────────────────────────────────────────────── -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="showRequestModal"
          class="fixed inset-0 bg-black/50 z-40 flex items-center justify-center p-4"
          @click.self="showRequestModal = false">
          <div class="bg-white rounded-xl shadow-xl w-full max-w-md p-6">
            <h3 class="text-base font-semibold text-gray-900 mb-4">새 배경조회 요청</h3>

            <!-- 선택 직원 정보 -->
            <div class="bg-gray-50 rounded-lg p-4 mb-5 text-sm space-y-2">
              <div class="flex justify-between">
                <span class="text-gray-500">이름</span>
                <span class="font-medium text-gray-900">
                  {{ selectedEmployee?.firstName }} {{ selectedEmployee?.lastName }}
                </span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-500">사번</span>
                <span class="font-mono text-gray-900">{{ selectedEmployee?.employeeId }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-500">부서</span>
                <span class="font-medium text-gray-900">{{ selectedEmployee?.department || '-' }}</span>
              </div>
            </div>

            <p class="text-xs text-gray-500 mb-5">
              위 직원에 대해 배경조회를 요청합니다. 조회 결과는 수 분 내 확인할 수 있습니다.
            </p>

            <div class="flex gap-3 justify-end">
              <button @click="showRequestModal = false" :disabled="requesting"
                class="px-4 py-2 border border-gray-300 text-gray-700 text-sm font-medium rounded-lg hover:bg-gray-50 disabled:opacity-40">
                취소
              </button>
              <button @click="requestCheck" :disabled="requesting"
                class="px-4 py-2 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-300 text-white text-sm font-medium rounded-lg transition-colors">
                {{ requesting ? '요청 중...' : '요청' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- ── 상세 모달 ─────────────────────────────────────────────────────── -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="showDetailModal"
          class="fixed inset-0 bg-black/50 z-40 flex items-center justify-center p-4"
          @click.self="showDetailModal = false">
          <div class="bg-white rounded-xl shadow-xl w-full max-w-md p-6">
            <div class="flex items-center justify-between mb-5">
              <h3 class="text-base font-semibold text-gray-900">배경조회 상세 결과</h3>
              <button @click="showDetailModal = false"
                class="text-gray-400 hover:text-gray-600 text-xl leading-none">✕</button>
            </div>

            <div v-if="detailLoading" class="text-center text-sm text-gray-400 py-10">
              불러오는 중...
            </div>

            <div v-else-if="detail" class="space-y-4">
              <!-- 상태 -->
              <div class="flex items-center justify-between">
                <span class="text-sm text-gray-500">최종 상태</span>
                <span :class="detail.status === 'clear'
                    ? 'bg-green-100 text-green-700'
                    : 'bg-red-100 text-red-700'"
                  class="px-2.5 py-0.5 text-xs font-medium rounded-full">
                  {{ detail.status === 'clear' ? '이상 없음' : '이슈 있음' }}
                </span>
              </div>

              <!-- 결과 4개 카드 -->
              <div class="grid grid-cols-2 gap-3">
                <div class="bg-gray-50 rounded-lg p-3">
                  <p class="text-xs text-gray-500 mb-1">범죄 이력</p>
                  <p class="text-sm font-semibold"
                    :class="detail.criminalRecord === true ? 'text-red-600'
                           : detail.criminalRecord === false ? 'text-green-600'
                           : 'text-gray-400'">
                    {{ detail.criminalRecord !== undefined && detail.criminalRecord !== null
                        ? (detail.criminalRecord ? '있음' : '없음') : '-' }}
                  </p>
                </div>
                <div class="bg-gray-50 rounded-lg p-3">
                  <p class="text-xs text-gray-500 mb-1">학력 검증</p>
                  <p class="text-sm font-semibold"
                    :class="detail.educationVerified === true ? 'text-green-600'
                           : detail.educationVerified === false ? 'text-red-600'
                           : 'text-gray-400'">
                    {{ detail.educationVerified !== undefined && detail.educationVerified !== null
                        ? (detail.educationVerified ? '확인' : '미확인') : '-' }}
                  </p>
                </div>
                <div class="bg-gray-50 rounded-lg p-3">
                  <p class="text-xs text-gray-500 mb-1">경력 검증</p>
                  <p class="text-sm font-semibold"
                    :class="detail.employmentVerified === true ? 'text-green-600'
                           : detail.employmentVerified === false ? 'text-red-600'
                           : 'text-gray-400'">
                    {{ detail.employmentVerified !== undefined && detail.employmentVerified !== null
                        ? (detail.employmentVerified ? '확인' : '미확인') : '-' }}
                  </p>
                </div>
                <div class="bg-gray-50 rounded-lg p-3">
                  <p class="text-xs text-gray-500 mb-1">신용 점수</p>
                  <p class="text-sm font-semibold text-gray-900">{{ detail.creditScore || '-' }}</p>
                </div>
              </div>

              <!-- 날짜 / Check ID -->
              <div class="border-t border-gray-100 pt-3 space-y-1.5 text-sm">
                <div class="flex justify-between">
                  <span class="text-gray-500">요청일시</span>
                  <span class="text-gray-700 text-xs">{{ formatDate(detail.createdAt) }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-gray-500">완료일시</span>
                  <span class="text-gray-700 text-xs">{{ detail.completedAt ? formatDate(detail.completedAt) : '-' }}</span>
                </div>
                <div class="flex justify-between items-center">
                  <span class="text-gray-500">Check ID</span>
                  <span class="font-mono text-xs text-gray-500">{{ detail.checkId }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </AppLayout>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import AppLayout from '../../components/AppLayout.vue'
import api from '../../api/axios'

// ── State ─────────────────────────────────────────────────────────────────────

const employees      = ref([])
const selectedEmployeeId = ref('')
const history        = ref([])
const historyLoading = ref(false)

const showRequestModal = ref(false)
const requesting       = ref(false)

const showDetailModal = ref(false)
const detailLoading   = ref(false)
const detail          = ref(null)

const toasts = ref([])
let   toastSeq = 0
let   pollTimer = null

// ── Computed ───────────────────────────────────────────────────────────────────

const selectedEmployee = computed(() =>
  employees.value.find(e => e.employeeId === selectedEmployeeId.value)
)

const hasPending = computed(() =>
  history.value.some(h => h.status === 'pending')
)

// ── Watchers ───────────────────────────────────────────────────────────────────

watch(selectedEmployeeId, (newId) => {
  stopPolling()
  history.value = []
  detail.value  = null
  if (newId) fetchHistory()
})

watch(hasPending, (val) => {
  if (val) startPolling()
  else     stopPolling()
})

// ── Lifecycle ──────────────────────────────────────────────────────────────────

onMounted(async () => {
  try {
    const res = await api.get('/admin/employees', {
      params: { size: 200, sortBy: 'firstName', direction: 'asc' }
    })
    employees.value = res.data.data.content.filter(e => e.status === 'ACTIVE')
  } catch (err) {
    showToast('직원 목록을 불러오지 못했습니다.')
  }
})

onUnmounted(() => stopPolling())

// ── Polling ────────────────────────────────────────────────────────────────────

function startPolling() {
  if (pollTimer) return
  pollTimer = setInterval(() => fetchHistory(false), 30_000)
}

function stopPolling() {
  if (!pollTimer) return
  clearInterval(pollTimer)
  pollTimer = null
}

// ── API ────────────────────────────────────────────────────────────────────────

async function fetchHistory(showLoading = true) {
  if (!selectedEmployeeId.value) return
  if (showLoading) historyLoading.value = true
  try {
    const res = await api.get('/admin/background-checks', {
      params: { employeeId: selectedEmployeeId.value }
    })
    history.value = res.data.data?.checks || []
  } catch (err) {
    if (showLoading) showToast(formatApiError(err, '이력 조회에 실패했습니다.'))
    // 폴링 중 에러는 무시 (30초 후 재시도)
  } finally {
    if (showLoading) historyLoading.value = false
  }
}

async function requestCheck() {
  requesting.value = true
  try {
    const res = await api.post('/admin/background-checks', {
      employeeId: selectedEmployeeId.value
    })
    showRequestModal.value = false
    // pending 항목을 맨 앞에 추가 → watch(hasPending) 가 폴링 시작
    history.value = [res.data.data, ...history.value]
  } catch (err) {
    showToast(formatApiError(err, '배경조회 요청에 실패했습니다.'))
  } finally {
    requesting.value = false
  }
}

async function openDetail(item) {
  if (item.status === 'pending') {
    showToast('조사가 진행 중입니다. 완료 후 확인해 주세요.', 'info')
    return
  }
  showDetailModal.value = true
  detailLoading.value   = true
  detail.value          = null
  try {
    const res = await api.get(`/admin/background-checks/${item.checkId}`)
    detail.value = res.data.data
  } catch (err) {
    showToast(formatApiError(err, '상세 조회에 실패했습니다.'))
    showDetailModal.value = false
  } finally {
    detailLoading.value = false
  }
}

// ── Toast ──────────────────────────────────────────────────────────────────────

function showToast(message, type = 'error') {
  const id = ++toastSeq
  toasts.value.push({ id, message, type })
  setTimeout(() => removeToast(id), 4000)
}

function removeToast(id) {
  toasts.value = toasts.value.filter(t => t.id !== id)
}

// ── Helpers ────────────────────────────────────────────────────────────────────

const ERROR_SUFFIX = {
  X001: '서비스 불가',
  X002: '타임아웃',
  X003: '500 서버 오류',
  X004: '결과 없음',
  X005: 'CB OPEN',
}

function formatApiError(err, fallback) {
  const code   = err.response?.data?.code
  const msg    = err.response?.data?.message || fallback
  const suffix = ERROR_SUFFIX[code]
  return suffix ? `${msg} (${suffix})` : msg
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

/* Toast */
.toast-enter-active { transition: all 0.25s ease; }
.toast-leave-active { transition: all 0.2s ease; }
.toast-enter-from,
.toast-leave-to     { opacity: 0; transform: translateX(16px); }

/* Modal */
.modal-enter-active { transition: all 0.2s ease; }
.modal-leave-active { transition: all 0.15s ease; }
.modal-enter-from,
.modal-leave-to     { opacity: 0; }
.modal-enter-from :deep(.bg-white),
.modal-leave-to :deep(.bg-white) { transform: scale(0.97); }
</style>
