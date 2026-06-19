你/**
 * ColdChain Guardian 毕业设计答辩PPT
 * 使用 PptxGenJS 生成
 *
 * 配色方案：Teal Trust
 *   Primary: 028090 (teal) - 主色，符合冷链主题
 *   Secondary: 00A896 (seafoam) - 辅助色
 *   Accent: 02C39A (mint) - 强调色
 *   Background: FFFFFF / 028090 (标题页使用深色背景)
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
    x: 0.5, y: 1.0, w: 9.0, h: 0.8,
    fontSize: 44,
    bold: true,
    color: COLOR_LIGHT,
    align: 'center'
});

// 副标题
slide1.addText('基于大语言模型的冷链仓储安全管理系统', {
    x: 0.5, y: 2.0, w: 9.0, h: 0.5,
    fontSize: 28,
    color: COLOR_LIGHT,
    align: 'center'
});

// 作者信息
slide1.addText('毕业设计答辩', {
    x: 0.5, y: 3.2, w: 9.0, h: 0.4,
    fontSize: 20,
    color: 'CADCFC',
    align: 'center'
});

slide1.addText('答 辩 人：\n指导教师：\n学 院：\n日 期：', {
    x: 2.5, y: 4.2, w: 5.0, h: 1.5,
    fontSize: 18,
    color: COLOR_LIGHT,
    align: 'left',
    lineSpacing: 1.5
});

// ==================== 幻灯片 2: 目录 ====================
let slide2 = pres.addSlide();
slide2.background = { color: COLOR_LIGHT };

slide2.addText('目 录', {
    x: 0.5, y: 0.3, w: 9.0, h: 0.5,
    fontSize: 36,
    bold: true,
    color: COLOR_PRIMARY,
    align: 'left'
});

// 分割线
slide2.addShape(pres.shapes.RECTANGLE, {
    x: 0.5, y: 0.85, w: 9.0, h: 0.03,
    fill: { color: COLOR_PRIMARY }
});

const tocItems = [
    '1. 选题背景与研究意义',
    '2. 系统需求分析',
    '3. 系统架构设计',
    '4. 核心功能模块',
    '5. 关键技术实现',
    '6. AI智能助手集成',
    '7. 系统展示',
    '8. 创新点与特色',
    '9. 总结与展望'
];

let yStart = 1.3;
tocItems.forEach((item, idx) => {
    slide2.addText(item, {
        x: 1.0, y: yStart + idx * 0.55, w: 8.0, h: 0.45,
        fontSize: 20,
        color: COLOR_DARK,
        bullet: false
    });
});

// ==================== 幻灯片 3: 选题背景与研究意义 ====================
let slide3 = pres.addSlide();
slide3.background = { color: COLOR_LIGHT };

slide3.addText('1. 选题背景与研究意义', {
    x: 0.5, y: 0.3, w: 9.0, h: 0.5,
    fontSize: 32,
    bold: true,
    color: COLOR_PRIMARY
});

slide3.addShape(pres.shapes.RECTANGLE, {
    x: 0.5, y: 0.85, w: 9.0, h: 0.03,
    fill: { color: COLOR_PRIMARY }
});

// 两栏布局：左侧问题，右侧意义
// 左侧：行业背景
slide3.addText('🎯 行业背景', {
    x: 0.6, y: 1.1, w: 4.2, h: 0.35,
    fontSize: 22,
    bold: true,
    color: COLOR_PRIMARY
});

const industryBg = [
    '• 冷链物流是食品安全的重要保障',
    '• 温湿度异常是冷链损耗主要原因',
    '• 传统人工巡检效率低、滞后性强',
            '• 物联网设备产生海量数据需要智能化分析'
];

slide3.addText(industryBg.join('\n'), {
    x: 0.6, y: 1.5, w: 4.2, h: 1.8,
    fontSize: 16,
    color: COLOR_DARK,
    lineSpacing: 1.4
});

// 框起来
slide3.addShape(pres.shapes.ROUNDED_RECTANGLE, {
    x: 0.5, y: 1.0, w: 4.4, h: 2.4,
    fill: { color: COLOR_LIGHT_BG },
    line: { color: COLOR_SECONDARY, width: 1 }
});

// 右侧：研究意义
slide3.addText('💡 研究意义', {
    x: 5.1, y: 1.1, w: 4.2, h: 0.35,
    fontSize: 22,
    bold: true,
    color: COLOR_PRIMARY
});

const researchMeaning = [
    '• 提升冷链仓储安全管理智能化水平',
    '• 实现异常问题早发现、早处理',
    '• 降低冷链损耗，保障食品安全',
            '• 探索大语言模型在工业物联网中的应用'
];

slide3.addText(researchMeaning.join('\n'), {
    x: 5.1, y: 1.5, w: 4.2, h: 1.8,
    fontSize: 16,
    color: COLOR_DARK,
    lineSpacing: 1.4
});

slide3.addShape(pres.shapes.ROUNDED_RECTANGLE, {
    x: 5.0, y: 1.0, w: 4.4, h: 2.4,
    fill: { color: COLOR_LIGHT_BG },
    line: { color: COLOR_SECONDARY, width: 1 }
});

// 底部：研究目标
slide3.addText('📋 研究目标', {
    x: 0.6, y: 3.6, w: 8.8, h: 0.35,
    fontSize: 20,
    bold: true,
    color: COLOR_PRIMARY
});

const objectives = [
    '构建完整的冷链环境监测体系 → 实现温湿度数据实时采集与监控 | ' +
    '建立智能预警机制 → 及时发现并处理潜在安全隐患 | ' +
    '实现告警→工单完整闭环管理 → 规范处理流程 | ' +
    '集成大语言模型 → 提供智能对话式数据分析能力'
];

slide3.addText('构建完整的冷链环境监测体系，实现温湿度数据实时采集与监控\n建立智能预警机制，及时发现并处理潜在安全隐患\n实现告警→工单完整闭环管理，规范处理流程\n集成大语言模型，提供智能对话式数据分析能力', {
    x: 0.6, y: 4.0, w: 8.8, h: 1.0,
    fontSize: 16,
    color: COLOR_DARK,
    lineSpacing: 1.4
});

// ==================== 幻灯片 4: 系统需求分析 ====================
let slide4 = pres.addSlide();
slide4.background = { color: COLOR_LIGHT };

slide4.addText('2. 系统需求分析', {
    x: 0.5, y: 0.3, w: 9.0, h: 0.5,
    fontSize: 32,
    bold: true,
    color: COLOR_PRIMARY
});

slide4.addShape(pres.shapes.RECTANGLE, {
    x: 0.5, y: 0.85, w: 9.0, h: 0.03,
    fill: { color: COLOR_PRIMARY }
});

// 四宫格布局
const requirements = [
    {
        title: '功能需求',
        points: [
            '• 用户认证与权限管理',
            '• 库区与设备管理',
            '• 实时数据监控',
            '• 异常告警管理',
            '• 工单闭环处理'
        ],
        x: 0.6, y: 1.1, w: 4.3, h: 2.2
    },
    {
        title: '非功能需求',
        points: [
            '• 系统响应时间 < 200ms',
            '• 支持多用户并发访问',
            '• 数据持久化存储',
            '• 前端界面友好易用',
            '• 代码可维护可扩展'
        ],
        x: 5.1, y: 1.1, w: 4.3, h: 2.2
    },
    {
        title: '用户角色',
        points: [
            '• 管理员：系统配置、用户管理',
            '• 管理层：数据分析、报表查看',
            '• 员工：现场处理、工单执行',
            '• AI助手：智能问答分析'
        ],
        x: 0.6, y: 3.5, w: 4.3, h: 1.8
    },
    {
        title: 'AI能力需求',
        points: [
            '• 自然语言查询系统数据',
            '• 告警智能分析与建议',
            '• 自动化报告生成',
            '• 对话上下文记忆'
        ],
        x: 5.1, y: 3.5, w: 4.3, h: 1.8
    }
];

requirements.forEach(req => {
    slide4.addText(req.title, {
        x: req.x + 0.1, y: req.y + 0.05, w: req.w - 0.2, h: 0.35,
        fontSize: 20,
        bold: true,
        color: COLOR_PRIMARY
    });
    slide4.addText(req.points.join('\n'), {
        x: req.x + 0.1, y: req.y + 0.45, w: req.w - 0.2, h: req.h - 0.5,
        fontSize: 15,
        color: COLOR_DARK,
        lineSpacing: 1.4
    });
    slide4.addShape(pres.shapes.ROUNDED_RECTANGLE, {
        x: req.x, y: req.y, w: req.w, h: req.h,
        fill: { color: COLOR_LIGHT_BG },
        line: { color: COLOR_SECONDARY, width: 1 }
    });
});

// ==================== 幻灯片 5: 系统架构设计 ====================
let slide5 = pres.addSlide();
slide5.background = { color: COLOR_LIGHT };

slide5.addText('3. 系统架构设计', {
    x: 0.5, y: 0.3, w: 9.0, h: 0.5,
    fontSize: 32,
    bold: true,
    color: COLOR_PRIMARY
});

slide5.addShape(pres.shapes.RECTANGLE, {
    x: 0.5, y: 0.85, w: 9.0, h: 0.03,
    fill: { color: COLOR_PRIMARY }
});

// 分层架构图（文本描述+色块）
const layers = [
    {
        name: '物联网采集层',
        desc: '温湿度传感器 → MQTT协议上传',
        color: COLOR_ACCENT,
        y: 1.2,
        height: 0.8
    },
    {
        name: '后端服务层',
        desc: 'Spring Boot + MyBatis-Plus + MySQL + Redis',
        color: COLOR_SECONDARY,
        y: 2.2,
        height: 0.8
    },
    {
        name: '前端应用层',
        desc: 'Web管理端 (Vue 3) + 微信小程序 (员工端)',
        color: COLOR_PRIMARY,
        y: 3.2,
        height: 0.8
    },
    {
        name: '智能分析层',
        desc: 'Spring AI + 通义千问大语言模型',
        color: COLOR_DARK,
        y: 4.2,
        height: 0.8
    }
];

layers.forEach(layer => {
    slide5.addShape(pres.shapes.RECTANGLE, {
        x: 1.0, y: layer.y, w: 8.0, h: layer.height,
        fill: { color: layer.color },
        line: { color: COLOR_LIGHT, width: 2 }
    });
    slide5.addText(layer.name, {
        x: 1.2, y: layer.y + 0.15, w: 3.0, h: 0.4,
        fontSize: 22,
        bold: true,
        color: COLOR_LIGHT
    });
    slide5.addText(layer.desc, {
        x: 4.0, y: layer.y + 0.18, w: 4.5, h: 0.4,
        fontSize: 16,
        color: COLOR_LIGHT
    });
});

// 技术栈总结
slide5.addText('技术栈', {
    x: 0.6, y: 5.2, w: 2.0, h: 0.3,
    fontSize: 18,
    bold: true,
    color: COLOR_PRIMARY
});

slide5.addText('后端: Java 17 + Spring Boot 3.2 + Spring AI | 前端: Vue 3.5 + Element Plus + Pinia | 数据库: MySQL 8.0 + Redis', {
    x: 2.8, y: 5.2, w: 6.5, h: 0.3,
    fontSize: 14,
    color: COLOR_DARK
});

// ==================== 幻灯片 6: 数据库设计 ====================
let slide6 = pres.addSlide();
slide6.background = { color: COLOR_LIGHT };

slide6.addText('3.1 数据库设计', {
    x: 0.5, y: 0.3, w: 9.0, h: 0.5,
    fontSize: 32,
    bold: true,
    color: COLOR_PRIMARY
});

slide6.addShape(pres.shapes.RECTANGLE, {
    x: 0.5, y: 0.85, w: 9.0, h: 0.03,
    fill: { color: COLOR_PRIMARY }
});

// 核心数据表（分两列）
const tablesLeft = [
    '📊 users - 用户表 (支持微信绑定)',
    '🏬 warehouse_areas - 库区表 (5级树形结构)',
    '📟 devices - 传感器设备表 (阈值继承)',
    '🌡️ sensor_data - 温湿度数据表 (优化索引)',
    '⚙️ alert_configs - 告警配置表'
];

const tablesRight = [
    '🚨 alerts - 告警记录表 (持续告警统计)',
    '📋 work_orders - 维修工单表',
    '📝 work_order_logs - 工单流转日志',
    '💬 ai_chat_sessions - AI会话表',
            '✉️ ai_chat_messages - AI消息明细表'
];

slide6.addShape(pres.shapes.ROUNDED_RECTANGLE, {
    x: 0.6, y: 1.1, w: 4.4, h: 2.0,
    fill: { color: COLOR_LIGHT_BG },
    line: { color: COLOR_SECONDARY, width: 1 }
});

slide6.addText('核心数据表', {
    x: 0.8, y: 1.2, w: 4.0, h: 0.3,
    fontSize: 20,
    bold: true,
    color: COLOR_PRIMARY
});

slide6.addText(tablesLeft.join('\n'), {
    x: 0.8, y: 1.6, w: 4.0, h: 1.3,
    fontSize: 15,
    color: COLOR_DARK,
    lineSpacing: 1.6
});

slide6.addShape(pres.shapes.ROUNDED_RECTANGLE, {
    x: 5.0, y: 1.1, w: 4.4, h: 2.0,
    fill: { color: COLOR_LIGHT_BG },
    line: { color: COLOR_SECONDARY, width: 1 }
});

slide6.addText(' ', {
    x: 5.2, y: 1.2, w: 4.0, h: 0.3,
    fontSize: 20,
    bold: true,
    color: COLOR_PRIMARY
});

slide6.addText(tablesRight.join('\n'), {
    x: 5.2, y: 1.6, w: 4.0, h: 1.3,
    fontSize: 15,
    color: COLOR_DARK,
    lineSpacing: 1.6
});

// 设计特点
slide6.addText('设计特点', {
    x: 0.6, y: 3.3, w: 8.8, h: 0.3,
    fontSize: 20,
    bold: true,
    color: COLOR_PRIMARY
});

const designFeatures = [
    '• 遵循第三范式，保证数据一致性',
    '• 合理建立索引优化查询性能（设备+时间联合索引用于历史曲线查询）',
    '• 支持树形库区结构，适配复杂仓储布局',
    '• 预留扩展字段应对未来需求变化'
];

slide6.addText(designFeatures.join('\n'), {
    x: 0.6, y: 3.7, w: 8.8, h: 0.9,
    fontSize: 15,
    color: COLOR_DARK,
    lineSpacing: 1.4
});

// ==================== 幻灯片 7: 核心功能模块 - 用户&库区&设备 ====================
let slide7 = pres.addSlide();
slide7.background = { color: COLOR_LIGHT };

slide7.addText('4. 核心功能模块', {
    x: 0.5, y: 0.3, w: 9.0, h: 0.5,
    fontSize: 32,
    bold: true,
    color: COLOR_PRIMARY
});

slide7.addShape(pres.shapes.RECTANGLE, {
    x: 0.5, y: 0.85, w: 9.0, h: 0.03,
    fill: { color: COLOR_PRIMARY }
});

// 三模块
const modulesRow1 = [
    {
        icon: '🔐',
        title: '用户认证',
        points: [
            'JWT令牌认证',
            '多角色权限控制',
            '管理员/员工/管理层',
            '微信小程序绑定'
        ],
        x: 0.6, y: 1.1, w: 2.8, h: 2.0
    },
    {
        icon: '🏬',
        title: '库区管理',
        points: [
            '五级树形结构',
            'SITE/WAREHOUSE/FLOOR/AREA/BIN',
            '温湿度阈值设定',
            '完整CRUD操作'
        ],
        x: 3.6, y: 1.1, w: 2.8, h: 2.0
    },
    {
        icon: '📟',
        title: '设备管理',
        points: [
            '传感器注册配置',
            '阈值继承/覆盖',
            '在线状态监控',
            '批量操作支持'
        ],
        x: 6.6, y: 1.1, w: 2.8, h: 2.0
    }
];

modulesRow1.forEach(mod => {
    slide7.addText(`${mod.icon} ${mod.title}`, {
        x: mod.x + 0.15, y: mod.y + 0.1, w: mod.w - 0.3, h: 0.35,
        fontSize: 18,
        bold: true,
        color: COLOR_PRIMARY
    });
    slide7.addText(mod.points.join('\n'), {
        x: mod.x + 0.15, y: mod.y + 0.5, w: mod.w - 0.3, h: mod.h - 0.6,
        fontSize: 14,
        color: COLOR_DARK,
        lineSpacing: 1.5
    });
    slide7.addShape(pres.shapes.ROUNDED_RECTANGLE, {
        x: mod.x, y: mod.y, w: mod.w, h: mod.h,
        fill: { color: COLOR_LIGHT_BG },
        line: { color: COLOR_SECONDARY, width: 1 }
    });
});

// ==================== 幻灯片 8: 核心功能模块 - 监控&告警&工单 ====================
let slide8 = pres.addSlide();
slide8.background = { color: COLOR_LIGHT };

slide8.addText('4. 核心功能模块', {
    x: 0.5, y: 0.3, w: 9.0, h: 0.5,
    fontSize: 32,
    bold: true,
    color: COLOR_PRIMARY
});

slide8.addShape(pres.shapes.RECTANGLE, {
    x: 0.5, y: 0.85, w: 9.0, h: 0.03,
    fill: { color: COLOR_PRIMARY }
});

const modulesRow2 = [
    {
        icon: '📈',
        title: '实时监控',
        points: [
            '温湿度实时展示',
            'ECharts趋势分析',
            '多时间维度查看',
            'WebSocket实时推送'
        ],
        x: 0.6, y: 1.1, w: 2.8, h: 2.0
    },
    {
        icon: '🚨',
        title: '告警系统',
        points: [
            '阈值异常检测',
            '四级告警分级',
            '智能分析能力',
            '趋势/重复/根因分析'
        ],
        x: 3.6, y: 1.1, w: 2.8, h: 2.0
    },
    {
        icon: '📋',
        title: '工单管理',
        points: [
            '告警一键转工单',
            '完整生命周期流转',
            '处理日志记录',
            '闭环验收机制'
        ],
        x: 6.6, y: 1.1, w: 2.8, h: 2.0
    }
];

modulesRow2.forEach(mod => {
    slide8.addText(`${mod.icon} ${mod.title}`, {
        x: mod.x + 0.15, y: mod.y + 0.1, w: mod.w - 0.3, h: 0.35,
        fontSize: 18,
        bold: true,
        color: COLOR_PRIMARY
    });
    slide8.addText(mod.points.join('\n'), {
        x: mod.x + 0.15, y: mod.y + 0.5, w: mod.w - 0.3, h: mod.h - 0.6,
        fontSize: 14,
        color: COLOR_DARK,
        lineSpacing: 1.5
    });
    slide8.addShape(pres.shapes.ROUNDED_RECTANGLE, {
        x: mod.x, y: mod.y, w: mod.w, h: mod.h,
        fill: { color: COLOR_LIGHT_BG },
        line: { color: COLOR_SECONDARY, width: 1 }
    });
});

// 底部：仪表盘
slide8.addShape(pres.shapes.ROUNDED_RECTANGLE, {
    x: 0.6, y: 3.3, w: 8.8, h: 1.5,
    fill: { color: COLOR_LIGHT_BG },
    line: { color: COLOR_SECONDARY, width: 1 }
});

slide8.addText('📊 仪表盘', {
    x: 0.8, y: 3.4, w: 8.4, h: 0.3,
    fontSize: 18,
    bold: true,
    color: COLOR_PRIMARY
});

slide8.addText('KPI指标展示（在线设备数、今日告警、未处理告警、今日闭环工单） | ' +
    '库区状态卡片 | 实时趋势图 | 紧急告警列表 | 待处理工单列表 | 快捷操作入口', {
    x: 0.8, y: 3.8, w: 8.4, h: 0.8,
    fontSize: 14,
    color: COLOR_DARK,
    lineSpacing: 1.4
});

// ==================== 幻灯片 9: 关键技术实现 - 告警分析 ====================
let slide9 = pres.addSlide();
slide9.background = { color: COLOR_LIGHT };

slide9.addText('5. 关键技术实现 - 智能告警分析', {
    x: 0.5, y: 0.3, w: 9.0, h: 0.5,
    fontSize: 32,
    bold: true,
    color: COLOR_PRIMARY
});

slide9.addShape(pres.shapes.RECTANGLE, {
    x: 0.5, y: 0.85, w: 9.0, h: 0.03,
    fill: { color: COLOR_PRIMARY }
});

// 四种分析算法
const analysisMethods = [
    {
        title: '📈 趋势分析',
        desc: '基于时间序列统计告警数量\n识别异常波动和周期性规律\n为决策提供数据支持',
        x: 0.6, y: 1.1, w: 4.2, h: 1.4
    },
    {
        title: '🔁 重复告警分析',
        desc: '按设备和告警类型统计频次\n识别高频顽固问题\n帮助定位系统性隐患',
        x: 5.1, y: 1.1, w: 4.2, h: 1.4
    },
    {
        title: '❤️ 设备健康度评分',
        desc: '基于告警级别和频次加权\n计算综合健康分数\n优先安排维护高风险设备',
        x: 0.6, y: 2.7, w: 4.2, h: 1.4
    },
    {
        title: '🔍 根因分析',
        desc: '基于时间窗口聚类\n识别同时发生的关联告警\n快速定位问题源头',
        x: 5.1, y: 2.7, w: 4.2, h: 1.4
    }
];

analysisMethods.forEach(method => {
    slide9.addText(method.title, {
        x: method.x + 0.15, y: method.y + 0.08, w: method.w - 0.3, h: 0.3,
        fontSize: 20,
        bold: true,
        color: COLOR_PRIMARY
    });
    slide9.addText(method.desc, {
        x: method.x + 0.15, y: method.y + 0.42, w: method.w - 0.3, h: method.h - 0.5,
        fontSize: 14,
        color: COLOR_DARK,
        lineSpacing: 1.5
    });
    slide9.addShape(pres.shapes.ROUNDED_RECTANGLE, {
        x: method.x, y: method.y, w: method.w, h: method.h,
        fill: { color: COLOR_LIGHT_BG },
        line: { color: COLOR_SECONDARY, width: 1 }
    });
});

// 状态流转图文字说明
slide9.addText('告警状态流转', {
    x: 0.6, y: 4.3, w: 8.8, h: 0.3,
    fontSize: 18,
    bold: true,
    color: COLOR_PRIMARY
});

slide9.addText('UNHANDLED (未处理) → HANDLING (处理中) → RESOLVED (已解决) / IGNORED (已忽略)', {
    x: 0.6, y: 4.7, w: 8.8, h: 0.4,
    fontSize: 16,
    color: COLOR_DARK,
    align: 'center'
});

// ==================== 幻灯片 10: 关键技术实现 - 工单闭环 ====================
let slide10 = pres.addSlide();
slide10.background = { color: COLOR_LIGHT };

slide10.addText('5. 关键技术实现 - 工单闭环管理', {
    x: 0.5, y: 0.3, w: 9.0, h: 0.5,
    fontSize: 32,
    bold: true,
    color: COLOR_PRIMARY
});

slide10.addShape(pres.shapes.RECTANGLE, {
    x: 0.5, y: 0.85, w: 9.0, h: 0.03,
    fill: { color: COLOR_PRIMARY }
});

// 工单状态流转时间线
const flow = [
    {
        step: 'PENDING',
        name: '待处理',
        desc: '工单创建完成\n等待接受分配',
        x: 0.5,
        color: COLOR_PRIMARY
    },
    {
        step: 'PROCESSING',
        name: '处理中',
        desc: '员工接受工单\n正在现场处理',
        x: 2.5,
        color: COLOR_SECONDARY
    },
    {
        step: 'VERIFYING',
        name: '待验收',
        desc: '处理完成\n等待管理员验收',
        x: 4.5,
        color: COLOR_ACCENT
    },
    {
        step: 'COMPLETED',
        name: '已完成',
        desc: '验收通过\n工单处理完成',
        x: 6.5,
        color: COLOR_DARK
    },
    {
        step: 'CLOSED',
        name: '已关闭',
        desc: '最终归档\n完成闭环',
        x: 8.0,
        color: COLOR_DARK
    }
];

// 连接线
slide10.addShape(pres.shapes.RECTANGLE, {
    x: 1.0, y: 2.8, w: 8.0, h: 0.05,
    fill: { color: COLOR_PRIMARY }
});

flow.forEach(s => {
    slide10.addShape(pres.shapes.RECTANGLE, {
        x: s.x, y: 2.0, w: 1.8, h: 1.6,
        fill: { color: COLOR_LIGHT_BG },
        line: { color: s.color, width: 2 }
    });
    slide10.addText(`${s.step}\n${s.name}`, {
        x: s.x + 0.1, y: 2.1, w: 1.6, h: 0.6,
        fontSize: 16,
        bold: true,
        color: s.color,
        align: 'center'
    });
    slide10.addText(s.desc, {
        x: s.x + 0.1, y: 2.8, w: 1.6, h: 0.6,
        fontSize: 12,
        color: COLOR_DARK,
        align: 'center',
        lineSpacing: 1.3
    });
});

// 特色
slide10.addText('🔹 系统特色', {
    x: 0.5, y: 4.0, w: 4.0, h: 0.3,
    fontSize: 18,
    bold: true,
    color: COLOR_PRIMARY
});

const features = [
    '• 完整操作日志记录，全程可追溯',
    '• 与告警系统深度集成，状态同步更新',
    '• 支持多维度筛选和统计分析',
    '• 从告警发现到问题解决的完整闭环'
];

slide10.addText(features.join('\n'), {
    x: 0.5, y: 4.4, w: 4.5, h: 0.8,
    fontSize: 14,
    color: COLOR_DARK,
    lineSpacing: 1.4
});

slide10.addText('🔹 工单分类', {
    x: 5.2, y: 4.0, w: 4.0, h: 0.3,
    fontSize: 18,
    bold: true,
    color: COLOR_PRIMARY
});

const types = [
    '• ALERT_FIX - 告警消缺处理',
    '• INSPECTION - 日常巡检任务',
    '• MAINTENANCE - 设备维护保养',
    '• 优先级：LOW/MEDIUM/HIGH/URGENT'
];

slide10.addText(types.join('\n'), {
    x: 5.2, y: 4.4, w: 4.0, h: 0.8,
    fontSize: 14,
    color: COLOR_DARK,
    lineSpacing: 1.4
});

// ==================== 幻灯片 11: AI智能助手 - 架构 ====================
let slide11 = pres.addSlide();
slide11.background = { color: COLOR_LIGHT };

slide11.addText('6. AI智能助手集成', {
    x: 0.5, y: 0.3, w: 9.0, h: 0.5,
    fontSize: 32,
    bold: true,
    color: COLOR_PRIMARY
});

slide11.addShape(pres.shapes.RECTANGLE, {
    x: 0.5, y: 0.85, w: 9.0, h: 0.03,
    fill: { color: COLOR_PRIMARY }
});

// 架构图文本
slide11.addText('🏗️ 系统架构', {
    x: 0.6, y: 1.0, w: 8.8, h: 0.35,
    fontSize: 22,
    bold: true,
    color: COLOR_PRIMARY
});

const arch = [
    '前端 → SSE流式请求 → AIAssistantController → AIAssistantService → Spring AI → 通义千问API',
    '                                                   ↓',
    '                         会话历史 + 业务上下文注入 → 大语言模型 → 流式输出 → 前端打字机效果'
];

slide11.addText(arch.join('\n'), {
    x: 0.6, y: 1.4, w: 8.8, h: 0.8,
    fontSize: 15,
    color: COLOR_DARK,
    lineSpacing: 1.5,
    align: 'center'
});

slide11.addShape(pres.shapes.RECTANGLE, {
    x: 0.6, y: 1.0, w: 8.8, h: 1.3,
    fill: { color: COLOR_LIGHT_BG },
    line: { color: COLOR_SECONDARY, width: 1 }
});

// 核心特性
const aiFeatures = [
    {
        title: '🌊 流式输出(SSE)',
        desc: 'Server-Sent Events + Flux<String>\n实现打字机效果，提升用户体验',
        x: 0.6, y: 2.5, w: 2.7, h: 1.2
    },
    {
        title: '🔍 轻量级RAG',
        desc: '附件上下文动态注入\n设备/告警信息自动查询\n丰富大模型回答内容',
        x: 3.5, y: 2.5, w: 2.7, h: 1.2
    },
    {
        title: '💾 会话记忆',
        desc: '会话+消息分离存储\n历史对话持久化\n支持多会话管理',
        x: 6.4, y: 2.5, w: 3.0, h: 1.2
    }
];

aiFeatures.forEach(f => {
    slide11.addText(f.title, {
        x: f.x + 0.15, y: f.y + 0.08, w: f.w - 0.3, h: 0.3,
        fontSize: 18,
        bold: true,
        color: COLOR_PRIMARY
    });
    slide11.addText(f.desc, {
        x: f.x + 0.15, y: f.y + 0.42, w: f.w - 0.3, h: f.h - 0.5,
        fontSize: 13,
        color: COLOR_DARK,
        lineSpacing: 1.4
    });
    slide11.addShape(pres.shapes.ROUNDED_RECTANGLE, {
        x: f.x, y: f.y, w: f.w, h: f.h,
        fill: { color: COLOR_LIGHT_BG },
        line: { color: COLOR_SECONDARY, width: 1 }
    });
});

// 解决的问题
slide11.addText('遇到并解决的关键问题', {
    x: 0.6, y: 3.9, w: 8.8, h: 0.3,
    fontSize: 18,
    bold: true,
    color: COLOR_PRIMARY
});

const problems = [
    '1. Spring AI版本依赖冲突 → 使用稳定版本M1，通过BOM统一管理',
    '2. SSE流式输出403错误 → SecurityConfig配置DispatcherType.ASYNC.permitAll',
    '3. API兼容性问题 → 适配Spring AI 1.0.0-M1标准API'
];

slide11.addText(problems.join('\n'), {
    x: 0.6, y: 4.3, w: 8.8, h: 0.7,
    fontSize: 14,
    color: COLOR_DARK,
    lineSpacing: 1.5
});

// ==================== 幻灯片 12: AI智能助手 - 功能 ====================
let slide12 = pres.addSlide();
slide12.background = { color: COLOR_LIGHT };

slide12.addText('6.1 AI智能助手功能特性', {
    x: 0.5, y: 0.3, w: 9.0, h: 0.5,
    fontSize: 32,
    bold: true,
    color: COLOR_PRIMARY
});

slide12.addShape(pres.shapes.RECTANGLE, {
    x: 0.5, y: 0.85, w: 9.0, h: 0.03,
    fill: { color: COLOR_PRIMARY }
});

// 两列：功能+使用场景
const aiFunctions = [
    '✓ 自然语言查询系统数据',
    '✓ 告警智能总结分析',
    '✓ 提供处置建议',
    '✓ 自动化报告生成',
    '✓ 趋势分析辅助决策',
    '✓ 对话上下文记忆'
];

const aiScenarios = [
    '• "帮我看看最近一周温度异常情况"',
    '• "分析一下这个告警可能是什么原因"',
            '• "这个设备最近一个月告警统计"',
            '• "帮我生成一份今日仓储安全日报"'
];

slide12.addShape(pres.shapes.ROUNDED_RECTANGLE, {
    x: 0.6, y: 1.1, w: 4.4, h: 2.2,
    fill: { color: COLOR_LIGHT_BG },
    line: { color: COLOR_SECONDARY, width: 1 }
});

slide12.addText('支持功能', {
    x: 0.8, y: 1.2, w: 4.0, h: 0.3,
    fontSize: 20,
    bold: true,
    color: COLOR_PRIMARY
});

slide12.addText(aiFunctions.join('\n'), {
    x: 0.8, y: 1.6, w: 4.0, h: 1.5,
    fontSize: 16,
    color: COLOR_DARK,
    lineSpacing: 1.6
});

slide12.addShape(pres.shapes.ROUNDED_RECTANGLE, {
    x: 5.0, y: 1.1, w: 4.4, h: 2.2,
    fill: { color: COLOR_LIGHT_BG },
    line: { color: COLOR_SECONDARY, width: 1 }
});

slide12.addText('使用示例', {
    x: 5.2, y: 1.2, w: 4.0, h: 0.3,
    fontSize: 20,
    bold: true,
    color: COLOR_PRIMARY
});

slide12.addText(aiScenarios.join('\n'), {
    x: 5.2, y: 1.6, w: 4.0, h: 1.5,
    fontSize: 14,
    color: COLOR_DARK,
    lineSpacing: 1.8
});

// 技术亮点总结
slide12.addText('✨ 技术亮点', {
    x: 0.6, y: 3.5, w: 8.8, h: 0.3,
    fontSize: 20,
    bold: true,
    color: COLOR_PRIMARY
});

const highlights = [
    '• 轻量级RAG设计：不使用复杂向量数据库，根据附件自动查询业务数据注入上下文，实现轻量化检索增强生成',
    '• Spring AI框架集成：依托Spring AI统一API，便于切换不同大语言模型供应商',
            '• OpenAI兼容模式：通义千问通过OpenAI兼容接口接入，配置简单，稳定性好',
            '• 完整会话管理：分离会话和消息设计，支持多会话切换和历史对话持久化'
];

slide12.addText(highlights.join('\n'), {
    x: 0.6, y: 3.9, w: 8.8, h: 1.0,
    fontSize: 14,
    color: COLOR_DARK,
    lineSpacing: 1.4
});

// ==================== 幻灯片 13: 系统展示 - Web端 ====================
let slide13 = pres.addSlide();
slide13.background = { color: COLOR_LIGHT };

slide13.addText('7. 系统展示 - Web管理端', {
    x: 0.5, y: 0.3, w: 9.0, h: 0.5,
    fontSize: 32,
    bold: true,
    color: COLOR_PRIMARY
});

slide13.addShape(pres.shapes.RECTANGLE, {
    x: 0.5, y: 0.85, w: 9.0, h: 0.03,
    fill: { color: COLOR_PRIMARY }
});

const webFeatures = [
    {
        page: '仪表盘',
        desc: 'KPI总览 + 实时数据卡片',
        x: 0.6, y: 1.1, w: 4.2, h: 0.8
    },
    {
        page: '实时监控',
        desc: 'ECharts实时曲线 + 多维度查看',
        x: 0.6, y: 2.0, w: 4.2, h: 0.8
    },
    {
        page: '告警中心',
        desc: '智能分析面板 + 告警分诊',
        x: 0.6, y: 2.9, w: 4.2, h: 0.8
    },
    {
        page: '工单中心',
        desc: '全生命周期管理 + 时间轴展示',
        x: 0.6, y: 3.8, w: 4.2, h: 0.8
    },
    {
        page: '设备管理',
        desc: 'CRUD + 阈值配置 + 批量操作',
        x: 5.1, y: 1.1, w: 4.2, h: 0.8
    },
    {
        page: '库区管理',
        desc: '树形结构展示 + 层级管理',
        x: 5.1, y: 2.0, w: 4.2, h: 0.8
    },
    {
        page: 'AI助手',
        desc: '流式对话 + 上下文附件 + 会话管理',
        x: 5.1, y: 2.9, w: 4.2, h: 0.8
    },
    {
        page: '用户权限',
        desc: '多角色权限控制',
        x: 5.1, y: 3.8, w: 4.2, h: 0.8
    }
];

webFeatures.forEach(f => {
    slide13.addText(`<b>${f.page}:</b> ${f.desc}`, {
        x: f.x + 0.2, y: f.y + 0.25, w: f.w - 0.4, h: f.h - 0.3,
        fontSize: 16,
        color: COLOR_DARK
    });
    slide13.addShape(pres.shapes.ROUNDED_RECTANGLE, {
        x: f.x, y: f.y, w: f.w, h: f.h,
        fill: { color: COLOR_LIGHT_BG },
        line: { color: COLOR_SECONDARY, width: 1 }
    });
});

// 占位提示：请在此处插入截图
slide13.addText('（此处可插入系统界面截图）', {
    x: 0.6, y: 4.8, w: 8.8, h: 0.3,
    fontSize: 14,
    color: '888888',
    align: 'center',
    italic: true
});

// ==================== 幻灯片 14: 系统展示 - 小程序端 ====================
let slide14 = pres.addSlide();
slide14.background = { color: COLOR_LIGHT };

slide14.addText('7. 系统展示 - 微信小程序端', {
    x: 0.5, y: 0.3, w: 9.0, h: 0.5,
    fontSize: 32,
    bold: true,
    color: COLOR_PRIMARY
});

slide14.addShape(pres.shapes.RECTANGLE, {
    x: 0.5, y: 0.85, w: 9.0, h: 0.03,
    fill: { color: COLOR_PRIMARY }
});

const miniFeatures = [
    {
        page: '登录',
        desc: '微信一键授权登录',
        x: 0.6, y: 1.2, w: 4.2, h: 0.9
    },
    {
        page: '工作台',
        desc: '数据概览 + 待办统计 + 快捷入口',
        x: 5.1, y: 1.2, w: 4.2, h: 0.9
    },
    {
        page: '告警列表',
        desc: '查看告警 → 转工单 / 处理',
        x: 0.6, y: 2.3, w: 4.2, h: 0.9
    },
    {
        page: '工单列表',
        desc: '查看我的工单 → 处理操作',
        x: 5.1, y: 2.3, w: 4.2, h: 0.9
    },
    {
        page: '工单详情',
        desc: '详情查看 + 处理流程时间轴',
        x: 0.6, y: 3.4, w: 4.2, h: 0.9
    },
    {
        page: '个人中心',
        desc: '个人信息 + 账号绑定管理',
        x: 5.1, y: 3.4, w: 4.2, h: 0.9
    }
];

miniFeatures.forEach(f => {
    slide14.addText(`<b>${f.page}:</b> ${f.desc}`, {
        x: f.x + 0.2, y: f.y + 0.3, w: f.w - 0.4, h: f.h - 0.4,
        fontSize: 16,
        color: COLOR_DARK
    });
    slide14.addShape(pres.shapes.ROUNDED_RECTANGLE, {
        x: f.x, y: f.y, w: f.w, h: f.h,
        fill: { color: COLOR_LIGHT_BG },
        line: { color: COLOR_SECONDARY, width: 1 }
    });
});

slide14.addText('设计定位：员工移动端现场操作工具，方便随时随地处理告警和工单', {
    x: 0.6, y: 4.6, w: 8.8, h: 0.4,
    fontSize: 15,
    color: COLOR_DARK,
    align: 'center'
});

slide14.addText('（此处可插入小程序界面截图）', {
    x: 0.6, y: 5.0, w: 8.8, h: 0.3,
    fontSize: 14,
    color: '888888',
    align: 'center',
    italic: true
});

// ==================== 幻灯片 15: 创新点与特色 ====================
let slide15 = pres.addSlide();
slide15.background = { color: COLOR_LIGHT };

slide15.addText('8. 创新点与特色', {
    x: 0.5, y: 0.3, w: 9.0, h: 0.5,
    fontSize: 32,
    bold: true,
    color: COLOR_PRIMARY
});

slide15.addShape(pres.shapes.RECTANGLE, {
    x: 0.5, y: 0.85, w: 9.0, h: 0.03,
    fill: { color: COLOR_PRIMARY }
});

const innovations = [
    {
        title: '1. 大语言模型深度融合',
        desc: '将通义千问大语言模型集成到工业物联网监控系统中，\n通过轻量级RAG实现业务上下文注入，提供自然语言查询和智能分析能力',
        x: 0.6, y: 1.1, w: 8.8, h: 1.0
    },
    {
        title: '2. 智能告警多维度分析',
        desc: '不仅记录告警，还提供趋势分析、重复告警识别、设备健康度评分、根因分析，\n帮助管理人员快速定位问题根源',
        x: 0.6, y: 2.3, w: 8.8, h: 0.9
    },
    {
        title: '3. 完整闭环管理',
        desc: '从异常告警发现 → 转工单 → 分配处理 → 验收 → 关闭，形成完整管理闭环，\n每个操作都有日志记录，全程可追溯',
        x: 0.6, y: 3.4, w: 8.8, h: 0.9
    },
    {
        title: '4. 多端协同架构',
        desc: '管理员通过Web端进行系统配置和管理，员工通过微信小程序进行移动端现场作业，\n数据实时同步，分工明确',
        x: 0.6, y: 4.5, w: 8.8, h: 0.9
    }
];

innovations.forEach(item => {
    slide15.addText(item.title, {
        x: item.x + 0.1, y: item.y + 0.1, w: item.w - 0.2, h: 0.3,
        fontSize: 18,
        bold: true,
        color: COLOR_PRIMARY
    });
    slide15.addText(item.desc, {
        x: item.x + 0.1, y: item.y + 0.45, w: item.w - 0.2, h: item.h - 0.5,
        fontSize: 15,
        color: COLOR_DARK,
        lineSpacing: 1.4
    });
    slide15.addShape(pres.shapes.ROUNDED_RECTANGLE, {
        x: item.x, y: item.y, w: item.w, h: item.h,
        fill: { color: COLOR_LIGHT_BG },
        line: { color: COLOR_SECONDARY, width: 1 }
    });
});

// ==================== 幻灯片 16: 项目总结 ====================
let slide16 = pres.addSlide();
slide16.background = { color: COLOR_LIGHT };

slide16.addText('9. 总结与展望', {
    x: 0.5, y: 0.3, w: 9.0, h: 0.5,
    fontSize: 32,
    bold: true,
    color: COLOR_PRIMARY
});

slide16.addShape(pres.shapes.RECTANGLE, {
    x: 0.5, y: 0.85, w: 9.0, h: 0.03,
    fill: { color: COLOR_PRIMARY }
});

// 已完成工作
slide16.addShape(pres.shapes.ROUNDED_RECTANGLE, {
    x: 0.6, y: 1.1, w: 4.2, h: 2.0,
    fill: { color: COLOR_LIGHT_BG },
    line: { color: COLOR_PRIMARY, width: 2 }
});

slide16.addText('✅ 已完成工作', {
    x: 0.8, y: 1.2, w: 3.8, h: 0.35,
    fontSize: 20,
    bold: true,
    color: COLOR_PRIMARY
});

const completed = [
    '• 完成系统架构设计和数据库设计',
    '• 实现用户认证与权限管理',
    '• 实现库区和设备管理',
    '• 实现实时温湿度监控',
    '• 实现智能告警分析系统',
    '• 实现工单闭环管理',
    '• 集成通义千问大语言模型',
    '• 完成Web管理端和小程序开发'
];

slide16.addText(completed.join('\n'), {
    x: 0.8, y: 1.6, w: 3.8, h: 1.3,
    fontSize: 14,
    color: COLOR_DARK,
    lineSpacing: 1.5
});

// 未来展望
slide16.addShape(pres.shapes.ROUNDED_RECTANGLE, {
    x: 5.1, y: 1.1, w: 4.2, h: 2.0,
    fill: { color: COLOR_LIGHT_BG },
    line: { color: COLOR_PRIMARY, width: 2 }
});

slide16.addText('🔮 未来展望', {
    x: 5.3, y: 1.2, w: 3.8, h: 0.35,
    fontSize: 20,
    bold: true,
    color: COLOR_PRIMARY
});

const future = [
    '• 接入更多类型IoT设备',
    '• 预测性维护基于机器学习',
    '• 优化RAG使用向量数据库',
    '• 支持自动生成巡检报表',
    '• 移动端功能进一步完善',
    '• 添加数据可视化大屏',
    '• 性能优化和并发扩展',
    '• 完善单元测试覆盖'
];

slide16.addText(future.join('\n'), {
    x: 5.3, y: 1.6, w: 3.8, h: 1.3,
    fontSize: 14,
    color: COLOR_DARK,
    lineSpacing: 1.5
});

// 致谢
slide16.addText('🙏 感谢各位老师的聆听与指导！', {
    x: 0.6, y: 3.4, w: 8.8, h: 0.6,
    fontSize: 24,
    color: COLOR_PRIMARY,
    align: 'center',
    bold: true
});

// ==================== 保存PPT ====================
pres.writeFile('./coldchain-guardian-defense.pptx', (err) => {
    if (err) {
        console.error('Error generating PPT:', err);
    } else {
        console.log('PPT generated successfully: coldchain-guardian-defense.pptx');
    }
});
