<template>
  <AppLayout>
    <div class="max-w-2xl">
      <div class="flex items-center gap-3 mb-6">
        <button @click="$router.back()" class="text-gray-400 hover:text-gray-600">←</button>
        <h2 class="text-xl font-bold text-gray-900">직원 생성</h2>
      </div>

      <form @submit.prevent="handleCreate" class="bg-white rounded-xl border border-gray-200 p-6 space-y-4">
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="label">이름 (First Name) <span class="text-red-400">*</span></label>
            <input v-model="form.firstName" required class="input" placeholder="Gildong" />
          </div>
          <div>
            <label class="label">성 (Last Name) <span class="text-red-400">*</span></label>
            <input v-model="form.lastName" required class="input" placeholder="Hong" />
          </div>
        </div>

        <div>
          <label class="label">이메일 <span class="text-red-400">*</span></label>
          <input v-model="form.email" type="email" required class="input" placeholder="email@bit.com" />
        </div>

        <div>
          <label class="label">비밀번호 <span class="text-red-400">*</span></label>
          <input v-model="form.password" type="password" required class="input" placeholder="대문자+소문자+숫자+특수문자 포함 8자 이상" />
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="label">부서</label>
            <input v-model="form.department" class="input" placeholder="Engineering" />
          </div>
          <div>
            <label class="label">직책</label>
            <input v-model="form.position" class="input" placeholder="Developer" />
          </div>
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="label">생년월일 <span class="text-red-400">*</span></label>
            <input v-model="form.dateOfBirth" type="date" required class="input" />
          </div>
          <div>
            <label class="label">입사일 <span class="text-red-400">*</span></label>
            <input v-model="form.hireDate" type="date" required class="input" />
          </div>
        </div>

        <div>
          <label class="label">권한 <span class="text-red-400">*</span></label>
          <select v-model="form.role" required class="input">
            <option value="ROLE_USER">직원</option>
            <option value="ROLE_ADMIN">관리자</option>
          </select>
        </div>

        <p v-if="errorMsg" class="text-sm text-red-500">{{ errorMsg }}</p>

        <div class="flex gap-3 pt-2">
          <button type="submit" :disabled="saving"
            class="px-5 py-2 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-300 text-white text-sm font-medium rounded-lg transition-colors">
            {{ saving ? '생성 중...' : '계정 생성' }}
          </button>
          <button type="button" @click="$router.back()"
            class="px-5 py-2 border border-gray-300 text-gray-700 text-sm font-medium rounded-lg hover:bg-gray-50 transition-colors">
            취소
          </button>
        </div>
      </form>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import AppLayout from '../../components/AppLayout.vue'
import api from '../../api/axios'

const router = useRouter()
const saving = ref(false)
const errorMsg = ref('')
const form = ref({
  firstName: '', lastName: '', email: '', password: '',
  department: '', position: '', dateOfBirth: '', hireDate: '',
  role: 'ROLE_USER'
})

async function handleCreate() {
  saving.value = true
  errorMsg.value = ''
  try {
    await api.post('/admin/employees', form.value)
    router.push('/admin/employees')
  } catch (err) {
    const msg = err.response?.data?.message
    const fields = err.response?.data?.errors
    errorMsg.value = fields?.length
      ? fields.map(f => f.message).join(', ')
      : (msg || '생성에 실패했습니다.')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.label { @apply block text-sm font-medium text-gray-700 mb-1; }
.input { @apply w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500; }
</style>
