package com.reader.common;

public class UserContext {

    public record CurrentUser(Long id, Integer role, String username) {
        public boolean isAdmin() {
            return role != null && role == 1;
        }
    }

    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    public static void set(Long userId, Integer role, String username) {
        HOLDER.set(new CurrentUser(userId, role, username));
    }

    public static CurrentUser current() {
        return HOLDER.get();
    }

    public static Long get() {
        CurrentUser u = HOLDER.get();
        return u == null ? null : u.id();
    }

    public static Long require() {
        CurrentUser u = HOLDER.get();
        if (u == null) {
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "未登录");
        }
        return u.id();
    }

    public static Integer role() {
        CurrentUser u = HOLDER.get();
        return u == null ? null : u.role();
    }

    public static Long requireAdmin() {
        CurrentUser u = HOLDER.get();
        if (u == null) {
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "未登录");
        }
        if (!u.isAdmin()) {
            throw new BizException(ResultCode.FORBIDDEN.getCode(), "无管理员权限");
        }
        return u.id();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
