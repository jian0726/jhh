/**
 * jwt.js - JWT 工具函数
 *
 * 页面刷新后 userInfo 会丢失，需要从 localStorage 里的 token 反解出用户ID，
 * 再调用后端获取完整信息。这里集中处理解析逻辑，避免各页面重复实现。
 */

/**
 * 解析 JWT Payload
 * @param {string} token JWT 令牌
 * @returns {Object|null} payload 对象，失败返回 null
 */
export function parseJwtPayload(token) {
  if (!token || typeof token !== 'string') {
    return null
  }
  try {
    const parts = token.split('.')
    if (parts.length !== 3) {
      return null
    }
    // JWT payload 使用 base64url 编码，需先替换字符，再补全 padding
    let payloadBase64 = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    while (payloadBase64.length % 4 !== 0) {
      payloadBase64 += '='
    }
    const decoded = decodeURIComponent(
      atob(payloadBase64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join(''),
    )
    return JSON.parse(decoded)
  } catch {
    return null
  }
}

/**
 * 从 JWT 中提取用户ID
 * @param {string} token JWT 令牌
 * @returns {string|number|null}
 */
export function extractUserId(token) {
  const payload = parseJwtPayload(token)
  if (!payload) {
    return null
  }
  // 后端 JwtUtil 写入的是 userId
  return payload.userId ?? payload.sub ?? payload.id ?? payload.yongHuId ?? null
}

/**
 * 获取 Token 过期时间戳（秒）
 * @param {string} token JWT 令牌
 * @returns {number|null}
 */
export function getTokenExpiry(token) {
  const payload = parseJwtPayload(token)
  if (!payload) {
    return null
  }
  return payload.exp ?? null
}

/**
 * 检查 Token 是否已过期
 * @param {string} token JWT 令牌
 * @returns {boolean}
 */
export function isTokenExpired(token) {
  const expiry = getTokenExpiry(token)
  if (!expiry) {
    // 没有 exp 字段时不做判断，避免误踢用户
    return false
  }
  return expiry < Math.floor(Date.now() / 1000)
}
