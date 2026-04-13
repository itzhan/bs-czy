-- AI 配置表
CREATE TABLE IF NOT EXISTS system_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT,
    description VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 默认 AI 配置
INSERT INTO system_config (config_key, config_value, description) VALUES
('ai_api_key', '', '硅基流动 API Key'),
('ai_model', 'deepseek-ai/DeepSeek-V2.5', 'AI 模型名称'),
('ai_base_url', 'https://api.siliconflow.cn/v1', 'AI API 地址'),
('ai_system_prompt', '你是一个校园知识问答助手。请根据用户的问题，给出详细、准确、有帮助的回答。回答要条理清晰，如果涉及多个要点，请使用编号列表格式。', 'AI 系统提示词')
ON DUPLICATE KEY UPDATE config_key = config_key;
