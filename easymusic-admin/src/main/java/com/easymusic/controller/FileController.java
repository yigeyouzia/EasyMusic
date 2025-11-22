package com.easymusic.controller;

import com.easymusic.entity.config.AppConfig;
import com.easymusic.entity.constants.Constants;
import com.easymusic.utils.StringTools;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Date;

/**
 * @author cyt
 * * @date 2025/11/20 16:52:55
 */

@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private AppConfig appConfig;

    @RequestMapping("/getResource")
    public void getResource(HttpServletResponse response, @RequestHeader(required = false, name = "range") String range, @NotEmpty String filePath) {
        log.info("拖动进度条：{}", range);
        // 验证文件路径合法性
        if (!StringTools.pathIsOk(filePath)) {
            return;
        }

        filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + filePath;
        String suffix = StringTools.getFileSuffix(filePath);
        if (!StringTools.isEmpty(suffix) && ArrayUtils.contains(Constants.IMAGES_SUFFIX, suffix.toLowerCase())) {
            response.setHeader("Cache-Control", "max-age=30 * 24 * 60 * 60");
            response.setContentType("image/jpg");
        }
//        readFileAll(response, filePath);
        readFile(response, range, filePath);
    }


    /**
     * 读取文件 从Range开始
     *
     * @param response
     * @param rangeHeader
     * @param filePath
     */
    protected void readFile(HttpServletResponse response, String rangeHeader, String filePath) {
        File file = new File(filePath);

        // 验证文件存在性和可读性
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try (RandomAccessFile randomFile = new RandomAccessFile(file, "r"); ServletOutputStream out = response.getOutputStream()) {
            long fileSize = randomFile.length();
            long start = 0;
            long end = fileSize - 1; // 默认到文件末尾
            // 解析范围请求头
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                try {
                    String rangeValue = rangeHeader.substring(6); // bytes= 后面的值
                    String[] ranges = rangeValue.split("-");

                    start = Long.parseLong(ranges[0]);
                    if (ranges.length > 1 && !ranges[1].isEmpty()) {
                        end = Long.parseLong(ranges[1]);
                    }

                    // 验证范围有效性
                    if (start < 0 || end >= fileSize || start > end) {
                        response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                        response.setHeader("Content-Range", "bytes */" + fileSize);
                        return;
                    }
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
            }
            long contentLength = end - start + 1;
            // 设置响应头
            response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("Last-Modified", new Date(file.lastModified()).toString());
            // 处理范围请求
            if (rangeHeader != null) {
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileSize);
            }
            response.setHeader("Content-Length", String.valueOf(contentLength));
            byte[] buffer = new byte[1024 * 1024];
            randomFile.seek(start);

            long remaining = contentLength;
            while (remaining > 0) {
                int read = randomFile.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                if (read == -1) {
                    break; // 文件结束
                }

                out.write(buffer, 0, read);
                remaining -= read;

                // 刷新输出流，确保数据及时发送
                if (remaining <= 0 || read < buffer.length) {
                    out.flush();
                }
            }

        } catch (AsyncRequestNotUsableException e) {
        } catch (Exception e) {
            // 记录错误日志
            log.error("文件读取失败: " + filePath, e);
            // 如果响应还未提交，设置错误状态
            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }


//    /**
//     * 读取所有文件
//     *
//     * @param response
//     * @param filePath
//     */
//    protected void readFileAll(HttpServletResponse response, String filePath) {
//        File file = new File(filePath);
//
//        try (OutputStream out = new response.getOutputStream(); InputStream in = new FileInputStream(file)) {
//            int len = 0;
//            byte[] buffer = new byte[1024];
//            while ((len = in.read(buffer)) != -1) {
//                out.write(buffer, 0, len);
//            }
//            out.flush();
//        } catch (Exception e) {
//            log.error("读取文件失败: ", e);
//        }
//    }
}
