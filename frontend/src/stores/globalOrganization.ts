import { ref } from 'vue'
import { defineStore } from 'pinia'

export interface OrganizationInfo {
  code: string
  name: string
  level?: number
  parentId?: number | null
  provinceName?: string
  cityName?: string
  countyName?: string
  townshipName?: string
  communityName?: string
}

export const useGlobalOrganizationStore = defineStore('globalOrganization', () => {
  // 从 localStorage 读取选中的组织机构
  const STORAGE_KEY = 'evaluation_selected_organization'

  const getStoredOrganization = (): OrganizationInfo | null => {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored) {
      try {
        return JSON.parse(stored)
      } catch (e) {
        console.warn('Failed to parse stored organization:', e)
      }
    }
    return null
  }

  const selectedOrganization = ref<OrganizationInfo | null>(getStoredOrganization())

  // 设置选中的组织机构
  function setOrganization(org: OrganizationInfo | null) {
    selectedOrganization.value = org
    if (org) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(org))
    } else {
      localStorage.removeItem(STORAGE_KEY)
    }
  }

  // 清空选中的组织机构
  function clearOrganization() {
    selectedOrganization.value = null
    localStorage.removeItem(STORAGE_KEY)
  }

  return {
    selectedOrganization,
    setOrganization,
    clearOrganization
  }
})
