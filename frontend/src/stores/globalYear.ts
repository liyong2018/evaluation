import { ref } from 'vue'
import { defineStore } from 'pinia'

export const useGlobalYearStore = defineStore('globalYear', () => {
  // 从 localStorage 读取年份，默认为当前年份
  const STORAGE_KEY = 'evaluation_selected_year'

  const getStoredYear = (): number => {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored) {
      const year = parseInt(stored, 10)
      if (!isNaN(year) && year >= 2020 && year <= 2030) {
        return year
      }
    }
    return new Date().getFullYear()
  }

  const selectedYear = ref<number>(getStoredYear())

  // 设置年份
  function setYear(year: number) {
    selectedYear.value = year
    localStorage.setItem(STORAGE_KEY, String(year))
  }

  return {
    selectedYear,
    setYear
  }
})
