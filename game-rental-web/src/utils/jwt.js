/**
 * jwt.js - JWT工具函数
 *
 * 提供安全的JWT相关操作，避免在多处重复解析逻辑
 */

/**
 * 安全解析JWT Payload
 * @param {string} token - JWT令牌
 * @returns {Object|null} - 解析后的payload对象，失败返回null
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

    // JWT payload使用base64url编码，需要先替换字符
    const payloadBase64 = parts[1]
      .replace(/-/g, '+')
      .replace(/_/g, '/')

    const decoded = atob(payloadBase64)
    return JSON.parse(decoded)
  } catch {
    return null
  }
}

/**
 * 从JWT中提取用户ID
 * @param {string} token - JWT令牌
 * @returns {string|number|null} - 用户ID，失败返回null
 */
export function extractUserId(token) {
  const payload = parseJwtPayload(token)
  if (!payload) {
    return null
  }

  // 支持多种字段名：sub, userId, id, yongHuId
  return payload.sub || payload.userId || payload.id || payload.yongHuId || null
}

/**
 * 获取Token的过期时间
 * @param {string} token - JWT令牌
 * @returns {number|null} - 过期时间戳（秒），失败返回null
 */
export function getTokenExpiry(token) {
  const payload = parseJwtPayload(token)
  if (!payload) {
    return null
  }

  return payload.exp || null
}

/**
 * 检查Token是否已过期
 * @param {string} token - JWT令牌
 * @returns {boolean} - 是否已过期
 */
export function isTokenExpired(token) {
  const expiry = getTokenExpiry(token)
  if (!expiry) {
    return true
  }

  const currentTime = Math.floor(Date.now() / 1000)
  return expiry < currentTime
}