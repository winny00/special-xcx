-- ----------------------------
-- 特殊教育平台业务表 + 角色权限初始化
-- 在 ry_vue.sql 执行后运行本脚本
-- ----------------------------

-- 特教资源表
CREATE TABLE IF NOT EXISTS special_resource (
    id              bigint(20)      NOT NULL                    COMMENT '主键',
    title           varchar(200)    NOT NULL                    COMMENT '资源标题',
    resource_type   varchar(32)     NOT NULL                    COMMENT '资源类型 course/tool/teacher/org/assessment',
    category        varchar(64)     DEFAULT NULL                COMMENT '分类 感统/语言/社交/行为干预等',
    summary         varchar(500)    DEFAULT NULL                COMMENT '摘要',
    content         text                                        COMMENT '详情内容',
    cover_url       varchar(500)    DEFAULT NULL                COMMENT '封面图',
    org_id          bigint(20)      DEFAULT NULL                COMMENT '所属机构ID',
    provider_name   varchar(100)    DEFAULT NULL                COMMENT '提供者名称',
    contact_phone   varchar(20)     DEFAULT NULL                COMMENT '联系电话',
    region          varchar(100)    DEFAULT NULL                COMMENT '服务区域',
    price           decimal(10,2)   DEFAULT 0.00                COMMENT '参考价格',
    status          tinyint         DEFAULT 0                   COMMENT '状态 0草稿 1已发布 2已下架',
    view_count      int(11)         DEFAULT 0                   COMMENT '浏览量',
    create_dept     bigint(20)      DEFAULT NULL                COMMENT '创建部门',
    create_by       bigint(20)      DEFAULT NULL                COMMENT '创建者',
    create_time     datetime        DEFAULT NULL                COMMENT '创建时间',
    update_by       bigint(20)      DEFAULT NULL                COMMENT '更新者',
    update_time     datetime        DEFAULT NULL                COMMENT '更新时间',
    del_flag        char(1)         DEFAULT '0'                 COMMENT '删除标志',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='特教资源表';

-- 机构/学校表
CREATE TABLE IF NOT EXISTS special_organization (
    id              bigint(20)      NOT NULL                    COMMENT '主键',
    name            varchar(200)    NOT NULL                    COMMENT '机构名称',
    org_type        varchar(32)     NOT NULL                    COMMENT '类型 org/school',
    license_no      varchar(100)    DEFAULT NULL                COMMENT '资质编号',
    license_url     varchar(500)    DEFAULT NULL                COMMENT '资质证照',
    address         varchar(300)    DEFAULT NULL                COMMENT '地址',
    region          varchar(100)    DEFAULT NULL                COMMENT '所在区域',
    contact_name    varchar(50)     DEFAULT NULL                COMMENT '联系人',
    contact_phone   varchar(20)     DEFAULT NULL                COMMENT '联系电话',
    description     text                                        COMMENT '机构简介',
    audit_status    tinyint         DEFAULT 0                 COMMENT '审核状态 0待审 1通过 2拒绝',
    status          tinyint         DEFAULT 1                 COMMENT '状态 0停用 1正常',
    create_dept     bigint(20)      DEFAULT NULL                COMMENT '创建部门',
    create_by       bigint(20)      DEFAULT NULL                COMMENT '创建者',
    create_time     datetime        DEFAULT NULL                COMMENT '创建时间',
    update_by       bigint(20)      DEFAULT NULL                COMMENT '更新者',
    update_time     datetime        DEFAULT NULL                COMMENT '更新时间',
    del_flag        char(1)         DEFAULT '0'                 COMMENT '删除标志',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='特教机构学校表';

-- 预约/咨询申请表
CREATE TABLE IF NOT EXISTS special_appointment (
    id              bigint(20)      NOT NULL                    COMMENT '主键',
    resource_id     bigint(20)      DEFAULT NULL                COMMENT '关联资源ID',
    resource_title  varchar(200)    DEFAULT NULL                COMMENT '资源标题快照',
    user_id         bigint(20)      DEFAULT NULL                COMMENT '申请人用户ID',
    teacher_id      bigint(20)      DEFAULT NULL                COMMENT '老师档案ID',
    contact_name    varchar(50)     NOT NULL                    COMMENT '联系人',
    contact_phone   varchar(20)     NOT NULL                    COMMENT '联系电话',
    child_age       varchar(20)     DEFAULT NULL                COMMENT '儿童年龄',
    remark          varchar(500)    DEFAULT NULL                COMMENT '需求说明',
    appoint_status  tinyint         DEFAULT 0                 COMMENT '状态 0待处理 1已联系 2已完成 3已取消',
    handler_id      bigint(20)      DEFAULT NULL                COMMENT '处理人',
    handler_remark  varchar(500)    DEFAULT NULL                COMMENT '处理备注',
    create_dept     bigint(20)      DEFAULT NULL                COMMENT '创建部门',
    create_by       bigint(20)      DEFAULT NULL                COMMENT '创建者',
    create_time     datetime        DEFAULT NULL                COMMENT '创建时间',
    update_by       bigint(20)      DEFAULT NULL                COMMENT '更新者',
    update_time     datetime        DEFAULT NULL                COMMENT '更新时间',
    del_flag        char(1)         DEFAULT '0'                 COMMENT '删除标志',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='特教预约咨询表';

-- 小程序客户端（grant_type 含 xcx）
INSERT INTO sys_client VALUES (
    1762000000000000003, 'special_xcx_client_id', 'xcx', 'special_xcx_secret',
    'password,xcx,sms', 'xcx', null, null, 1800, 604800, '0', '0',
    1761000000000000103, 1761100000000000001, sysdate(), 1761100000000000001, sysdate()
) ON DUPLICATE KEY UPDATE client_key = 'xcx';

-- 四类角色
INSERT INTO sys_role VALUES (1763000000000000001, '家长', 'special_parent', 3, '5', 1, 1, '0', '0', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '自闭症家庭家长角色') ON DUPLICATE KEY UPDATE role_name = '家长';
INSERT INTO sys_role VALUES (1763000000000000002, '特教老师', 'special_teacher', 4, '5', 1, 1, '0', '0', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '特教老师角色') ON DUPLICATE KEY UPDATE role_name = '特教老师';
INSERT INTO sys_role VALUES (1763000000000000003, '机构管理员', 'special_org_admin', 5, '5', 1, 1, '0', '0', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '康复机构管理员') ON DUPLICATE KEY UPDATE role_name = '机构管理员';
INSERT INTO sys_role VALUES (1763000000000000004, '学校管理员', 'special_school_admin', 6, '5', 1, 1, '0', '0', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '特教学校管理员') ON DUPLICATE KEY UPDATE role_name = '学校管理员';

-- 顶级菜单：特教平台
INSERT INTO sys_menu VALUES (1764000000000000001, '特教平台', 0, 6, 'special', null, '', 'N', 'Y', 'M', '0', '0', '', 'education', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '特殊教育平台') ON DUPLICATE KEY UPDATE menu_name = '特教平台';

INSERT INTO sys_menu VALUES (1764000000000000002, '资源管理', 1764000000000000001, 1, 'resource', 'special/resource/index', '', 'N', 'Y', 'C', '0', '0', 'special:resource:list', 'documentation', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '资源管理';
INSERT INTO sys_menu VALUES (1764000000000000003, '机构管理', 1764000000000000001, 2, 'organization', 'special/organization/index', '', 'N', 'Y', 'C', '0', '0', 'special:organization:list', 'tree', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '机构管理';
INSERT INTO sys_menu VALUES (1764000000000000004, '预约管理', 1764000000000000001, 3, 'appointment', 'special/appointment/index', '', 'N', 'Y', 'C', '0', '0', 'special:appointment:list', 'form', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '预约管理';

-- 资源管理按钮权限
INSERT INTO sys_menu VALUES (1764000000000000011, '资源查询', 1764000000000000002, 1, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:resource:query', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '资源查询';
INSERT INTO sys_menu VALUES (1764000000000000012, '资源新增', 1764000000000000002, 2, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:resource:add', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '资源新增';
INSERT INTO sys_menu VALUES (1764000000000000013, '资源修改', 1764000000000000002, 3, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:resource:edit', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '资源修改';
INSERT INTO sys_menu VALUES (1764000000000000014, '资源删除', 1764000000000000002, 4, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:resource:remove', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '资源删除';

-- 机构管理按钮权限
INSERT INTO sys_menu VALUES (1764000000000000021, '机构查询', 1764000000000000003, 1, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:organization:query', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '机构查询';
INSERT INTO sys_menu VALUES (1764000000000000022, '机构新增', 1764000000000000003, 2, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:organization:add', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '机构新增';
INSERT INTO sys_menu VALUES (1764000000000000023, '机构修改', 1764000000000000003, 3, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:organization:edit', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '机构修改';
INSERT INTO sys_menu VALUES (1764000000000000024, '机构删除', 1764000000000000003, 4, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:organization:remove', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '机构删除';

-- 预约管理按钮权限
INSERT INTO sys_menu VALUES (1764000000000000031, '预约查询', 1764000000000000004, 1, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:appointment:query', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '预约查询';
INSERT INTO sys_menu VALUES (1764000000000000032, '预约处理', 1764000000000000004, 2, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:appointment:edit', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '预约处理';
INSERT INTO sys_menu VALUES (1764000000000000033, '预约删除', 1764000000000000004, 3, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:appointment:remove', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '预约删除';

-- 数据概览
INSERT INTO sys_menu VALUES (1764000000000000005, '数据概览', 1764000000000000001, 0, 'dashboard', 'special/dashboard/index', '', 'N', 'Y', 'C', '0', '0', 'special:dashboard:view', 'dashboard', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '工作台概览') ON DUPLICATE KEY UPDATE menu_name = '数据概览';
INSERT INTO sys_menu VALUES (1764000000000000040, '概览查看', 1764000000000000005, 1, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:dashboard:view', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '概览查看';

-- 字典：资源类型
INSERT INTO sys_dict_type VALUES (1765000000000000001, '特教资源类型', 'special_resource_type', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '特教资源类型') ON DUPLICATE KEY UPDATE dict_name = '特教资源类型';
INSERT INTO sys_dict_data VALUES (1765000000000000011, 1, '课程', 'course', 'special_resource_type', '', 'primary', 'N', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE dict_label = '课程';
INSERT INTO sys_dict_data VALUES (1765000000000000012, 2, '工具', 'tool', 'special_resource_type', '', 'success', 'N', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE dict_label = '工具';
INSERT INTO sys_dict_data VALUES (1765000000000000013, 3, '老师', 'teacher', 'special_resource_type', '', 'info', 'N', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE dict_label = '老师';
INSERT INTO sys_dict_data VALUES (1765000000000000014, 4, '机构', 'org', 'special_resource_type', '', 'warning', 'N', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE dict_label = '机构';
INSERT INTO sys_dict_data VALUES (1765000000000000015, 5, '评估', 'assessment', 'special_resource_type', '', 'danger', 'N', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE dict_label = '评估';

-- 示例数据
INSERT INTO special_resource VALUES
(1766000000000000001, '感觉统合训练入门课', 'course', '感统', '面向自闭症儿童的感统训练基础课程', '系统介绍感统训练理论与家庭练习方法。', null, null, '阳光特教中心', '13800000001', '广州市', 299.00, 1, 0, 1761000000000000103, 1761100000000000001, sysdate(), null, null, '0'),
(1766000000000000002, '社交故事工具包', 'tool', '社交', '帮助儿童理解社交场景的图文工具', '包含20套常用社交故事模板。', null, null, '星语康复', '13800000002', '深圳市', 0.00, 1, 0, 1761000000000000103, 1761100000000000001, sysdate(), null, null, '0'),
(1766000000000000003, '语言干预专家-李老师', 'teacher', '语言', '10年特教语言干预经验', '擅长儿童语言发育迟缓与AAC辅助沟通。', null, null, '李老师', '13800000003', '北京市', 500.00, 1, 0, 1761000000000000103, 1761100000000000001, sysdate(), null, null, '0');

INSERT INTO special_organization VALUES
(1767000000000000001, '阳光特教中心', 'org', 'GZ2024001', null, '广州市天河区体育西路100号', '广州市', '张主任', '13800000001', '专注自闭症儿童康复训练', 1, 1, 1761000000000000103, 1761100000000000001, sysdate(), null, null, '0'),
(1767000000000000002, '希望特教学校', 'school', 'SZ2024002', null, '深圳市南山区科技园路88号', '深圳市', '王校长', '13800000002', '融合教育示范学校', 1, 1, 1761000000000000103, 1761100000000000001, sysdate(), null, null, '0');

-- phase2a-cos: 机构封面
ALTER TABLE special_organization
  ADD COLUMN IF NOT EXISTS cover_url varchar(500) DEFAULT NULL COMMENT '封面图' AFTER description;

-- 特教管理员复用 OSS 上传（RuoYi 菜单 118 文件管理下 OSS 权限）
-- 若 admin 角色 role_id=1 已有 *:*:* 可跳过；否则插入 role_menu 关联 system:oss:upload

-- phase2a-article: 资讯/政策表
CREATE TABLE IF NOT EXISTS special_article (
    id              bigint(20)      NOT NULL                    COMMENT '主键',
    title           varchar(200)    NOT NULL                    COMMENT '标题',
    summary         varchar(500)    DEFAULT NULL                COMMENT '摘要',
    content         text                                        COMMENT '正文 HTML',
    cover_url       varchar(500)    DEFAULT NULL                COMMENT '封面图',
    category        varchar(32)     DEFAULT NULL                COMMENT '分类 policy/news/guide',
    status          tinyint         DEFAULT 0                   COMMENT '状态 0草稿 1已发布 2已下架',
    publish_time    datetime        DEFAULT NULL                COMMENT '发布时间',
    view_count      int(11)         DEFAULT 0                   COMMENT '浏览量',
    create_dept     bigint(20)      DEFAULT NULL                COMMENT '创建部门',
    create_by       bigint(20)      DEFAULT NULL                COMMENT '创建者',
    create_time     datetime        DEFAULT NULL                COMMENT '创建时间',
    update_by       bigint(20)      DEFAULT NULL                COMMENT '更新者',
    update_time     datetime        DEFAULT NULL                COMMENT '更新时间',
    del_flag        char(1)         DEFAULT '0'                 COMMENT '删除标志',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='特教资讯政策表';

INSERT INTO sys_menu VALUES (1764000000000000006, '资讯管理', 1764000000000000001, 3, 'article', 'special/article/index', '', 'N', 'Y', 'C', '0', '0', 'special:article:list', 'documentation', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '资讯管理';
INSERT INTO sys_menu VALUES (1764000000000000051, '资讯查询', 1764000000000000006, 1, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:article:query', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '资讯查询';
INSERT INTO sys_menu VALUES (1764000000000000052, '资讯新增', 1764000000000000006, 2, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:article:add', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '资讯新增';
INSERT INTO sys_menu VALUES (1764000000000000053, '资讯修改', 1764000000000000006, 3, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:article:edit', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '资讯修改';
INSERT INTO sys_menu VALUES (1764000000000000054, '资讯删除', 1764000000000000006, 4, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:article:remove', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '资讯删除';

-- phase2b-parent-crm: 家长管理（只读）
INSERT INTO sys_menu VALUES (1764000000000000007, '家长管理', 1764000000000000001, 4, 'parent', 'special/parent/index', '', 'N', 'Y', 'C', '0', '0', 'special:parent:list', 'user', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '家长管理';
INSERT INTO sys_menu VALUES (1764000000000000061, '家长查询', 1764000000000000007, 1, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:parent:query', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '家长查询';
INSERT INTO sys_menu VALUES (1764000000000000062, '家长列表', 1764000000000000007, 2, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:parent:list', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '家长列表';

-- phase2b-teacher-audit: 老师档案 + 审核备注
CREATE TABLE IF NOT EXISTS special_teacher (
    id              bigint(20)      NOT NULL                    COMMENT '主键',
    user_id         bigint(20)      DEFAULT NULL                COMMENT '关联用户',
    name            varchar(100)    NOT NULL                    COMMENT '姓名',
    title           varchar(100)    DEFAULT NULL                COMMENT '职称头衔',
    specialties     varchar(500)    DEFAULT NULL                COMMENT '擅长领域',
    qualification   varchar(500)    DEFAULT NULL                COMMENT '资质说明',
    cert_image_url  varchar(500)    DEFAULT NULL                COMMENT '证书图',
    avatar_url      varchar(500)    DEFAULT NULL                COMMENT '头像',
    org_id          bigint(20)      DEFAULT NULL                COMMENT '所属机构',
    intro           text                                        COMMENT '简介',
    status          tinyint         DEFAULT 0                   COMMENT '状态 0待审 1通过 2拒绝',
    resource_id     bigint(20)      DEFAULT NULL                COMMENT '关联资源',
    audit_remark    varchar(500)    DEFAULT NULL                COMMENT '审核备注',
    audit_by        bigint(20)      DEFAULT NULL                COMMENT '审核人',
    audit_time      datetime        DEFAULT NULL                COMMENT '审核时间',
    create_dept     bigint(20)      DEFAULT NULL                COMMENT '创建部门',
    create_by       bigint(20)      DEFAULT NULL                COMMENT '创建者',
    create_time     datetime        DEFAULT NULL                COMMENT '创建时间',
    update_by       bigint(20)      DEFAULT NULL                COMMENT '更新者',
    update_time     datetime        DEFAULT NULL                COMMENT '更新时间',
    del_flag        char(1)         DEFAULT '0'                 COMMENT '删除标志',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='特教老师档案表';

INSERT INTO sys_menu VALUES (1764000000000000008, '老师档案', 1764000000000000001, 5, 'teacher', 'special/teacher/index', '', 'N', 'Y', 'C', '0', '0', 'special:teacher:list', 'peoples', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '老师档案';
INSERT INTO sys_menu VALUES (1764000000000000071, '老师查询', 1764000000000000008, 1, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:teacher:query', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '老师查询';
INSERT INTO sys_menu VALUES (1764000000000000072, '老师新增', 1764000000000000008, 2, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:teacher:add', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '老师新增';
INSERT INTO sys_menu VALUES (1764000000000000073, '老师修改', 1764000000000000008, 3, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:teacher:edit', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '老师修改';
INSERT INTO sys_menu VALUES (1764000000000000074, '老师删除', 1764000000000000008, 4, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:teacher:remove', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '老师删除';
INSERT INTO sys_menu VALUES (1764000000000000075, '老师列表', 1764000000000000008, 5, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:teacher:list', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '老师列表';

INSERT INTO sys_menu VALUES (1764000000000000009, '用户角色', 1764000000000000001, 6, 'account', 'special/account/index', '', 'N', 'Y', 'C', '0', '0', 'special:account:list', 'peoples', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '用户角色';
INSERT INTO sys_menu VALUES (1764000000000000081, '用户角色查询', 1764000000000000009, 1, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:account:list', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '用户角色查询';
INSERT INTO sys_menu VALUES (1764000000000000082, '用户角色编辑', 1764000000000000009, 2, '', '', '', 'N', 'Y', 'F', '0', '0', 'special:account:edit', '#', '', '', 1761000000000000103, 1761100000000000001, sysdate(), null, null, '') ON DUPLICATE KEY UPDATE menu_name = '用户角色编辑';

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1763000000000000002, 1764000000000000071 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1763000000000000002 AND menu_id = 1764000000000000071);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1763000000000000002, 1764000000000000073 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1763000000000000002 AND menu_id = 1764000000000000073);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1763000000000000002, 1764000000000000004 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1763000000000000002 AND menu_id = 1764000000000000004);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1763000000000000002, 1764000000000000031 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1763000000000000002 AND menu_id = 1764000000000000031);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1763000000000000002, 1764000000000000032 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1763000000000000002 AND menu_id = 1764000000000000032);

INSERT INTO special_article VALUES
(1768000000000000001, '广东省特殊教育提升计划解读', '梳理省内特教资源布局与入学支持政策要点。', '<p>本文介绍广东省特殊教育提升计划的核心目标：扩大特教学位供给、加强融合教育支持、完善评估与转衔服务。</p><p>家长可关注当地教育局发布的入学指南与康复补贴申请渠道。</p>', null, 'policy', 1, sysdate(), 128, 1761000000000000103, 1761100000000000001, sysdate(), null, null, '0'),
(1768000000000000002, '自闭症儿童家庭支持服务指南', '为新手家长整理评估、干预与社区资源对接路径。', '<p>建议家长优先完成发育评估，再根据评估结果选择感统、语言或行为干预课程。</p><p>本平台可预约咨询，对接机构与老师资源。</p>', null, 'guide', 1, sysdate(), 86, 1761000000000000103, 1761100000000000001, sysdate(), null, null, '0'),
(1768000000000000003, '平台上线：特教资源一站式预约', '课程、老师、机构资源集中展示，支持在线预约咨询。', '<p>特教资源平台现已上线，家长可在首页浏览推荐资源并提交预约。</p>', null, 'news', 1, sysdate(), 42, 1761000000000000103, 1761100000000000001, sysdate(), null, null, '0');
