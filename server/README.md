# 特教平台后端

基于 [RuoYi-Vue-Plus](https://gitee.com/dromara/RuoYi-Vue-Plus) 6.x，扩展 `ruoyi-special` 业务模块。

## 业务模块

- `ruoyi-special` — 特教资源、机构、预约 CRUD + 移动端 API

## 配置

1. 复制 `ruoyi-admin/src/main/resources/application-dev.yml` 配置 MySQL / Redis
2. 设置微信小程序：

```yaml
special:
  wechat:
    app-id: 你的AppID
    app-secret: 你的AppSecret
```

## 启动

```bash
# 导入 SQL
mysql -u root -p ruoyi_vue < script/sql/ry_vue.sql
mysql -u root -p ruoyi_vue < script/sql/ry_special.sql

./mvnw spring-boot:run -pl ruoyi-admin
```

API 文档：http://localhost:8080/doc.html

## 角色

| role_key | 说明 |
|----------|------|
| special_parent | 家长 |
| special_teacher | 特教老师 |
| special_org_admin | 机构管理员 |
| special_school_admin | 学校管理员 |
