package com.reader.service;

import com.reader.common.BizException;
import com.reader.common.JwtUtil;
import com.reader.dto.LoginRequest;
import com.reader.dto.RegisterRequest;
import com.reader.entity.User;
import com.reader.mapper.UserMapper;
import com.reader.vo.LoginResponse;
import com.reader.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public UserVO register(RegisterRequest req) {
        if (userMapper.findByUsername(req.getUsername()) != null) {
            throw new BizException("用户名已存在");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(ENCODER.encode(req.getPassword()));
        user.setNickname(req.getNickname() == null || req.getNickname().isBlank()
                ? req.getUsername() : req.getNickname());
        user.setEmail(req.getEmail());
        userMapper.insert(user);
        return toVO(user);
    }

    public LoginResponse login(LoginRequest req) {
        User user = userMapper.findByUsername(req.getUsername());
        if (user == null || !ENCODER.matches(req.getPassword(), user.getPassword())) {
            throw new BizException(401, "用户名或密码错误");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new LoginResponse(token, toVO(user));
    }

    private UserVO toVO(User u) {
        return new UserVO(u.getId(), u.getUsername(), u.getNickname(), u.getEmail());
    }
}
