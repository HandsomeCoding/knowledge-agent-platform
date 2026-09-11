package com.knowledge.knowledgeagentplatform.service.impl;

import com.knowledge.knowledgeagentplatform.common.exception.BusinessException;
import com.knowledge.knowledgeagentplatform.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

@Component
public class LocalFileStorageService implements FileStorageService {

    /**
     * 允许的后缀（小写）。新增格式往这加，别改逻辑
     */
    private static final Set<String> ALLOWED_EXT = Set.of("pdf", "doc", "docx", "txt", "md");

    private static final DateTimeFormatter MONTH_DIR = DateTimeFormatter.ofPattern("yyyyMM");

    private final Path rootDir;

    public LocalFileStorageService(@Value("${app.upload.dir:./upload}") String uploadDir) {
        this.rootDir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageService.class);

    /**
     * 保存文件返回给前端
     *
     * @param file
     * @return
     */
    @Override
    public String save(MultipartFile file) {
        String original = file.getOriginalFilename();
        String ext = StringUtils.getFilenameExtension(original);
        if (ext == null || !ALLOWED_EXT.contains(ext.toLowerCase())) {
            throw new BusinessException(400, "不支持的文件格式，仅支持: " + ALLOWED_EXT);
        }

        // {月份目录}/{uuid}.{后缀} —— 文件名与用户输入彻底无关
        String relative = LocalDate.now().format(MONTH_DIR) + "/" + UUID.randomUUID() + "." + ext.toLowerCase();

        Path target = resolve(relative);
        try {
            Files.createDirectories(target.getParent());
            // transferTo 比 getInputStream+copy 少一次内存拷贝（容器会把 tmp 文件直接移动）
            file.transferTo(target);
        } catch (IOException e) {
            throw new BusinessException(500, "文件保存失败: " + e.getMessage());
        }
        return relative;
    }

    @Override
    public Path resolve(String relativePath) {
        // normalize 后必须仍以 rootDir 开头，防止 "../" 逃逸出上传目录
        Path path = rootDir.resolve(relativePath).normalize();
        if (!path.startsWith(rootDir)) {
            throw new BusinessException(400, "非法的文件路径");
        }
        return path;
    }

    @Override
    public void delete(String relativePath) {
        try {
            Files.deleteIfExists(resolve(relativePath));
        } catch (IOException e) {
            // 删除失败不影响业务（孤儿文件可接受，手动清理），不向上抛
            log.warn("删除文件名为 ： " + relativePath + "报错 报错信息为"+ e.getMessage());
            throw new BusinessException("删除文件名为 ： " + relativePath + "报错,请核实");
        }
    }
}
