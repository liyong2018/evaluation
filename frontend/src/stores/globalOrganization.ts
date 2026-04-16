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
  preferredCapacityModel?: 'government' | 'enterprise' | 'social-organization'
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
    if (org) {
      const mergedOrg: OrganizationInfo = { ...org }
      if (
        mergedOrg.preferredCapacityModel === undefined &&
        selectedOrganization.value?.preferredCapacityModel
      ) {
        mergedOrg.preferredCapacityModel = selectedOrganization.value.preferredCapacityModel
      }
      selectedOrganization.value = mergedOrg
      localStorage.setItem(STORAGE_KEY, JSON.stringify(mergedOrg))
    } else {
      selectedOrganization.value = null
      localStorage.removeItem(STORAGE_KEY)
    }
  }

  function setPreferredCapacityModel(mode: 'government' | 'enterprise' | 'social-organization' | null) {
    if (!selectedOrganization.value) return
    const nextOrg: OrganizationInfo = {
      ...selectedOrganization.value,
      preferredCapacityModel: mode ?? undefined
    }
    selectedOrganization.value = nextOrg
    localStorage.setItem(STORAGE_KEY, JSON.stringify(nextOrg))
  }

  // 清空选中的组织机构
  function clearOrganization() {
    selectedOrganization.value = null
    localStorage.removeItem(STORAGE_KEY)
  }

  return {
    selectedOrganization,
    setOrganization,
    setPreferredCapacityModel,
    clearOrganization
  }
})
