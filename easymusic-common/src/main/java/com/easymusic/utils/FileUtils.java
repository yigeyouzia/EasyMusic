package com.easymusic.utils;

import com.easymusic.entity.config.AppConfig;
import com.easymusic.entity.constants.Constants;
import com.easymusic.entity.enums.DateTimePatternEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Date;

/**
 * @author cyt
 * * @date 2025/11/19 15:27:15
 */

@Component("fileUtils")
@Slf4j
public class FileUtils {

    @Resource
    private AppConfig appConfig;

    public String uploadFile(MultipartFile file, String folderName, String fileName) {
        if (StringTools.isEmpty(folderName)) {
            folderName = DateUtil.format(new Date(), DateTimePatternEnum.YYYYMM.getPattern()) + "/";
        }
        String folderPath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + folderName;
        // 如果文件夹不存在，则创建
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdir();
        }

        // 文件路径
        if (StringTools.isEmpty(fileName)) {
            fileName = System.currentTimeMillis() + StringTools.getFileSuffix(file.getOriginalFilename());
        }

        try {
            // 保存文件
            file.transferTo(new File(folderPath, fileName));
            log.info("上传文件成功! 文件路径：{}， => 文件名：{}", folderPath, fileName);

        } catch (Exception e) {
            log.error("上传文件失败：{}", e);
        }

        return folderName + fileName;
    }

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

    public String downloadFile(String url, String suffix) {
        String folderName = DateUtil.format(new Date(), DateTimePatternEnum.YYYYMM.getPattern());
        String folderPath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + folderName + "/";

        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdir();
        }
        String fileName = StringTools.getRandomString(Constants.LENGTH_30) + suffix;
        String filePath = folderPath + fileName;
        OKHttpUtils.download(url, filePath);
        return folderName + "/" + fileName;
    }

}
