package com.knowledge.knowledgeagentplatform.service.impl;

import com.knowledge.knowledgeagentplatform.common.exception.BusinessException;
import com.knowledge.knowledgeagentplatform.service.FileStorageService;
import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 上传到minio
 */
@Component
public class MinIOFileStorageService implements FileStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public MinIOFileStorageService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }


    /**
     * 上传文件
     *
     * @return 返回文件的唯一标识名 objectName
     */
    @Override
    public String save(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "上传的文件不能为空");
        }
        // 2. 生成唯一的文件名（UUID + 原文件名），防止同名覆盖
        String originalFilename = file.getOriginalFilename();
        String objectName = UUID.randomUUID().toString().replace("-", "") + "_" + originalFilename;

        // 3. 执行上传
        try (InputStream inputStream = file.getInputStream()) {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception e) {
            throw new BusinessException(500, "文件上传到minio失败：" + e.getMessage());
        }
        return objectName;
    }

    /**
     * 获取文件的临时访问链接（有效期 7 天）
     * 适用于私有桶的预览、下载
     */
    @Override
    public String resolve(String relativePath) {
        String url = "";
        try {
            url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(relativePath)
                            .expiry(7, TimeUnit.DAYS) // 链接过期时间
                            .build()
            );
        } catch (Exception e) {
            throw new BusinessException(500, "获取minio预览或下载地址失败:" + e.getMessage());
        }

        return url;
    }

    @Override
    public void delete(String relativePath) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(relativePath)
                            .build()
            );
        } catch (Exception e) {
            throw new BusinessException(500, "删除minio文件报错，报错信息为" + e.getMessage());
        }
    }


    public InputStream getInputStream(String objectName) {
        try {

            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new BusinessException(500, "oss文件下载报错：" + e.getMessage());
        }
    }
}
