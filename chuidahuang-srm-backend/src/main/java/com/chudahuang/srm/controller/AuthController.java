package com.chudahuang.srm.controller;

import com.chudahuang.srm.common.Result;
import com.chudahuang.srm.dto.LoginRequest;
import com.chudahuang.srm.dto.LoginResponse;
import com.chudahuang.srm.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "认证管理", description = "用户登录、登出等接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return Result.success("登录成功", response);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/user-info")
    public Result<LoginResponse> getUserInfo(@RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return Result.fail("未登录");
            }
            String token = authorization.substring(7);
            if (!authService.validateToken(token)) {
                return Result.fail("Token已过期");
            }
            LoginResponse response = authService.getUserInfoFromToken(token);
            return Result.success(response);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @Operation(summary = "验证Token")
    @GetMapping("/validate")
    public Result<Boolean> validateToken(@RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return Result.success(false);
            }
            String token = authorization.substring(7);
            boolean valid = authService.validateToken(token);
            return Result.success(valid);
        } catch (Exception e) {
            return Result.success(false);
        }
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        // 前端负责清除Token，后端只需要返回成功
        return Result.success("登出成功", null);
    }
}
