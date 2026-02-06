package com.graduate.research.common;

public class Constants {
    
    /** 用户角色 */
    public static class Role {
        public static final int ADMIN = 1;
        public static final int TEACHER = 2;
        public static final int STUDENT = 3;
    }
    
    /** 用户状态 */
    public static class UserStatus {
        public static final int DISABLED = 0;
        public static final int ENABLED = 1;
    }
    
    /** 成果类型 */
    public static class AchievementType {
        public static final int PAPER = 1;
        public static final int PATENT = 2;
        public static final int SOFTWARE = 3;
    }
    
    /** 成果状态 */
    public static class AchievementStatus {
        public static final int PUBLISHED = 1;
        public static final int APPLYING = 2;
    }
    
    /** 周报状态 */
    public static class ReportStatus {
        public static final int DRAFT = 1;
        public static final int SUBMITTED = 2;
        public static final int REVIEWED = 3;
    }
}
