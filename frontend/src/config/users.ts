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

// 验证用户密码
export function validateUser(username: string, password: string): UserConfig | null {
  const user = userConfigList.find(u => u.value === username)
  if (user && user.password === password) {
    return user
  }
  return null
}

// 获取用户选项（用于下拉框）
export function getUserOptions() {
  return userConfigList.map(u => ({ label: u.label, value: u.value }))
}
