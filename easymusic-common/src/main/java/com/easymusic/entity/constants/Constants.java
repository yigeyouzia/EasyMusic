package com.easymusic.entity.constants;

/**
 * @author cyt
 * * @date 2025/11/19 14:08:36
 */
public class Constants {

    public static final String REDIS_KEY_PREFIX = "easymusic:";

    public static final String ZERO_STR = "0";

    public static final Integer LENGTH_5 = 5;

    public static final Integer LENGTH_8 = 8;

    public static final Integer LENGTH_12 = 12;

    // 微信订单order_id长度 后面的随机数
    public static final Integer LENGTH_14 = 14;

    public static final Integer LENGTH_15 = 15;

    public static final Integer LENGTH_20 = 20;

    public static final Integer LENGTH_30 = 30;

    /**
     * file
     */
    public static final String FILE_FOLDER_FILE = "file/";

    public static final String FILE_FOLDER_AVATAR_NAME = "avatar/";

    public static final String AVATAR_SUFFIX = ".jpg";

    public static final Integer AVATAR_LEN = 9;

    public static final String DEFAULT_AVATAR_PATH = "/avatar/%d.jpg";

    public static final String[] IMAGES_SUFFIX = {".jpg", ".png", ".jpeg", ".gif", ".bmp", ".svg", ".webp", ".ico"};

    public static final String AUDIO_SUFFIX = ".mp3";


    /**
     * redis key *********************
     */
    // 过期时间
    public static final Long REDIS_KEY_EXPIRES_ONE_MIN = 60L;

    // 验证码
    public static final String REDIS_KEY_CHECK_CODE = REDIS_KEY_PREFIX + "checkCode:";

    // token
    public static final String REDIS_KEY_TOKEN_WEB_USER = REDIS_KEY_PREFIX + "token:";

    public static final String REDIS_KEY_TOKEN_ADMIN_USER = REDIS_KEY_PREFIX + "token:admin:";

    public static final Long REDIS_KEY_EXPIRES_ONE_DAY = REDIS_KEY_EXPIRES_ONE_MIN * 60 * 24;

    /**
     * pay 支付相关
     */
    // 微信支付二维码请求失效时间
    public static final Integer ORDER_TIMEOUT_MIN = 10;


    /**
     * 订单 积分相关
     */

    // 已经支付的订单 轮询得到的，需要后续处理
    public static final String REDIS_KEY_ORDER_HAVE_PAY = REDIS_KEY_PREFIX + "order:havepay:";

    // 超时订单队列
    public static final String REDIS_KEY_ORDER_DELAY_QUEUE = REDIS_KEY_PREFIX + "order:delay:queue:";

    // 字典 系统配置
    public static final String REDIS_KEY_SYS_DICT = REDIS_KEY_PREFIX + "sysdict:";

    // 歌曲创建队列
    public static final String REDIS_KEY_MUSIC_CREATE_QUEUE = REDIS_KEY_PREFIX + "create:queue:";

}
