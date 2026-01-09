
-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) DEFAULT '' COMMENT '昵称',
    email VARCHAR(100) DEFAULT '' COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT '' COMMENT '手机号',
    status INT DEFAULT 1 COMMENT '状态(1:正常,0:禁用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    description VARCHAR(200) DEFAULT '' COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 菜单/权限表
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    path VARCHAR(200) DEFAULT '' COMMENT '路由路径',
    component VARCHAR(200) DEFAULT '' COMMENT '组件路径',
    perms VARCHAR(100) DEFAULT '' COMMENT '权限标识',
    icon VARCHAR(100) DEFAULT '' COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    menu_type INT DEFAULT 1 COMMENT '类型(0:目录,1:菜单,2:按钮)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 初始化数据
INSERT INTO sys_user (username, password, nickname, status) 
SELECT 'admin', '$2a$10$u.bP1mZcpRpU2zb/C7MbwO6TaeIXHPJUW8xJ1mOL4qWke1Hhu27ca', '管理员', 1 
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'admin');

-- 初始化角色
INSERT INTO sys_role (role_name, role_code, description)
SELECT '管理员', 'ROLE_ADMIN', '系统管理员'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_code = 'ROLE_ADMIN');

INSERT INTO sys_role (role_name, role_code, description)
SELECT '普通用户', 'ROLE_USER', '普通用户'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_code = 'ROLE_USER');

CREATE TABLE IF NOT EXISTS organization (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    parent_id BIGINT NULL COMMENT '父级机构ID',
    code VARCHAR(32) NOT NULL COMMENT '机构编码（行政区划代码）',
    name VARCHAR(128) NOT NULL COMMENT '机构名称',
    level TINYINT NOT NULL COMMENT '级别：1省、2市、3县、4乡镇、5社区',
    data_source VARCHAR(32) NOT NULL COMMENT '来源：COMMUNITY/TOWNSHIP 等',
    province_name VARCHAR(128) NULL COMMENT '省名称',
    city_name VARCHAR(128) NULL COMMENT '市名称',
    county_name VARCHAR(128) NULL COMMENT '县名称',
    township_name VARCHAR(128) NULL COMMENT '乡镇名称',
    community_name VARCHAR(128) NULL COMMENT '社区名称',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_organization_code (code),
    KEY idx_organization_parent (parent_id),
    KEY idx_organization_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='组织机构表';

INSERT INTO organization (parent_id, code, name, level, data_source, province_name, city_name, county_name, township_name, community_name, is_deleted)
SELECT NULL, '51', '四川省', 1, 'TOWNSHIP', '四川省', NULL, NULL, NULL, NULL, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM organization WHERE code = '51');

INSERT INTO organization (parent_id, code, name, level, data_source, province_name, city_name, county_name, township_name, community_name, is_deleted)
SELECT p.id, '5114', '眉山市', 2, 'TOWNSHIP', '四川省', '眉山市', NULL, NULL, NULL, 0
FROM organization p
WHERE p.code = '51'
AND NOT EXISTS (SELECT 1 FROM organization WHERE code = '5114');

INSERT INTO organization (parent_id, code, name, level, data_source, province_name, city_name, county_name, township_name, community_name, is_deleted)
SELECT c.id, '511425', '青神县', 3, 'TOWNSHIP', '四川省', '眉山市', '青神县', NULL, NULL, 0
FROM organization c
WHERE c.code = '5114'
AND NOT EXISTS (SELECT 1 FROM organization WHERE code = '511425');

CREATE TABLE IF NOT EXISTS sys_role_organization (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    organization_id BIGINT NOT NULL COMMENT '机构ID',
    PRIMARY KEY (role_id, organization_id),
    KEY idx_role_id (role_id),
    KEY idx_organization_id (organization_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-机构关联表';

INSERT INTO sys_role_organization (role_id, organization_id)
SELECT r.id, o.id
FROM sys_role r, organization o
WHERE r.role_code = 'ROLE_ADMIN'
  AND o.code = '51'
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_organization sro WHERE sro.role_id = r.id AND sro.organization_id = o.id
  );

-- 关联管理员用户和角色
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id 
FROM sys_user u, sys_role r 
WHERE u.username = 'admin' AND r.role_code = 'ROLE_ADMIN'
AND NOT EXISTS (SELECT 1 FROM sys_user_role sur WHERE sur.user_id = u.id AND sur.role_id = r.id);
