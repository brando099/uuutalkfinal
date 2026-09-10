-- 添加账号功能升级：执行一次即可。
ALTER TABLE sys_user
    ADD COLUMN parent_username VARCHAR(100) NULL COMMENT '父账号用户名' AFTER username;
