/**
 * 用户配置
 * 可以在这里添加、修改用户信息
 */

export interface UserConfig {
  label: string
  value: string
  password: string
  isAdmin?: boolean
}

export const userConfigList: UserConfig[] = [
  {
    label: 'admin',
    value: 'admin',
    password: 'admin@123',
    isAdmin: true
  },
  {
    label: '林老师',
    value: '林老师',
    password: '123456',
    isAdmin: false
  },
  {
    label: '荣老师',
    value: '荣老师',
    password: '123456',
    isAdmin: false
  },
  {
    label: '何春梅',
    value: '何春梅',
    password: '123456',
    isAdmin: false
  }
]

const LOCAL_STORAGE_KEY = 'registered_users'

// 获取所有用户（包括预配置和注册的用户）
function getAllUsers(): UserConfig[] {
  const stored = localStorage.getItem(LOCAL_STORAGE_KEY)
  const registeredUsers: UserConfig[] = stored ? JSON.parse(stored) : []
  return [...userConfigList, ...registeredUsers]
}

// 验证用户密码
export function validateUser(username: string, password: string): UserConfig | null {
  const allUsers = getAllUsers()
  const user = allUsers.find(u => u.value === username)
  if (user && user.password === password) {
    return user
  }
  return null
}

// 获取用户选项（用于下拉框）
export function getUserOptions() {
  const allUsers = getAllUsers()
  return allUsers.map(u => ({ label: u.label, value: u.value }))
}

// 检查用户名是否已存在
export function isUserExists(username: string): boolean {
  const allUsers = getAllUsers()
  return allUsers.some(u => u.value === username)
}

// 注册新用户
export function registerUser(username: string, password: string): boolean {
  if (isUserExists(username)) {
    return false
  }

  const stored = localStorage.getItem(LOCAL_STORAGE_KEY)
  const registeredUsers: UserConfig[] = stored ? JSON.parse(stored) : []

  registeredUsers.push({
    label: username,
    value: username,
    password: password,
    isAdmin: false
  })

  localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(registeredUsers))
  return true
}
