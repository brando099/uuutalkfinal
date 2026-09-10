-- 套餐管理功能升级：执行一次即可。
ALTER TABLE user_package
    ADD COLUMN package_name VARCHAR(100) NOT NULL DEFAULT '普通套餐' COMMENT '套餐名称' AFTER account_id;
