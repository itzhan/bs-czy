package com.campus.zhihu.controller;

import com.campus.zhihu.common.Result;
import com.campus.zhihu.dto.LoginDTO;
import com.campus.zhihu.dto.RegisterDTO;
import com.campus.zhihu.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/auth") @RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginDTO dto) { return Result.success(authService.login(dto)); }

    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterDTO dto) { return Result.success(authService.register(dto)); }

    @GetMapping("/current")
    public Result<?> current(Authentication auth) { return Result.success(authService.getCurrentUser((Long) auth.getPrincipal())); }
}
