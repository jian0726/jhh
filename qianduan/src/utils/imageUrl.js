/**
 * imageUrl.js - 图片地址解析
 *
 * 后端托管图片时，商品/轮播的图片字段可能是三种形式：
 * - 绝对地址：http://localhost:8080/uploads/xxx.png   → 直接使用
 * - 相对地址：/uploads/xxx.png                          → 按 API 源补全（推荐，端口/域名变化无需改数据）
 * - 数据/本地：data:... / /img/...                      → 原样返回
 *
 * 统一用 resolveImageUrl(url) 处理后再交给 <img :src>。
 */

/** API 源（去掉结尾斜杠），用于补全 /uploads 相对路径 */
const API_BASE = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/+$/, '')

/**
 * 把图片字段解析为浏览器可直接访问的地址。
 * @param {string} url 后端返回的图片地址
 * @returns {string} 可直接用于 <img :src> 的地址
 */
export function resolveImageUrl(url) {
  if (!url || typeof url !== 'string') {
    return ''
  }
  const value = url.trim()
  if (!value) {
    return ''
  }
  // 绝对地址、协议相对地址、内联数据、blob：原样返回
  if (/^(https?:)?\/\//i.test(value) || value.startsWith('data:') || value.startsWith('blob:')) {
    return value
  }
  // 后端上传目录的相对路径：按 API 源补全
  if (value.startsWith('/uploads/')) {
    return API_BASE + value
  }
  // 其它相对路径（如前端静态资源 /img/...）：原样返回，由前端自己提供
  return value
}

/**
 * 从商品/轮播数据对象中提取第一张可用的图片地址。
 * 兼容后端的多种字段命名。
 * @param {Object} data 商品或轮播数据
 * @returns {string} 解析后的图片地址，取不到则返回空串
 */
export function pickImageUrl(data) {
  if (!data) {
    return ''
  }
  const candidates = [
    data.coverImageUrl,
    data.tuPianUrl,
    data.imageUrl,
    Array.isArray(data.images) ? data.images[0] : undefined,
  ]
  for (const candidate of candidates) {
    const resolved = resolveImageUrl(candidate)
    if (resolved) {
      return resolved
    }
  }
  return ''
}

/**
 * 从商品数据中收集全部图片地址（用于详情页画廊）。
 * @param {Object} data 商品数据
 * @returns {string[]} 解析后的图片地址数组
 */
export function collectImageUrls(data) {
  if (!data) {
    return []
  }
  const raw = []
  const push = (value) => {
    if (Array.isArray(value)) {
      value.forEach(push)
    } else if (typeof value === 'string' && value.trim()) {
      raw.push(value.trim())
    }
  }
  push(data.images)
  push(data.coverImages)
  push(data.pingZhengImages)
  push(data.coverImageUrl)
  push(data.tuPianUrl)
  push(data.imageUrl)

  const seen = new Set()
  const result = []
  raw.forEach((value) => {
    const resolved = resolveImageUrl(value)
    if (resolved && !seen.has(resolved)) {
      seen.add(resolved)
      result.push(resolved)
    }
  })
  return result
}

export default resolveImageUrl
