package com.campus.zhihu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.zhihu.common.BusinessException;
import com.campus.zhihu.dto.LoginDTO;
import com.campus.zhihu.dto.RegisterDTO;
import com.campus.zhihu.entity.User;
import com.campus.zhihu.mapper.UserMapper;
import com.campus.zhihu.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public Map<String, Object> login(LoginDTO dto) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() == 0) throw new BusinessException(403, "账号已被禁用");
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", sanitizeUser(user));
        return result;
    }

    public User register(RegisterDTO dto) {
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername())) > 0) {
            throw new BusinessException("用户名已存在");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole() != null ? dto.getRole() : 0);
        user.setDepartment(dto.getDepartment());
        user.setStatus(1);
        userMapper.insert(user);
        return sanitizeUser(user);
    }

    public User getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        return sanitizeUser(user);
    }

    private User sanitizeUser(User user) {
        user.setPassword(null);
        return user;
    }
}
