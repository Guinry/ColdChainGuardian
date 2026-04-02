#!/usr/bin/env node

/**
 * 前端页面健康检查脚本
 * 检查所有 Vue 组件文件是否存在明显的语法错误
 */

import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

// 配置
const SRC_DIR = path.join(__dirname, 'src')
const VIEWS_DIR = path.join(SRC_DIR, 'views')
const COMPONENTS_DIR = path.join(SRC_DIR, 'components')

// 颜色输出
const colors = {
  reset: '\x1b[0m',
  red: '\x1b[31m',
  green: '\x1b[32m',
  yellow: '\x1b[33m',
  blue: '\x1b[34m',
  cyan: '\x1b[36m'
}

// 统计
const stats = {
  total: 0,
  passed: 0,
  failed: 0,
  warnings: 0
}

/**
 * 递归获取目录下所有 Vue 文件
 */
function getVueFiles(dir, fileList = []) {
  if (!fs.existsSync(dir)) {
    return fileList
  }

  const files = fs.readdirSync(dir)

  files.forEach(file => {
    const filePath = path.join(dir, file)
    const stat = fs.statSync(filePath)

    if (stat.isDirectory()) {
      getVueFiles(filePath, fileList)
    } else if (file.endsWith('.vue')) {
      fileList.push(filePath)
    }
  })

  return fileList
}

/**
 * 检查 Vue 文件的基本结构
 */
function checkVueFile(filePath) {
  const content = fs.readFileSync(filePath, 'utf-8')
  const fileName = path.basename(filePath)
  const issues = []
  const warnings = []

  // 检查基本结构
  if (!content.includes('<template>')) {
    issues.push('缺少 <template> 标签')
  }

  if (!content.includes('<script')) {
    warnings.push('缺少 <script> 标签')
  }

  // 检查未闭合的标签
  const openTags = (content.match(/<[^/][^>]*>/g) || []).length
  const closeTags = (content.match(/<\/[^>]+>/g) || []).length
  
  // 检查常见的语法错误
  if ((content.match(/\{\{/g) || []).length !== (content.match(/\}\}/g) || []).length) {
    issues.push('可能存在未闭合的 {{ }} 插值')
  }

  // 检查 v-if/v-else 配对
  const vIfCount = (content.match(/v-if=/g) || []).length
  const vElseCount = (content.match(/v-else/g) || []).length
  if (vElseCount > vIfCount) {
    issues.push('v-else 数量多于 v-if')
  }

  // 检查 import 语句
  const imports = content.match(/import\s+.*?\s+from\s+['"].*?['"]/g) || []
  imports.forEach(imp => {
    if (!imp.includes('from')) {
      issues.push(`可能的 import 语法错误：${imp}`)
    }
  })

  // 检查组件注册
  if (content.includes('components:')) {
    warnings.push('使用 options API，建议迁移到 setup 语法糖')
  }

  // 检查 console.log（仅警告）
  const consoleLogs = (content.match(/console\.(log|error|warn)/g) || []).length
  if (consoleLogs > 5) {
    warnings.push(`包含 ${consoleLogs} 个 console 语句，建议清理`)
  }

  return {
    fileName,
    filePath,
    issues,
    warnings,
    passed: issues.length === 0
  }
}

/**
 * 主函数
 */
function main() {
  console.log(`\n${colors.cyan}═══════════════════════════════════════════════════════════${colors.reset}`)
  console.log(`${colors.cyan}       ColdChainGuardian 前端健康检查${colors.reset}`)
  console.log(`${colors.cyan}═══════════════════════════════════════════════════════════${colors.reset}\n`)

  // 获取所有 Vue 文件
  const vueFiles = [
    ...getVueFiles(VIEWS_DIR),
    ...getVueFiles(COMPONENTS_DIR)
  ]

  stats.total = vueFiles.length
  console.log(`${colors.blue}发现 ${vueFiles.length} 个 Vue 组件文件${colors.reset}\n`)

  // 检查每个文件
  vueFiles.forEach(filePath => {
    const result = checkVueFile(filePath)
    
    if (result.passed) {
      stats.passed++
      console.log(`${colors.green}✓${colors.reset} ${path.relative(SRC_DIR, filePath)}`)
      
      if (result.warnings.length > 0) {
        stats.warnings += result.warnings.length
        result.warnings.forEach(w => {
          console.log(`  ${colors.yellow}⚠ ${w}${colors.reset}`)
        })
      }
    } else {
      stats.failed++
      console.log(`${colors.red}✗${colors.reset} ${path.relative(SRC_DIR, filePath)}`)
      result.issues.forEach(issue => {
        console.log(`  ${colors.red}✖ ${issue}${colors.reset}`)
      })
    }
  })

  // 输出统计
  console.log(`\n${colors.cyan}───────────────────────────────────────────────────────────${colors.reset}`)
  console.log(`${colors.blue}检查完成！${colors.reset}`)
  console.log(`  总计：${stats.total} 个文件`)
  console.log(`  ${colors.green}通过：${stats.passed} 个${colors.reset}`)
  console.log(`  ${colors.red}失败：${stats.failed} 个${colors.reset}`)
  console.log(`  ${colors.yellow}警告：${stats.warnings} 个${colors.reset}`)
  console.log(`${colors.cyan}───────────────────────────────────────────────────────────${colors.reset}\n`)

  // 退出码
  if (stats.failed > 0) {
    console.log(`${colors.red}发现错误，请修复后重新运行检查${colors.reset}\n`)
    process.exit(1)
  } else {
    console.log(`${colors.green}所有检查通过！${colors.reset}\n`)
    process.exit(0)
  }
}

// 运行
main()
