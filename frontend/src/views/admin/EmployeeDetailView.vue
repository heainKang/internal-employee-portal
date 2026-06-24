<template>
  <AppLayout>
    <div class="max-w-2xl">
      <div class="flex items-center gap-3 mb-6">
        <button @click="$router.back()" class="text-gray-400 hover:text-gray-600">←</button>
        <h2 class="text-xl font-bold text-gray-900">직원 상세</h2>
      </div>

      <div v-if="loading" class="text-sm text-gray-400">불러오는 중...</div>

      <template v-else-if="employee">
        <!-- 기본 정보 -->
        <div class="bg-white rounded-xl border border-gray-200 p-6 mb-4">
          <div class="flex items-start justify-between mb-4">
            <div>
              <p class="text-lg font-semibold text-gray-900">{{ employee.firstName }} {{ employee.lastName }}</p>
              <p class="text-sm text-gray-500">{{ employee.employeeId }}</p>
            </div>
            <div class="flex gap-2">
              <span :class="employee.role === 'ROLE_ADMIN' ? 'badge-blue' : 'badge-gray'">
                {{ employee.role === 'ROLE_ADMIN' ? '관리자' : '직원' }}
              </span>
              <span :class="employee.status === 'ACTIVE' ? 'badge-green' : 'badge-red'">
                {{ employee.status === 'ACTIVE' ? '재직' : '퇴사' }}
              </span>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-3 text-sm">
            <div>
              <p class="text-gray-500">이메일</p>
              <p class="font-medium text-gray-900">{{ employee.email }}</p>
            </div>
            <div>
              <p class="text-gray-500">전화번호</p>
              <p class="font-medium text-gray-900">{{ employee.phone || '-' }}</p>
            </div>
            <div>
              <p class="text-gray-500">부서</p>
              <p class="font-medium text-gray-900">{{ employee.department || '-' }}</p>
            </div>
            <div>
              <p class="text-gray-500">직책</p>
              <p class="font-medium text-gray-900">{{ employee.position || '-' }}</p>
            </div>
            <div>
              <p class="text-gray-500">생년월일</p>
              <p class="font-medium text-gray-900">{{ employee.dateOfBirth || '-' }}</p>
            </div>
            <div>
              <p class="text-gray-500">입사일</p>
              <p class="font-medium text-gray-900">{{ employee.hireDate || '-' }}</p>
            </div>
          </div>

          <!-- 개인 연락처 (직원이 등록한 정보) -->
          <div class="mt-4 pt-4 border-t border-gray-100 grid grid-cols-2 gap-3 text-sm">
            <p class="col-span-2 text-xs font-semibold text-gray-400 uppercase tracking-wide">개인 연락처</p>
            <div>
              <p class="text-gray-500">주소</p>
              <p class="font-medium text-gray-900">{{ employee.address || '-' }}</p>
            </div>
            <div>
              <p class="text-gray-500">비상연락처 이름</p>
              <p class="font-medium text-gray-900">{{ employee.emergencyContactName || '-' }}</p>
            </div>
            <div>
              <p class="text-gray-500">비상연락처 전화번호</p>
              <p class="font-medium text-gray-900">{{ employee.emergencyContactPhone || '-' }}</p>
            </div>
            <div>
              <p class="text-gray-500">메모</p>
              <p class="font-medium text-gray-900 whitespace-pre-wrap">{{ employee.note || '-' }}</p>
            </div>
          </div>
        </div>

        <!-- 관리자 수정 폼 -->
        <div class="bg-white rounded-xl border border-gray-200 p-6 mb-4">
          <h3 class="text-sm font-semibold text-gray-700 mb-4">정보 수정</h3>
          <form @submit.prevent="handleUpdate" class="space-y-4">
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="label">부서</label>
                <input v-model="editForm.department" class="input" />
              </div>
              <div>
                <label class="label">직책</label>
                <input v-model="editForm.position" class="input" />
              </div>
            </div>
            <div>
              <label class="label">권한</label>
              <select v-model="editForm.role" class="input">
                <option value="ROLE_USER">직원</option>
                <option value="ROLE_ADMIN">관리자</option>
              </select>
            </div>
            <div class="flex items-center gap-3">
              <button type="submit" :disabled="saving"
                class="px-4 py-2 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-300 text-white text-sm font-medium rounded-lg transition-colors">
                {{ saving ? '저장 중...' : '저장' }}
              </button>
              <p v-if="updateMsg" class="text-sm text-green-600">{{ updateMsg }}</p>
              <p v-if="updateError" class="text-sm text-red-500">{{ updateError }}</p>
            </div>
          </form>
        </div>

        <!-- 퇴사 처리 -->
        <div v-if="employee.status === 'ACTIVE'"
          class="bg-white rounded-xl border border-red-200 p-6">
          <h3 class="text-sm font-semibold text-red-600 mb-2">퇴사 처리</h3>
          <p class="text-sm text-gray-500 mb-4">퇴사 처리 즉시 해당 계정의 모든 접근이 차단됩니다.</p>
          <button v-if="!confirmResign" @click="confirmResign = true"
            class="px-4 py-2 border border-red-300 text-red-600 text-sm font-medium rounded-lg hover:bg-red-50 transition-colors">
            퇴사 처리
          </button>
          <div v-else class="flex items-center gap-3">
            <p class="text-sm font-medium text-red-600">정말 퇴사 처리하시겠습니까?</p>
            <button @click="handleResign" :disabled="resigning"
              class="px-4 py-2 bg-red-600 hover:bg-red-700 disabled:bg-red-300 text-white text-sm font-medium rounded-lg transition-colors">
              {{ resigning ? '처리 중...' : '확인' }}
            </button>
            <button @click="confirmResign = false"
              class="px-4 py-2 border border-gray-300 text-gray-600 text-sm font-medium rounded-lg hover:bg-gray-50 transition-colors">
              취소
            </button>
          </div>
        </div>
      </template>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppLayout from '../../components/AppLayout.vue'
import api from '../../api/axios'

const route = useRoute()
const router = useRouter()
const employee = ref(null)
const loading = ref(true)
const saving = ref(false)
const resigning = ref(false)
const confirmResign = ref(false)
const updateMsg = ref('')
const updateError = ref('')
const editForm = ref({ department: '', position: '', role: 'ROLE_USER' })

onMounted(async () => {
  try {
    const res = await api.get(`/admin/employees/${route.params.id}`)
    employee.value = res.data.data
    editForm.value = {
      department: employee.value.department || '',
      position: employee.value.position || '',
      role: employee.value.role
    }
  } finally {
    loading.value = false
  }
})

async function handleUpdate() {
  saving.value = true
  updateMsg.value = ''
  updateError.value = ''
  try {
    const res = await api.patch(`/admin/employees/${route.params.id}`, editForm.value)
    employee.value = res.data.data
    updateMsg.value = '저장되었습니다.'
  } catch (err) {
    updateError.value = err.response?.data?.message || '저장에 실패했습니다.'
  } finally {
    saving.value = false
  }
}

async function handleResign() {
  resigning.value = true
  try {
    const res = await api.patch(`/admin/employees/${route.params.id}/status`, { status: 'RESIGNED' })
    employee.value = res.data.data
    confirmResign.value = false
  } finally {
    resigning.value = false
  }
}
</script>

<style scoped>
.label { @apply block text-sm font-medium text-gray-700 mb-1; }
.input { @apply w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500; }
.badge-green { @apply inline-block px-2 py-0.5 text-xs font-medium bg-green-100 text-green-700 rounded-full; }
.badge-red { @apply inline-block px-2 py-0.5 text-xs font-medium bg-red-100 text-red-700 rounded-full; }
.badge-blue { @apply inline-block px-2 py-0.5 text-xs font-medium bg-blue-100 text-blue-700 rounded-full; }
.badge-gray { @apply inline-block px-2 py-0.5 text-xs font-medium bg-gray-100 text-gray-600 rounded-full; }
</style>
