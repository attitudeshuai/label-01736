package com.graduate.research.controller;

import com.graduate.research.common.Result;
import com.graduate.research.exception.BusinessException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/files")
public class FileController {

    @Value("${file.upload-path}")
    private String uploadPath;

    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("请选择要上传的文件");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = FilenameUtils.getExtension(originalFilename);
            String newFilename = UUID.randomUUID().toString() + "." + extension;

            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Path filePath = uploadDir.resolve(newFilename);
            file.transferTo(filePath.toFile());

            Map<String, String> result = new HashMap<>();
            result.put("filename", newFilename);
            result.put("originalFilename", originalFilename);
            result.put("url", "/api/files/" + newFilename);

            log.info("文件上传成功: {} -> {}", originalFilename, newFilename);
            return Result.success(result);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败");
        }
    }

    @GetMapping("/{filename}")
    public void download(@PathVariable String filename, HttpServletResponse response) {
        try {
            Path filePath = Paths.get(uploadPath, filename);
            File file = filePath.toFile();

            if (!file.exists()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "attachment; filename=" + 
                    URLEncoder.encode(filename, StandardCharsets.UTF_8));
            response.setContentLengthLong(file.length());

            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
        } catch (Exception e) {
            log.error("文件下载失败: {}", filename, e);
        }
    }
}
