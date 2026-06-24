<template>
  <AppLayout>
    <div class="max-w-2xl">
      <h2 class="text-xl font-bold text-gray-900 mb-6">내 정보</h2>

      <div v-if="loading" class="text-sm text-gray-400">불러오는 중...</div>

      <template v-else-if="profile">
        <!-- 기본 정보 카드 -->
        <div class="bg-white rounded-xl border border-gray-200 p-6 mb-6">
          <h3 class="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-3">기본 정보</h3>
          <div class="grid grid-cols-2 gap-4 text-sm">
            <div>
              <p class="text-gray-500 mb-0.5">사번</p>
              <p class="font-medium text-gray-900">{{ profile.employeeId }}</p>
            </div>
            <div>
              <p class="text-gray-500 mb-0.5">권한</p>
              <span :class="profile.role === 'ROLE_ADMIN' ? 'badge-blue' : 'badge-gray'">
                {{ profile.role === 'ROLE_ADMIN' ? '관리자' : '직원' }}
              </span>
            </div>
            <div>
              <p class="text-gray-500 mb-0.5">이름</p>
              <p class="font-medium text-gray-900">{{ profile.firstName }} {{ profile.lastName }}</p>
            </div>
            <div>
              <p class="text-gray-500 mb-0.5">상태</p>
              <span :class="profile.status === 'ACTIVE' ? 'badge-green' : 'badge-red'">
                {{ profile.status === 'ACTIVE' ? '재직' : '퇴사' }}
              </span>
            </div>
            <div>
              <p class="text-gray-500 mb-0.5">부서</p>
              <p class="font-medium text-gray-900">{{ profile.department || '-' }}</p>
            </div>
            <div>
              <p class="text-gray-500 mb-0.5">직책</p>
              <p class="font-medium text-gray-900">{{ profile.position || '-' }}</p>
            </div>
            <div>
              <p class="text-gray-500 mb-0.5">이메일</p>
              <p class="font-medium text-gray-900">{{ profile.email }}</p>
            </div>
            <div>
              <p class="text-gray-500 mb-0.5">입사일</p>
              <p class="font-medium text-gray-900">{{ profile.hireDate || '-' }}</p>
            </div>
          </div>
        </div>

        <!-- 개인 연락처 카드 -->
        <div class="bg-white rounded-xl border border-gray-200 p-6 mb-6">
          <h3 class="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-3">개인 연락처</h3>
          <div class="grid grid-cols-2 gap-4 text-sm">
            <div>
              <p class="text-gray-500 mb-0.5">전화번호</p>
              <p class="font-medium text-gray-900">{{ profile.phone || '-' }}</p>
            </div>
            <div>
              <p class="text-gray-500 mb-0.5">주소</p>
              <p class="font-medium text-gray-900">{{ profile.address || '-' }}</p>
            </div>
            <div>
              <p class="text-gray-500 mb-0.5">비상연락처 이름</p>
              <p class="font-medium text-gray-900">{{ profile.emergencyContactName || '-' }}</p>
            </div>
            <div>
              <p class="text-gray-500 mb-0.5">비상연락처 전화번호</p>
              <p class="font-medium text-gray-900">{{ profile.emergencyContactPhone || '-' }}</p>
            </div>
            <div class="col-span-2">
              <p class="text-gray-500 mb-0.5">메모</p>
              <p class="font-medium text-gray-900 whitespace-pre-wrap">{{ profile.note || '-' }}</p>
            </div>
          </div>
        </div>

        <!-- 수정 폼 -->
        <div class="bg-white rounded-xl border border-gray-200 p-6">
          <h3 class="text-sm font-semibold text-gray-700 mb-4">정보 수정</h3>
          <form @submit.prevent="handleUpdate" class="space-y-4">
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="label">전화번호</label>
                <input v-model="form.phone" type="text" placeholder="010-0000-0000" class="input" />
              </div>
              <div>
                <label class="label">주소</label>
                <input v-model="form.address" type="text" placeholder="서울시 강남구..." class="input" />
              </div>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="label">비상연락처 이름</label>
                <input v-model="form.emergencyContactName" type="text" placeholder="홍길동" class="input" />
              </div>
              <div>
                <label class="label">비상연락처 전화번호</label>
                <input v-model="form.emergencyContactPhone" type="text" placeholder="010-0000-0000" class="input" />
              </div>
            </div>

            <div>
              <label class="label">메모</label>
              <textarea v-model="form.note" rows="3"
                placeholder="자기소개, 특이사항 등 자유롭게 작성하세요."
                class="input resize-none" />
            </div>

            <div class="flex items-center gap-3">
              <button type="submit" :disabled="saving"
                class="px-4 py-2 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-300 text-white text-sm font-medium rounded-lg transition-colors">
                {{ saving ? '저장 중...' : '저장' }}
              </button>
              <p v-if="successMsg" class="text-sm text-green-600">{{ successMsg }}</p>
              <p v-if="errorMsg" class="text-sm text-red-500">{{ errorMsg }}</p>
            </div>
          </form>
        </div>
      </template>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import AppLayout from '../components/AppLayout.vue'
import api from '../api/axios'

const profile = ref(null)
const loading = ref(true)
const saving = ref(false)
const successMsg = ref('')
const errorMsg = ref('')
const form = ref({
  phone: '', address: '', emergencyContactName: '', emergencyContactPhone: '', note: ''
})

onMounted(async () => {
  try {
    const res = await api.get('/me')
    profile.value = res.data.data
    const p = profile.value
    form.value = {
      phone: p.phone || '',
      address: p.address || '',
      emergencyContactName: p.emergencyContactName || '',
      emergencyContactPhone: p.emergencyContactPhone || '',
      note: p.note || ''
    }
  } finally {
    loading.value = false
  }
})

async function handleUpdate() {
  saving.value = true
  successMsg.value = ''
  errorMsg.value = ''
  try {
    const res = await api.patch('/me', form.value)
    profile.value = res.data.data
    successMsg.value = '저장되었습니다.'
  } catch (err) {
    const fields = err.response?.data?.errors
    errorMsg.value = fields?.length
      ? fields.map(f => f.message).join(' / ')
      : (err.response?.data?.message || '저장에 실패했습니다.')
  } finally {
    saving.value = false
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
