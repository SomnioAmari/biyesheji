package com.reader.service;

import com.reader.common.BizException;
import com.reader.common.PageResult;
import com.reader.common.UserContext;
import com.reader.entity.User;
import com.reader.mapper.UserMapper;
import com.reader.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private final UserMapper userMapper;

    public PageResult<UserVO> listUsers(String keyword, int page, int size) {
        int offset = (page - 1) * size;
        List<User> users = userMapper.findPage(keyword == null ? null : keyword.trim(), offset, size);
        long total = userMapper.count(keyword == null ? null : keyword.trim());
        return PageResult.of(total, users.stream().map(this::toVO).toList());
    }

    public void updateStatus(Long targetId, int status) {
        guardSelf(targetId);
        requireUser(targetId);
        if (status != 0 && status != 1) {
            throw new BizException("状态取值非法");
        }
        userMapper.updateStatus(targetId, status);
    }

    public void updateRole(Long targetId, int role) {
        guardSelf(targetId);
        requireUser(targetId);
        if (role != 0 && role != 1) {
            throw new BizException("角色取值非法");
        }
        userMapper.updateRole(targetId, role);
    }

    public void resetPassword(Long targetId, String password) {
        requireUser(targetId);
        if (password == null || password.length() < 6 || password.length() > 50) {
            throw new BizException("密码长度需在 6~50 之间");
        }
        userMapper.updatePassword(targetId, ENCODER.encode(password));
    }

    private void guardSelf(Long targetId) {
        Long current = UserContext.require();
        if (targetId != null && targetId.equals(current)) {
            throw new BizException("不能修改自己的状态或角色");
        }
    }

    private User requireUser(Long id) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        return user;
    }

    private UserVO toVO(User u) {
        return new UserVO(u.getId(), u.getUsername(), u.getNickname(), u.getEmail(), u.getRole(), u.getStatus());
    }
}
