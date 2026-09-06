/**
 * mockConfig.js - Mock模式配置
 *
 * 该文件控制是否使用Mock数据：
 * - USE_MOCK: true时使用Mock数据，false时使用真实接口
 */

/**
 * Mock模式开关
 * true: 使用mock数据，false: 使用真实接口
 * 通过环境变量 VITE_USE_MOCK 控制
 */
export const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true' || false
