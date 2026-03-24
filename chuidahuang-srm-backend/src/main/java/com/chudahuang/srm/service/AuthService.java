package com.chudahuang.srm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chudahuang.srm.dto.LoginRequest;
import com.chudahuang.srm.dto.LoginResponse;
import com.chudahuang.srm.entity.User;
import com.chudahuang.srm.mapper.UserMapper;
import com.chudahuang.srm.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 认证服务
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 验证密码（简单MD5验证，生产环境建议使用BCrypt）
        String inputPassword = cn.hutool.crypto.digest.DigestUtil.md5Hex(request.getPassword());
        if (!inputPassword.equals(user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 检查用户状态
        if (!"在职".equals(user.getStatus())) {
            throw new RuntimeException("用户状态异常，无法登录");
        }

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 生成Token
        String token = jwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getRole(),
                user.getDepartment()
        );

        // 获取头像（取姓名第一个字）
        String avatar = user.getName() != null ? user.getName().substring(0, 1) : "用";

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole())
                .department(user.getDepartment())
                .avatar(avatar)
                .build();
    }

    /**
     * 验证Token
     */
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    /**
     * 从Token获取用户信息
     */
    public LoginResponse getUserInfoFromToken(String token) {
        return LoginResponse.builder()
                .userId(jwtUtil.getUserIdFromToken(token))
                .username(jwtUtil.getUsernameFromToken(token))
                .name(jwtUtil.getNameFromToken(token))
                .role(jwtUtil.getRoleFromToken(token))
                .department(jwtUtil.getDepartmentFromToken(token))
                .build();
    }
}
