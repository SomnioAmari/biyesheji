package com.reader.service;

import com.reader.common.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> IMAGE_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp");

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    /**
     * 保存封面图片到本地目录，返回可访问的相对 URL（/uploads/covers/xxx.ext）。
     */
    public String saveCover(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String original = file.getOriginalFilename() == null ? "cover.jpg" : file.getOriginalFilename();
        String ext = original.contains(".")
                ? original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT)
                : "jpg";
        if (!IMAGE_EXT.contains(ext)) {
            throw new BizException("仅支持图片格式：" + String.join("/", IMAGE_EXT));
        }

        try {
            Path dir = Paths.get(uploadDir, "covers").toAbsolutePath().normalize();
            Files.createDirectories(dir);
            String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            Path target = dir.resolve(filename);
            file.transferTo(target.toFile());
            return "/uploads/covers/" + filename;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("封面保存失败：" + e.getMessage());
        }
    }
}
