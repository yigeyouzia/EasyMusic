package com.easymusic.entity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author cyt
 * * @date 2025/11/19 15:30:14
 */

@Configuration
public class AppConfig {

    @Value("${project.folder:}")
    private String projectFolder;

    public String getProjectFolder() {
        return projectFolder;
    }
}
