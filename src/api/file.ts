import request from "@/utils/request";

/**
 * 文件上传接口
 * @param {Object} params - 上传参数
 * @param {File} params.file - 要上传的文件对象（图片/视频）
 * @param {string} params.resourceType - 资源类型说明（图片/视频）
 * @returns {Promise<Object>} 上传结果（包含文件访问路径等信息）
 */
export function uploadFile({ file, resourceType }) {
  // 1. 创建FormData对象（适配multipart/form-data格式）
  const formData = new FormData();
  // 2. 添加文件和资源类型参数
  formData.append("file", file);
  formData.append("resourceType", resourceType);

  // 3. 发起POST请求
  return request({
    url: "/file/upload",
    method: "post",
    data: formData,
    // 4. 配置请求头（让浏览器自动处理multipart/form-data的boundary）
    headers: {
      "Content-Type": "multipart/form-data"
    },
  });
}