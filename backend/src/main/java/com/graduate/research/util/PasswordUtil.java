package com.graduate.research.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 密码工具类
 * 前端传输 MD5 加密后的密码，后端直接存储和比对 MD5 值
 */
public class PasswordUtil {

    /**
     * 对原始密码进行 MD5 加密
     * 用于后端创建用户时对明文密码加密
     */
    public static String encode(String rawPassword) {
        return md5(rawPassword);
    }

    /**
     * 验证密码是否匹配
     * @param inputPassword 前端传来的 MD5 加密后的密码
     * @param storedPassword 数据库中存储的 MD5 密码
     */
    public static boolean matches(String inputPassword, String storedPassword) {
        if (inputPassword == null || storedPassword == null) {
            return false;
        }
        // 前端已经 MD5 加密，直接比对
        return inputPassword.equalsIgnoreCase(storedPassword);
    }

    /**
     * MD5 加密
     */
    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    /**
     * 生成初始密码的 MD5 值
     */
    public static void main(String[] args) {
        System.out.println("admin123 MD5: " + md5("admin123"));
    }
}
