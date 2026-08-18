import type { IAuthLoginRes, ICaptcha, IDoubleTokenRes, IUpdateInfo, IUpdatePassword, IUserInfoRes } from './types/login'
import { http } from '@/http/http'

/** RuoYi 客户端 ID（见 server/script/sql/ry_special.sql） */
export const RUOYI_CLIENT_ID = 'special_xcx_client_id'

/**
 * 登录表单
 */
export interface ILoginForm {
  username: string
  password: string
}

/** RuoYi 登录响应 */
interface IRuoYiLoginVo {
  access_token?: string
  refresh_token?: string
  expire_in?: number
  refresh_expire_in?: number
  client_id?: string
  openid?: string
}

/** RuoYi 用户信息响应 */
interface IRuoYiUserInfoVo {
  user: {
    userId: number
    userName: string
    nickName: string
    avatar?: string
  }
  roles: string[]
}

function mapLoginVo(data: IRuoYiLoginVo): IAuthLoginRes {
  if (data.refresh_token) {
    return {
      accessToken: data.access_token || '',
      refreshToken: data.refresh_token,
      accessExpiresIn: data.expire_in || 7200,
      refreshExpiresIn: data.refresh_expire_in || 604800,
    }
  }
  return {
    token: data.access_token || '',
    expiresIn: data.expire_in || 7200,
  }
}

/**
 * 用户登录（密码模式）
 */
export function login(loginForm: ILoginForm) {
  return http.post<IRuoYiLoginVo>('/auth/login', {
    clientId: RUOYI_CLIENT_ID,
    grantType: 'password',
    username: loginForm.username,
    password: loginForm.password,
  }).then(mapLoginVo)
}

/**
 * 刷新token
 */
export function refreshToken(refreshToken: string) {
  return http.post<IDoubleTokenRes>('/auth/refreshToken', { refreshToken })
}

/**
 * 获取用户信息
 */
export function getUserInfo() {
  return http.get<IRuoYiUserInfoVo>('/system/user/getInfo').then((res) => {
    const info: IUserInfoRes = {
      userId: res.user.userId,
      username: res.user.userName,
      nickname: res.user.nickName,
      avatar: res.user.avatar,
      roles: res.roles,
    }
    return info
  })
}

/**
 * 退出登录
 */
export function logout() {
  return http.post<void>('/auth/logout')
}

/**
 * 修改用户信息
 */
export function updateInfo(data: IUpdateInfo) {
  return http.post('/system/user/profile', data)
}

/**
 * 修改用户密码
 */
export function updateUserPassword(data: IUpdatePassword) {
  return http.put('/system/user/profile/updatePwd', data)
}

/**
 * 获取微信登录凭证
 */
export function getWxCode() {
  return new Promise<UniApp.LoginRes>((resolve, reject) => {
    uni.login({
      provider: 'weixin',
      success: res => resolve(res),
      fail: err => reject(new Error(err.errMsg)),
    })
  })
}

/**
 * 微信小程序登录（RuoYi xcx 模式）
 */
export function wxLogin(data: { code: string }) {
  return http.post<IRuoYiLoginVo>('/auth/login', {
    clientId: RUOYI_CLIENT_ID,
    grantType: 'xcx',
    xcxCode: data.code,
    appid: import.meta.env.VITE_WX_APPID,
  }).then(mapLoginVo)
}

/** 兼容旧接口 */
export function getCode() {
  return http.get<ICaptcha>('/auth/code')
}
