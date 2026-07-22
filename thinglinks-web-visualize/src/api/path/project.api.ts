import { post } from '@/api/http'
import { httpErrorHandle } from '@/utils'
import { ContentTypeEnum, ModuleTypeEnum } from '@/enums/httpEnum'

interface UploadedFile {
  fileName: string
  fileurl: string
  id: string
}

// * 上传文件
export const uploadFile = async (data: object) => {
  try {
    const res = await post<UploadedFile>(
      `${ModuleTypeEnum.BASEfILE}/file/upload`,
      data,
      ContentTypeEnum.FORM_DATA
    )
    return res
  } catch {
    httpErrorHandle()
  }
}

/*
 * /findUrlFormTenantByIdUsingPOST
 * 通过id查询文件地址
*/
export const getIndexImage = async (data: string[]) => {
  try {
    const res = await post<Record<string, string>>(
      `${ModuleTypeEnum.BASEfILE}/file/findUrlById`,
      data
    )
    return res
  } catch {
    httpErrorHandle()
  }
}
