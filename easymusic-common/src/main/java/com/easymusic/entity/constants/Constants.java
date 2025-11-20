package com.easymusic.entity.constants;

/**
 * @author cyt
 * * @date 2025/11/19 14:08:36
 */
public class Constants {

    public static final String REDIS_KEY_PREFIX = "easymusic:";

    public static final Integer LENGTH_12 = 12;

    public static final Integer LENGTH_20 = 20;

    /**
     * file
     */
    public static final String FILE_FOLDER_FILE = "file/";

    public static final String FILE_FOLDER_AVATAR_NAME = "avatar/";

    public static final String AVATAR_SUFFIX = ".jpg";

    public static final Integer AVATAR_LEN = 9;

    public static final String DEFAULT_AVATAR_PATH = "/avatar/%d.jpg";

    public static final String[] IMAGES_SUFFIX = {".jpg", ".png", ".jpeg", ".gif", ".bmp", ".svg", ".webp", ".ico"};


    /**
     * redis key *********************
     */
    // 过期时间
    public static final Long REDIS_KEY_EXPIRES_ONE_MIN = 60L;

    // 验证码
    public static final String REDIS_KEY_CHECK_CODE = REDIS_KEY_PREFIX + "checkCode:";

    // token
    public static final String REDIS_KEY_TOKEN_WEB_USER = REDIS_KEY_PREFIX + "token:";

    public static final Long REDIS_KEY_EXPIRES_ONE_DAY = REDIS_KEY_EXPIRES_ONE_MIN * 60 * 24;

}
