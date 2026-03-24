-- 更新管理员密码为MD5格式
-- 密码: admin123
-- MD5: 0192023a7bbd73250516f069df18b500

UPDATE srm_user SET password = '0192023a7bbd73250516f069df18b500' WHERE username = 'admin';

-- 验证更新
SELECT username, name, role, password FROM srm_user WHERE username = 'admin';
