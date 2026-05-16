-- ============================================
-- 微信协作记账小程序 数据库初始化脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS wechat_collab DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE wechat_collab;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
  id VARCHAR(36) PRIMARY KEY,
  openid VARCHAR(64) NOT NULL UNIQUE,
  session_key VARCHAR(128),
  nick_name VARCHAR(64) DEFAULT '',
  avatar_url VARCHAR(512) DEFAULT '',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_openid (openid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 账本表
CREATE TABLE IF NOT EXISTS ledgers (
  id VARCHAR(36) PRIMARY KEY,
  name VARCHAR(64) NOT NULL,
  description VARCHAR(256) DEFAULT '',
  owner_id VARCHAR(36) NOT NULL,
  invite_code VARCHAR(16) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_owner (owner_id),
  INDEX idx_invite_code (invite_code),
  FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 账本成员表
CREATE TABLE IF NOT EXISTS ledger_members (
  ledger_id VARCHAR(36) NOT NULL,
  user_id VARCHAR(36) NOT NULL,
  role ENUM('owner', 'admin', 'member') NOT NULL DEFAULT 'member',
  joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (ledger_id, user_id),
  INDEX idx_user (user_id),
  FOREIGN KEY (ledger_id) REFERENCES ledgers(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 分类表
CREATE TABLE IF NOT EXISTS categories (
  id VARCHAR(36) PRIMARY KEY,
  name VARCHAR(32) NOT NULL,
  type ENUM('income', 'expense') NOT NULL,
  icon VARCHAR(64) DEFAULT 'default',
  sort_order INT DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 账目记录表
CREATE TABLE IF NOT EXISTS records (
  id VARCHAR(36) PRIMARY KEY,
  ledger_id VARCHAR(36) NOT NULL,
  user_id VARCHAR(36) NOT NULL,
  amount DECIMAL(12, 2) NOT NULL,
  type ENUM('income', 'expense') NOT NULL,
  category_id VARCHAR(36) DEFAULT NULL,
  description VARCHAR(256) DEFAULT '',
  record_date DATE NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_ledger (ledger_id),
  INDEX idx_user (user_id),
  INDEX idx_date (record_date),
  INDEX idx_category (category_id),
  FOREIGN KEY (ledger_id) REFERENCES ledgers(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 插入默认分类数据
-- ============================================

INSERT INTO categories (id, name, type, icon, sort_order) VALUES
-- 支出分类
('cat-expense-food', '餐饮', 'expense', 'food', 1),
('cat-expense-transport', '交通', 'expense', 'transport', 2),
('cat-expense-shopping', '购物', 'expense', 'shopping', 3),
('cat-expense-entertainment', '娱乐', 'expense', 'entertainment', 4),
('cat-expense-housing', '住房', 'expense', 'housing', 5),
('cat-expense-medical', '医疗', 'expense', 'medical', 6),
('cat-expense-education', '教育', 'expense', 'education', 7),
('cat-expense-travel', '旅行', 'expense', 'travel', 8),
('cat-expense-other', '其他支出', 'expense', 'other', 99),
-- 收入分类
('cat-income-salary', '工资', 'income', 'salary', 1),
('cat-income-bonus', '奖金', 'income', 'bonus', 2),
('cat-income-invest', '投资收益', 'income', 'invest', 3),
('cat-income-parttime', '兼职', 'income', 'parttime', 4),
('cat-income-other', '其他收入', 'income', 'other', 99);
