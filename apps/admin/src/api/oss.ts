import axios from 'axios'
import { CLIENT_ID, getToken } from '@/api/request'

export interface OssUploadResult {
  url: string
  fileName: string
  ossId: string
}

export async function uploadOss(file: File): Promise<OssUploadResult> {
  const form = new FormData()
  form.append('file', file)
  const { data } = await axios.post<{ code: number; data: OssUploadResult; msg: string }>(
    '/resource/oss/upload',
    form,
    {
      headers: {
        Clientid: CLIENT_ID,
        Authorization: getToken() ? `Bearer ${getToken()}` : '',
        'Content-Type': 'multipart/form-data',
      },
    },
  )
  if (data.code !== 200) throw new Error(data.msg || '上传失败')
  return data.data
}
