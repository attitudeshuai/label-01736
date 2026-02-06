package com.graduate.research.util;

import com.graduate.research.entity.User;

public class UserContext {
    
    private static final ThreadLocal<User> currentUser = new ThreadLocal<>();
    
    public static void setCurrentUser(User user) {
        currentUser.set(user);
    }
    
    public static User getCurrentUser() {
        return currentUser.get();
    }
    
    public static Long getCurrentUserId() {
        User user = currentUser.get();
        return user != null ? user.getId() : null;
    }
    
    public static String getCurrentUsername() {
        User user = currentUser.get();
        return user != null ? user.getUsername() : null;
    }
    
    public static Integer getCurrentRole() {
        User user = currentUser.get();
        return user != null ? user.getRole() : null;
    }
    
    public static void clear() {
        currentUser.remove();
    }
}
