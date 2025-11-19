package com.easymusic.utils;

import com.easymusic.entity.config.AppConfig;
import com.easymusic.entity.constants.Constants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author cyt
 * * @date 2025/11/19 15:27:15
 */

@Component("fileUtils")
@Slf4j
public class FileUtils {

    @Resource
    private AppConfig appConfig;

    public String copyAvatar(String userId) {

        try {
            int random = (int) (Math.random() * Constants.AVATAR_LEN) + 1;
            String avatarFolderPath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_AVATAR_NAME;
            File folder = new File(avatarFolderPath);
            if (!folder.exists()) {
                folder.mkdir();
            }

            // avatar/userid.png
            String avatarPath = Constants.FILE_FOLDER_AVATAR_NAME + userId + Constants.AVATAR_SUFFIX;
            // easymusic/file/ + avatar/userid.png
            File avatarFile = new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + avatarPath);
            ClassPathResource classPathResource = new ClassPathResource(String.format(Constants.DEFAULT_AVATAR_PATH, random));
            org.apache.commons.io.FileUtils.copyToFile(classPathResource.getInputStream(), avatarFile);
            return avatarPath;
        } catch (Exception e) {
            log.error("拷贝头像失败：{}", e);
        }
        return null;
    }

}
