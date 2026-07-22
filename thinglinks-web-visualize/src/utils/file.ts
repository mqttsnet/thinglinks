/**
 * * base64转file
 * @param dataurl 
 * @param fileName 
 * @returns 
 */
export const base64toFile = (dataurl: string, fileName: string) => {
  let dataArr = dataurl.split(","),
  mime = (dataArr as any[])[0].match(/:(.*?);/)[1],
  bstr = atob(dataArr[1]),
  n = bstr.length,
  u8arr = new Uint8Array(n);
  while (n--) {
    u8arr[n] = bstr.charCodeAt(n);
  }
  return new File([u8arr], fileName, { type: mime });
}

/**
 * * file转url
 */
 export const fileToUrl = (file: File): string => {
  const Url = URL || window.URL || window.webkitURL
  const ImageUrl = Url.createObjectURL(file)
  return ImageUrl
}

/**
 * * url转file
 */
 export const urlToFile = (fileUrl: string, fileName = `${new Date().getTime()}`): File => {
  const dataArr = fileUrl.split(',')
  const mime = (dataArr as any[])[0].match(/:(.*);/)[1]
  const originStr = atob(dataArr[1])
  return new File([originStr], `${fileName}`, { type: mime })
}

/**
 * * file转base64
 * @param file 文件数据
 * @param callback 回调函数 
 */
export const fileTobase64 = (file: File, callback: Function) => {
  let reader = new FileReader()
  reader.readAsDataURL(file)
  reader.onload = function (e: ProgressEvent<FileReader>) {
    if (e.target) {
      let base64 = e.target.result
      callback(base64)
    }
  }
}

/**
 * * canvas转file
 * @param canvas 
 */
export const canvastoFile = (canvas: HTMLCanvasElement, name?: string) => {
  const dataurl = canvas.toDataURL('image/png')
  return urlToFile(dataurl, name)
}

/**
 * *获取上传的文件数据
 * @param { File } file 文件对象
 */
export const readFile = (file: File) => {
  return new Promise((resolve: Function) => {
    try {
      const reader = new FileReader()
      reader.onload = (evt: ProgressEvent<FileReader>) => {
        if (evt.target) {
          resolve(evt.target.result)
        }
      }
      reader.readAsText(file)
    } catch (error) {
      window['$message'].error('文件读取失败！')
    }
  })
}

/**
 * * 通过 a 标签下载数据
 * @param url 
 * @param filename 
 * @param fileSuffix 
 */
export const downloadByA = (url: string, filename = new Date().getTime(), fileSuffix?: string) => {
  const ele = document.createElement('a') // 创建下载链接
  ele.download = `${filename}.${fileSuffix}` //设置下载的名称
  ele.style.display = 'none' // 隐藏的可下载链接
  // 字符内容转变成blob地址
  ele.href = url
  // 绑定点击时间
  document.body.appendChild(ele)
  ele.click()
  // 然后移除
  document.body.removeChild(ele)
}

/**
 * * 下载数据
 * @param { string } content 数据内容
 * @param { ?string } filename 文件名称（默认随机字符）
 * @param { ?string } fileSuffix 文件名称（默认随机字符）
 */
export const downloadTextFile = (
  content: string,
  filename = new Date().getTime(),
  fileSuffix?: string
) => {
  // 字符内容转变成blob地址
  const blob = new Blob([content])
  downloadByA(URL.createObjectURL(blob), filename, fileSuffix)
}

/**
 * @description: base64 to blob
 */
export const dataURLToBlob = (dataURL: string) => {
  const parts = dataURL.split(',');
  const mime = parts[0]?.match(/:(.*?);/)?.[1];
  if (!mime || !parts[1]) {
    throw new Error('Invalid data URL')
  }
  const bstr = atob(parts[1]);
  let n = bstr.length;
  const u8arr = new Uint8Array(n);

  while (n--) {
    u8arr[n] = bstr.charCodeAt(n);
  }

  return new Blob([u8arr], { type: mime });
}
