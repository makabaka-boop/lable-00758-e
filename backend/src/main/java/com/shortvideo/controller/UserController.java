package com.shortvideo.controller;

import com.shortvideo.annotation.Log;
import com.shortvideo.dto.LoginRequest;
import com.shortvideo.dto.Result;
import com.shortvideo.entity.User;
import com.shortvideo.service.UserService;
import com.shortvideo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Log("用户登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Validated @RequestBody LoginRequest request) {
        User user = userService.login(request.getUsername(), request.getPassword());
        if (user != null) {
            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getIsAdmin());
            Map<String, Object> data = new HashMap<>();
            data.put("id", user.getId());
            data.put("username", user.getUsername());
            data.put("nickname", user.getNickname());
            data.put("avatar", user.getAvatar());
            data.put("isAdmin", user.getIsAdmin());
            data.put("token", token);
            return Result.success(data);
        }
        return Result.error(401, "用户名或密码错误");
    }
}
