-- ----------------------------
-- 1. AI 智能助手：历史会话表 (Chat Sessions)
-- 作用：对应前端左侧的会话列表，每次点击"新建洞察"就在此表生成一条记录
-- ----------------------------
DROP TABLE IF EXISTS `ai_chat_sessions`;
CREATE TABLE `ai_chat_sessions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '所属用户ID (关联 users 表)',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '新对话' COMMENT '会话标题(由AI根据首轮对话自动生成总结)',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否已删除(1是 0否，用于前端历史列表的软删除)',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '会话创建时间',
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后对话时间(用于列表排序)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_time`(`user_id` ASC, `updated_time` DESC) USING BTREE COMMENT '用于快速拉取某用户的历史会话列表'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI助手-会话表';


-- ----------------------------
-- 2. AI 智能助手：消息明细表 (Chat Messages)
-- 作用：存储每个会话中的具体一问一答，是 Spring AI 记忆（Memory）的数据来源
-- ----------------------------
DROP TABLE IF EXISTS `ai_chat_messages`;
CREATE TABLE `ai_chat_messages` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL COMMENT '所属会话ID (关联 ai_chat_sessions 表)',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色类型：USER(用户提问), ASSISTANT(AI回答), SYSTEM(系统预设)',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容(支持Markdown格式)',

  -- 以下两个字段用于实现我们设计的【上下文动态注入 (RAG)】功能
  `attachment_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联附件类型(DEVICE/ALERT/WORK_ORDER/AREA)',
  `attachment_id` bigint NULL DEFAULT NULL COMMENT '关联附件的业务ID',

  `tokens_used` int NULL DEFAULT 0 COMMENT '消耗的Token数量(可选，用于后续统计大模型API成本)',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消息发送时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_session_time`(`session_id` ASC, `created_time` ASC) USING BTREE COMMENT '用于按时间顺序拉取某个会话的所有聊天记录',
  CONSTRAINT `fk_msg_session` FOREIGN KEY (`session_id`) REFERENCES `ai_chat_sessions` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI助手-消息明细表';