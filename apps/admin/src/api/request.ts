import axios, { type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

export interface RuoYiResponse<T = unknown> {
  code: number
  data: T
  msg: string
}

export interface PageResult<T> {
  rows: T[]
  total: number
}

const TOKEN_KEY = 'admin_token'

/** RuoYi PC 客户端 ID，请求头需与登录时一致，否则会判定登录状态异常 */
export const CLIENT_ID = 'e5cd7e4891bf95d1d19206ce24a7b32e'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
}

const instance = axios.create({
  baseURL: '/',
  timeout: 30000,
})

instance.interceptors.request.use((config) => {
  config.headers.Clientid = CLIENT_ID
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

instance.interceptors.response.use(
  (response) => response,
  (error) => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  },
)

async function request<T>(config: AxiosRequestConfig): Promise<T> {
  const response = await instance(config)
  const res = response.data as RuoYiResponse<T>
  if (res.code === 200) {
    return res.data
  }
  ElMessage.error(res.msg || '请求失败')
  if (res.code === 401) {
    removeToken()
    router.push('/login')
  }
  return Promise.reject(new Error(res.msg || '请求失败'))
}

export default {
  get<T>(url: string, config?: AxiosRequestConfig) {
    return request<T>({ ...config, method: 'GET', url })
  },
  post<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return request<T>({ ...config, method: 'POST', url, data })
  },
  put<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return request<T>({ ...config, method: 'PUT', url, data })
  },
  delete<T>(url: string, config?: AxiosRequestConfig) {
    return request<T>({ ...config, method: 'DELETE', url })
  },
}
