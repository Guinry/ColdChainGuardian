/**
 * ColdChain Guardian 毕业设计中期检查PPT
 * 使用 PptxGenJS 生成
 *
 * 配色方案：Teal Trust
 *   Primary: 028090 (teal) - 主色，符合冷链主题
 *   Secondary: 00A896 (seafoam) - 辅助色
 *   Accent: 02C39A (mint) - 强调色
 */

const pptx = require('pptxgenjs');
const fs = require('fs');

let pres = new pptx();

// ==================== 配色定义 ====================
const COLOR_PRIMARY = '028090';      // 主色调-青蓝色
const COLOR_SECONDARY = '00A896';    // 辅助色-海草绿
const COLOR_ACCENT = '02C39A';       // 强调色-薄荷绿
const COLOR_DARK = '21295C';         // 深色文本
const COLOR_LIGHT = 'FFFFFF';        // 白色文本
const COLOR_LIGHT_BG = 'F8FAFC';     // 浅色背景

// ==================== 幻灯片 1: 封面 ====================
let slide1 = pres.addSlide();
slide1.background = { color: COLOR_PRIMARY };

// 标题
slide1.addText('ColdChain Guardian', {
    x: 0.5, y: 1.2, w: 9.0, h: 0.8,
    fontSize: 44,
    bold: true,
    color: COLOR_LIGHT,
    align: 'center'
});

// 副标题
slide1.addText('基于大语言模型的冷链仓储安全管理系统', {
    x: 0.5, y: 2.2, w: 9.0, h: 0.5,
    fontSize: 28,
    color: COLOR_LIGHT,
    align: 'center'
});

// 中期检查
slide1.addText('毕 业 设 计 中 期 检 查', {
    x: 0.5, y: 3.2, w: 9.0, h: 0.5,
    fontSize: 32,
    bold: true,
    color: COLOR_LIGHT,
    align: 'center'
});

// 作者信息
slide1.addText('姓\t名：\n学\t号：\n指导教师：\n日\t期：\t2026年4月', {
    x: 2.5, y: 4.0, w: 5.0, h: 1.5,
    fontSize: 18,
    color: COLOR_LIGHT,
    align: 'left',
    lineSpacing: 1.5
});

// ==================== 幻灯片 2: 目录 ====================
let slide2 = pres.addSlide();
slide2.background = { color: COLOR_LIGHT };

slide2.addText('中 期 检 查 目 录', {
    x: 0.5, y: 0.3, w: 9.0, h: 0.5,
    fontSize: 36,
    bold: true,
    color: COLOR_PRIMARY,
    align: 'left'
});

pres.shapes.RECTANGLE
slide2.addShape(pres.shapes.RECTANGLE, {
    x: 0.5, y: 0.85, w: 9.0, h: 0.03,
    fill: { color: COLOR_PRIMARY }
});

const tocItems = [
    '1. 项目概述',
    '2. 已完成工作',
    '3. 后端系统实现',
    '4. 前端Web端实现',
    '5. AI大语言模型集成',
    '6. 微信小程序开发进展',
    '7. 未完成工作',
    '8. 后续工作计划',
    '9. 中期总结'
];

let yStart = 1.2;
tocItems.forEach((item, idx) => {
    slide2.addText(item, {
        x: 1.0, y: yStart + idx * 0.5, w: 8.0, h: 0.4,
        fontSize: 20,
        color: COLOR_DARK
    });
});

// ==================== 幻灯片 3: 项目概述 ====================
let slide3 = pres.addSlide();
slide3.background = { color: COLOR_LIGHT };

slide3.addText('1. 项目概述', {
    x: 0.5, y: 0.3, w: 9.0, h: 0.5,
    fontSize: 32,
    bold: true,
    color: COLOR_PRIMARY
});

slide3.addShape(pres.shapes.RECTANGLE, {
    x: 0.5, y: 0.85, w: 9.0, h: 0.03,
    fill: { color: COLOR_PRIMARY }
});

// 两栏
slide3.addShape(pres.shapes.ROUNDED_RECTANGLE, {
    x: 0.6, y: 1.1, w: 4.3, h: 2.2,
    fill: { color: COLOR_LIGHT_BG },
    line: { color: COLOR_PRIMARY, width: 2 }
});

slide3.addText('🎯 项目背景', {
    x: 0.8, y: 1.2, w: 3.8, h: 0.35,
    fontSize: 20,
    bold: true,
    color: COLOR_PRIMARY
});

const background = [
    '• 冷链物流是食品安全重要保障',
    '• 温湿度异常导致大量损耗',
    '• 传统人工巡检效率低滞后性强',
    '• 海量IoT数据需要智能化分析',
    '• 毕业设计题目要求结合AI'
];

slide3.addText(background.join('\n'), {
    x: 0.8, y: 1.6, w: 3.8, h: 1.5,
    fontSize: 15,
    color: COLOR_DARK,
    lineSpacing: 1.5
});

slide3.addShape(pres.shapes.ROUNDED_RECTANGLE, {
    x: 5.1, y: 1.1, w: 4.3, h: 2.2,
    fill: { color: COLOR_LIGHT_BG },
    line: { color: COLOR_PRIMARY, width: 2 }
});

slide3.addText('💡 研究目标', {
    x: 5.3, y: 1.2, w: 3.8, h: 0.35,
    fontSize: 20,
    bold: true,
    color: COLOR_PRIMARY
});

const objectives = [
    '• 构建完整冷链环境监测体系',
    '• 建立智能预警机制',
    '• 实现告警→工单闭环管理',
    '• 集成大语言模型提供智能分析',
    '• 完成Web端+小程序双端开发'
];

slide3.addText(objectives.join('\n'), {
    x: 5.3, y: 1.6, w: 3.8, h: 1.5,
    fontSize: 15,
    color: COLOR_DARK,
    lineSpacing: 1.5
});

// 系统架构简图
slide3.addText('🏗️ 系统架构', {
    x: 0.6, y: 3.5, w: 8.8, h: 0.3,
    fontSize: 20,
    bold: true,
    color: COLOR_PRIMARY
});

slide3.addText('物联网采集层 → 温湿度传感器 → MQTT协议\n后端服务层 → Spring Boot + MyBatis-Plus + MySQL + Redis\n前端应用层 → Vue 3 Web管理端 + 微信小程序员工端\n智能分析层 → Spring AI + 通义千问大语言模型', {
    x: 0.6, y: 3.9, w: 8.8, h: 0.8,
    fontSize: 14,
    color: COLOR_DARK,
    lineSpacing: 1.4
});

// ==================== 幻灯片 4: 已完成工作 ====================
let slide4 = pres.addSlide();
slide4.background = { color: COLOR_LIGHT };

slide4.addText('2. 已完成工作', {
    x: 0.5, y: 0.3, w: 9.0, h: 0.5,
    fontSize: 32,
    bold: true,
    color: COLOR_PRIMARY
});

slide4.addShape(pres.shapes.RECTANGLE, {
    x: 0.5, y: 0.85, w: 9.0, h: 0.03,
    fill: { color: COLOR_PRIMARY }
});

// 分块展示
const doneList = [
    {
        category: '✅ 项目基础架构',
        items: [
            '✓ 多模块Maven项目结构搭建',
            '✓ 数据库设计与初始化脚本',
            '✓ Spring Boot框架配置',
            '✓ Spring Security + JWT认证'
        ],
        x: 0.6, y: 1.1, w: 4.2, h: 1.3
    },
    {
        category: '✅ 后端业务功能',
        items: [
            '✓ 用户认证与权限管理',
            '✓ 库区树形结构管理',
            '✓ 传感器设备管理',
            '✓ 温湿度数据采集存储'
        ],
        x: 5.1, y: 1.1, w: 4.2, h: 1.3
    },
    {
        category: '✅ 核心业务模块',
        items: [
            '✓ 智能告警系统（四级分析）',
            '✓ 工单闭环管理',
            '✓ 仪表盘KPI统计',
            '✓ RESTful API完整实现'
        ],
        x: 0.6, y: 2.6, w: 4.2, h: 1.3
    },
    {
        category: '✅ 前端Web管理端',
        items: [
            '✓ Vue 3 + Element Plus框架',
            '✓ 所有功能页面开发完成',
            '✓ ECharts数据可视化',
            '✓ Pinia状态管理'
        ],
        x: 5.1, y: 2.6, w: 4.2, h: 1.3
    },
    {
        category: '✅ AI大语言模型',
        items: [
            '✓ Spring AI框架集成',
            '✓ 通义千问接入完成',
            '✓ 流式对话SSE实现',
            '✓ 轻量级RAG上下文注入'
        ],
        x: 0.6, y: 4.1, w: 4.2, h: 1.2
    },
    {
        category: '✅ 微信小程序',
        items: [
            '✓ 基本框架搭建完成',
            '✓ 微信登录绑定完成',
            '✓ 主要页面结构完成',
            '✓ API对接基本完成'
        ],
        x: 5.1, y: 4.1, w: 4.2, h: 1.2
    }
];

doneList.forEach(block => {
    slide4.addText(block.category, {
        x: block.x + 0.1, y: block.y + 0.08, w: block.w - 0.2, h: 0.3,
        fontSize: 18,
        bold: true,
        color: COLOR_PRIMARY
    });
    slide4.addText(block.items.join('\n'), {
        x: block.x + 0.1, y: block.y + 0.4, w: block.w - 0.2, h: block.h - 0.45,
        fontSize: 14,
        color: COLOR_DARK,
        lineSpacing: 1.6
    });
    slide4.addShape(pres.shapes.ROUNDED_RECTANGLE, {
        x: block.x, y: block.y, w: block.w, h: block.h,
        fill: { color: COLOR_LIGHT_BG },
        line: { color: COLOR_SECONDARY, width: 1 }
    });
});

// ==================== 幻灯片 5: 后端系统实现 ====================
let slide5 = pres.addSlide();
slide5.background = { color: COLOR_LIGHT };

slide5.addText('3. 后端系统实现', {
    x: 0.5, y: 0.3, w: 9.0, h: 0.5,
    fontSize: 32,
    bold: true,
    color: COLOR_PRIMARY
});

slide5.addShape(pres.shapes.RECTANGLE, {
    x: 0.5, y: 0.85, w: 9.0, h: 0.03,
    fill: { color: COLOR_PRIMARY }
});

// 分层展示
const backendModules = [
    {
        name: 'controller层',
        desc: '所有核心模块API都已完成\n' +
            '• AuthController - 用户认证\n' +
            '• AreaController - 库区管理\n' +
            '• DeviceController - 设备管理\n' +
            '• AlertController - 告警管理\n' +
            '• WorkOrderController - 工单管理\n' +
            '• AIAssistantController - AI助手',
        x: 0.6, y: 1.1, w: 2.8, h: 2.0
    },
    {
        name: 'service层',
        desc: '完整业务逻辑实现\n' +
            '• 实现所有核心业务算法\n' +
            '• 告警四维分析算法\n' +
            '• 事务保证数据一致性\n' +
            '• 异常处理统一规范\n' +
            '• 接口权限控制',
        x: 3.6, y: 1.1, w: 2.8, h: 2.0
    },
    {
        name: '数据访问层',
        desc: 'MyBatis-Plus集成完成\n' +
            '• Entity实体映射\n' +
            '• Mapper接口定义\n' +
            '• Repository封装\n' +
            '• 合理建立索引\n' +
            '• 外键约束保证完整性',
        x: 6.6, y: 1.1, w: 2.8, h: 2.0
    }
];

backendModules.forEach(mod => {
    slide5.addText(mod.name, {
        x: mod.x + 0.15, y: mod.y + 0.1, w: mod.w - 0.3, h: 0.3,
        fontSize: 17,
        bold: true,
        color: COLOR_PRIMARY
    });
    slide5.addText(mod.desc, {
        x: mod.x + 0.15, y: mod.y + 0.45, w: mod.w - 0.3, h: mod.h - 0.5,
        fontSize: 12,
        color: COLOR_DARK,
        lineSpacing: 1.5
    });
    slide5.addShape(pres.shapes.ROUNDED_RECTANGLE, {
        x: mod.x, y: mod.y, w: mod.w, h: mod.h,
        fill: { color: COLOR_LIGHT_BG },
        line: { color: COLOR_SECONDARY, width: 1 }
    });
});

// 告警分析算法
slide5.addShape(pres.shapes.ROUNDED_RECTANGLE, {
    x: 0.6, y: 3.3, w: 8.8, h: 1.6,
    fill: { color: COLOR_LIGHT_BG },
    line: { color: COLOR_PRIMARY, width: 2 }
});

slide5.addText('🔍 核心算法：智能告警四维分析', {
    x: 0.8, y: 3.4, w: 8.4, h: 0.3,
    fontSize: 18,
    bold: true,
    color: COLOR_PRIMARY
});

const algoItems = [
    '• 趋势分析：基于时间序列统计告警数量，识别异常波动',
    '• 重复告警：按设备类型统计高频告警，定位系统性问题',
    '• 设备健康度：基于告警级别和频次计算综合评分',
            '• 根因分析：基于时间窗口聚类关联告警，快速定位问题源头'
];

slide5.addText(algoItems.join('\n'), {
    x: 0.8, y: 3.8, w: 8.4, h: 0.9,
    fontSize: 14,
    color: COLOR_DARK,
    lineSpacing: 1.5
});

// ==================== 幻灯片 6: 前端Web端实现 ====================
let slide6 = pres.addSlide();
slide6.background = { color: COLOR_LIGHT };

slide6.addText('4. 前端Web管理端实现', {
    x: 0.5, y: 0.3, w: 9.0, h: 0.5,
    fontSize: 32,
    bold: true,
    color: COLOR_PRIMARY
});

slide6.addShape(pres.shapes.RECTANGLE, {
    x: 0.5, y: 0.85, w: 9.0, h: 0.03,
    fill: { color: COLOR_PRIMARY }
});

// 已完成页面表格形式
const pages = [
    { name: 'Dashboard.vue', feature: '仪表盘KPI总览 + 数据可视化', done: '✓' },
    { name: 'LoginPage.vue', feature: '用户登录界面', done: '✓' },
    { name: 'DeviceManagementView.vue', feature: '设备管理CRUD + 阈值配置', done: '✓' },
    { name: 'WarehouseAreaView.vue', feature: '库区树形结构管理', done: '✓' },
    { name: 'MonitorView.vue', feature: '实时监控 + ECharts曲线', done: '✓' },
    { name: 'AlertCenterView.vue', feature: '告警中心 + 智能分析', done: '✓' },
    { name: 'WorkOrderCenterView.vue', feature: '工单中心 + 状态流转', done: '✓' },
    { name: 'AIAssistantView.vue', feature: 'AI助手流式对话界面', done: '✓' },
    { name: 'EmployeeManagement.vue', feature: '员工列表管理', done: '✓' },
    { name: 'ProfileView.vue', feature: '个人信息修改', done: '✓' }
];

// 两列
let leftPages = pages.slice(0, 5);
let rightPages = pages.slice(5);

slide6.addShape(pres.shapes.ROUNDED_RECTANGLE, {
    x: 0.6, y: 1.1, w: 4.3, h: 4.3,
    fill: { color: COLOR_LIGHT_BG },
    line: { color: COLOR_SECONDARY, width: 1 }
});

slide6.addText('📄 已完成页面（左侧）', {
    x: 0.8, y: 1.2, w: 3.9, h: 0.3,
    fontSize: 17,
    bold: true,
    color: COLOR_PRIMARY
});

leftPages.forEach((page, idx) => {
    slide6.addText(`${page.done}  ${page.name}\n    ↳ ${page.feature}`, {
        x: 0.8, y: 1.6 + idx * 0.72, w: 3.9, h: 0.6,
        fontSize: 13,
        color: COLOR_DARK,
        lineSpacing: 1.4
    });
});

slide6.addShape(pres.shapes.ROUNDED_RECTANGLE, {
    x: 5.1, y: 1.1, w: 4.3, h: 4.3,
    fill: { color: COLOR_LIGHT_BG },
    line: { color: COLOR_SECONDARY, width: 1 }
});

slide6.addText('📄 已完成页面（右侧）', {
    x: 5.3, y: 1.2, w: 3.9, h: 0.3,
    fontSize: 17,
    bold: true,
    color: COLOR_PRIMARY
});

rightPages.forEach((page, idx) => {
    slide6.addText(`${page.done}  ${page.name}\n    ↳ ${page.feature}`, {
        x: 5.3, y: 1.6 + idx * 0.72, w: 3.9, h: 0.6,
        fontSize: 13,
        color: COLOR_DARK,
        lineSpacing: 1.4
    });
});

// ==================== 幻灯片 7: AI大语言模型集成 ====================
let slide7 = pres.addSlide();
slide7.background = { color: COLOR_LIGHT };

slide7.addText('5. AI大语言模型集成', {
    x: 0.5, y: 0.3, w: 9.0, h: 0.5,
    fontSize: 32,
    bold: true,
    color: COLOR_PRIMARY
});

slide7.addShape(pres.shapes.RECTANGLE, {
    x: 0.5, y: 0.85, w: 9.0, h: 0.03,
    fill: { color: COLOR_PRIMARY }
});

// 架构图
slide7.addShape(pres.shapes.ROUNDED_RECTANGLE, {
    x: 0.6, y: 1.1, w: 8.8, h: 1.2,
    fill: { color: COLOR_LIGHT_BG },
    line: { color: COLOR_PRIMARY, width: 2 }
});

slide7.addText('🏗️ 集成架构', {
    x: 0.8, y: 1.2, w: 8.4, h: 0.3,
    fontSize: 18,
    bold: true,
    color: COLOR_PRIMARY
});

slide7.addText('前端 → SSE流式请求 → AIAssistantController → AIAssistantService → Spring AI → 通义千问API\n' +
    '                          ↓\n' +
            '会话历史 + 设备/告警业务上下文注入 → 大语言模型 → 流式token输出 → 前端打字机效果', {
    x: 0.8, y: 1.6, w: 8.4, h: 0.5,
    fontSize: 14,
    color: COLOR_DARK,
    lineSpacing: 1.4,
    align: 'center'
});

// 三大特性
const aiFeatures = [
    {
        title: '🌊 流式输出(SSE)',
        desc: 'Server-Sent Events + Flux<String>\n实现打字机效果，提升用户体验',
        x: 0.6, y: 2.5, w: 2.7, h: 1.1
    },
    {
        title: '🔍 轻量级RAG',
        desc: '附件上下文动态注入\n自动查询业务数据\n丰富大模型回答内容',
        x: 3.5, y: 2.5, w: 2.7, h: 1.1
    },
    {
        title: '💾 会话记忆管理',
        desc: '会话+消息分离存储\n历史对话持久化\n支持多会话切换',
        x: 6.4, y: 2.5, w: 3.0, h: 1.1
    }
];

aiFeatures.forEach(f => {
    slide7.addText(f.title, {
        x: f.x + 0.15, y: f.y + 0.08, w: f.w - 0.3, h: 0.28,
        fontSize: 16,
        bold: true,
        color: COLOR_PRIMARY
    });
    slide7.addText(f.desc, {
        x: f.x + 0.15, y: f.y + 0.4, w: f.w - 0.3, h: f.h - 0.45,
        fontSize: 13,
        color: COLOR_DARK,
        lineSpacing: 1.4
    });
    slide7.addShape(pres.shapes.ROUNDED_RECTANGLE, {
        x: f.x, y: f.y, w: f.w, h: f.h,
        fill: { color: COLOR_LIGHT_BG },
        line: { color: COLOR_SECONDARY, width: 1 }
    });
});

// 解决的问题
slide7.addShape(pres.shapes.ROUNDED_RECTANGLE, {
    x: 0.6, y: 3.8, w: 8.8, h: 1.1,
    fill: { color: COLOR_LIGHT_BG },
    line: { color: COLOR_SECONDARY, width: 1 }
});

slide7.addText('✅ 已解决的集成问题', {
    x: 0.8, y: 3.9, w: 8.4, h: 0.25,
    fontSize: 16,
    bold: true,
    color: COLOR_PRIMARY
});

const problems = [
    '1. Spring AI版本依赖冲突 → 使用稳定里程碑版本，通过BOM统一管理依赖',
    '2. SSE流式输出403错误 → SecurityConfig配置DispatcherType.ASYNC.permitAll绕过认证',
            '3. API兼容性问题 → 适配Spring AI 1.0.0-M1标准API，保证稳定运行'
];

slide7.addText(problems.join('\n'), {
    x: 0.8, y: 4.2, w: 8.4, h: 0.6,
    fontSize: 13,
    color: COLOR_DARK,
    lineSpacing: 1.5
});

// ==================== 幻灯片 8: 微信小程序开发进展 ====================
let slide8 = pres.addSlide();
slide8.background = { color: COLOR_LIGHT };

slide8.addText('6. 微信小程序开发进展', {
    x: 0.5, y: 0.3, w: 9.0, h: 0.5,
    fontSize: 32,
    bold: true,
    color: COLOR_PRIMARY
});

slide8.addShape(pres.shapes.RECTANGLE, {
    x: 0.5, y: 0.85, w: 9.0, h: 0.03,
    fill: { color: COLOR_PRIMARY }
});

// 已完成
slide8.addShape(pres.shapes.ROUNDED_RECTANGLE, {
    x: 0.6, y: 1.1, w: 8.8, h: 2.2,
    fill: { color: COLOR_LIGHT_BG },
    line: { color: COLOR_ACCENT, width: 2 }
});

slide8.addText('✅ 已完成工作', {
    x: 0.8, y: 1.2, w: 8.4, h: 0.35,
    fontSize: 20,
    bold: true,
    color: COLOR_PRIMARY
});

const miniDone = [
    '✓ 项目框架搭建完成，app.js/app.json/app.wxss配置完成',
    '✓ 微信授权登录功能完成，用户账号绑定完成',
    '✓ 工作台页面开发完成 → 数据概览、待办统计、快捷入口',
    '✓ 告警列表页面开发完成 → 下拉刷新、上拉加载、处理操作',
    '✓ 工单列表页面开发完成 → 查看我的工单、状态筛选',
            '✓ 工单详情页面开发完成 → 详情展示、处理流程时间轴',
            '✓ 个人中心页面开发完成 → 个人信息展示、账号解绑'
];

slide8.addText(miniDone.join('\n'), {
    x: 0.8, y: 1.6, w: 8.4, h: 1.5,
    fontSize: 14,
    color: COLOR_DARK,
    lineSpacing: 1.4
});

// 未完成
slide8.addShape(pres.shapes.ROUNDED_RECTANGLE, {
    x: 0.6, y: 3.5, w: 8.8, h: 1.4,
    fill: { color: COLOR_LIGHT_BG },
    line: { color: COLOR_DARK, width: 2 }
});

slide8.addText('⚙️ 未完成工作（需要后续完善）', {
    x: 0.8, y: 3.6, w: 8.4, h: 0.3,
    fontSize: 18,
    bold: true,
    color: COLOR_DARK
});

const miniTodo = [
    '• 扫码功能需要进一步调试',
    '• 语音报修功能还未实现',
            '• 真机测试和UI细节优化待完成',
            '• 消息推送功能待配置'
];

slide8.addText(miniTodo.join('\n'), {
    x: 0.8, y: 4.0, w: 8.4, h: 0.8,
    fontSize: 14,
    color: COLOR_DARK,
    lineSpacing: 1.5
});

// ==================== 幻灯片 9: 未完成工作 ====================
let slide9 = pres.addSlide();
slide9.background = { color: COLOR_LIGHT };

slide9.addText('7. 未完成工作', {
    x: 0.5, y: 0.3, w: 9.0, h: 0.5,
    fontSize: 32,
    bold: true,
    color: COLOR_PRIMARY
});

slide9.addShape(pres.shapes.RECTANGLE, {
    x: 0.5, y: 0.85, w: 9.0, h: 0.03,
    fill: { color: COLOR_PRIMARY }
});

const todoSections = [
    {
        title: '📱 微信小程序',
        items: [
            '1. 扫码报修功能调试',
            '2. 语音报修功能实现',
            '3. UI交互细节优化',
            '4. 真机测试和Bug修复'
        ],
        x: 0.6, y: 1.1, w: 4.2, h: 1.8
    },
    {
        title: '🧪 测试',
        items: [
            '1. 单元测试用例编写',
            '2. 集成测试覆盖核心API',
            '3. 功能完整性测试',
            '4. 性能测试和优化'
        ],
        x: 5.1, y: 1.1, w: 4.2, h: 1.8
    },
    {
        title: '📄 文档',
        items: [
            '1. 毕业论文撰写',
            '2. 答辩PPT准备',
            '3. 使用说明书完善',
            '4. 部署文档整理'
        ],
        x: 0.6, y: 3.1, w: 4.2, h: 1.8
    },
    {
        title: '✨ 优化',
        items: [
            '1. 数据库查询性能优化',
            '2. 前端响应速度优化',
            '3. 异常处理完善',
            '4. AI提示词工程优化'
        ],
        x: 5.1, y: 3.1, w: 4.2, h: 1.8
    }
];

todoSections.forEach(section => {
    slide9.addText(section.title, {
        x: section.x + 0.15, y: section.y + 0.1, w: section.w - 0.3, h: 0.3,
        fontSize: 18,
        bold: true,
        color: COLOR_PRIMARY
    });
    slide9.addText(section.items.join('\n'), {
        x: section.x + 0.15, y: section.y + 0.45, w: section.w - 0.3, h: section.h - 0.5,
        fontSize: 14,
        color: COLOR_DARK,
        lineSpacing: 1.6
    });
    slide9.addShape(pres.shapes.ROUNDED_RECTANGLE, {
        x: section.x, y: section.y, w: section.w, h: section.h,
        fill: { color: COLOR_LIGHT_BG },
        line: { color: COLOR_SECONDARY, width: 1 }
    });
});

// ==================== 幻灯片 10: 后续工作计划 ====================
let slide10 = pres.addSlide();
slide10.background = { color: COLOR_LIGHT };

slide10.addText('8. 后续工作计划', {
    x: 0.5, y: 0.3, w: 9.0, h: 0.5,
    fontSize: 32,
    bold: true,
    color: COLOR_PRIMARY
});

slide10.addShape(pres.shapes.RECTANGLE, {
    x: 0.5, y: 0.85, w: 9.0, h: 0.03,
    fill: { color: COLOR_PRIMARY }
});

// 时间线
const timeline = [
    {
        time: '4.26 - 5.10',
        task: '完成微信小程序',
        content: '完成剩余功能开发\n进行真机测试\n修复已知问题',
        x: 0.8, y: 1.2
    },
    {
        time: '5.11 - 5.20',
        task: '论文撰写',
        content: '完成毕业论文撰写\n修改完善内容\n准备查重',
        x: 3.3, y: 1.2
    },
    {
        time: '5.21 - 5.25',
        task: '测试优化',
        content: '补充单元测试\n进行系统测试\n性能优化',
        x: 5.8, y: 1.2
    },
    {
        time: '5.26 - 5.30',
        task: '答辩准备',
        content: '修改论文\n准备答辩PPT\n最终检查',
        x: 0.8, y: 3.2
    },
    {
        time: '6月',
        task: '答辩',
        content: '毕业论文答辩',
        x: 3.3, y: 3.2
    }
];

// 连接线
slide10.addShape(pres.shapes.RECTANGLE, {
    x: 0.8, y: 2.6, w: 7.4, h: 0.05,
    fill: { color: COLOR_PRIMARY }
});

timeline.forEach(item => {
    slide10.addShape(pres.shapes.ROUNDED_RECTANGLE, {
        x: item.x, y: item.y, w: 2.0, h: 1.3,
        fill: { color: COLOR_LIGHT_BG },
        line: { color: COLOR_PRIMARY, width: 2 }
    });
    slide10.addText(item.time, {
        x: item.x + 0.1, y: item.y + 0.1, w: 1.8, h: 0.25,
        fontSize: 15,
        bold: true,
        color: COLOR_PRIMARY,
        align: 'center'
    });
    slide10.addText(item.task, {
        x: item.x + 0.1, y: item.y + 0.4, w: 1.8, h: 0.25,
        fontSize: 16,
        bold: true,
        color: COLOR_DARK,
        align: 'center'
    });
    slide10.addText(item.content, {
        x: item.x + 0.1, y: item.y + 0.7, w: 1.8, h: 0.5,
        fontSize: 13,
        color: COLOR_DARK,
        align: 'center',
        lineSpacing: 1.3
    });
});

// 进度
slide10.addShape(pres.shapes.ROUNDED_RECTANGLE, {
    x: 5.8, y: 3.2, w: 2.0, h: 1.3,
    fill: { color: COLOR_LIGHT_BG },
    line: { color: COLOR_PRIMARY, width: 2 }
});
slide10.addText('5.26 - 5.30', {
    x: 5.8 + 0.1, y: 3.2 + 0.1, w: 1.8, h: 0.25,
    fontSize: 15,
    bold: true,
    color: COLOR_PRIMARY,
    align: 'center'
});
slide10.addText('答辩准备', {
    x: 5.8 + 0.1, y: 3.2 + 0.4, w: 1.8, h: 0.25,
    fontSize: 16,
    bold: true,
    color: COLOR_DARK,
    align: 'center'
});
slide10.addText('修改论文\n准备答辩PPT\n最终检查', {
    x: 5.8 + 0.1, y: 3.2 + 0.7, w: 1.8, h: 0.5,
    fontSize: 13,
    color: COLOR_DARK,
    align: 'center',
    lineSpacing: 1.3
});

// 进度统计
slide10.addShape(pres.shapes.ROUNDED_RECTANGLE, {
    x: 0.6, y: 4.7, w: 8.8, h: 0.6,
    fill: { color: COLOR_LIGHT_BG },
    line: { color: COLOR_SECONDARY, width: 1 }
});

slide10.addText('📊 总体进度：约 75%', {
    x: 0.8, y: 4.8, w: 3.0, h: 0.4,
    fontSize: 18,
    bold: true,
    color: COLOR_PRIMARY
});

slide10.addText('按原计划推进，预计能按时完成全部开发任务', {
    x: 4.0, y: 4.8, w: 5.0, h: 0.4,
    fontSize: 15,
    color: COLOR_DARK,
    align: 'right'
});

// ==================== 幻灯片 11: 中期总结 ====================
let slide11 = pres.addSlide();
slide11.background = { color: COLOR_PRIMARY };

slide11.addText('9. 中期总结', {
    x: 0.5, y: 0.8, w: 9.0, h: 0.6,
    fontSize: 36,
    bold: true,
    color: COLOR_LIGHT,
    align: 'center'
});

const summaryItems = [
    '• 按照开题报告计划，已按期完成预定的中期开发任务',
    '• 后端核心功能全部完成：用户认证、库区设备管理、告警分析、工单管理',
    '• 前端Web管理端全部页面开发完成',
    '• AI大语言模型集成成功，解决了Spring AI集成问题',
    '• 微信小程序基本框架和主要页面完成，待细节完善',
            '• 遇到的技术问题都已解决，项目整体进展顺利',
            '• 剩余工作按计划在剩余时间内可以完成'
];

slide11.addText(summaryItems.join('\n'), {
    x: 1.0, y: 1.8, w: 8.0, h: 3.0,
    fontSize: 18,
    color: COLOR_LIGHT,
    lineSpacing: 1.6
});

slide11.addText('\n🙏 请各位老师批评指正！', {
    x: 0.5, y: 4.8, w: 9.0, h: 0.5,
    fontSize: 24,
    bold: true,
    color: COLOR_LIGHT,
    align: 'center'
});

// ==================== 保存PPT ====================
pres.writeFile({ fileName: 'coldchain-guardian-midterm-check.pptx' }, (err) => {
    if (err) {
        console.error('Error generating PPT:', err);
    } else {
        console.log('PPT generated successfully: coldchain-guardian-midterm-check.pptx');
    }
});
