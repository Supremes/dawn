package com.dawn.strategy.impl;

import com.dawn.config.properties.MinioProperties;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.messages.Item;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service("minioUploadStrategyImpl")
public class MinioUploadStrategyImpl extends AbstractUploadStrategyImpl {

    @Autowired
    private MinioProperties minioProperties;

    @Override
    public Boolean exists(String filePath) {
        boolean exist = true;
        try {
            getMinioClient()
                    .statObject(StatObjectArgs.builder().bucket(minioProperties.getBucketName()).object(filePath).build());
        } catch (Exception e) {
            exist = false;
        }
        return exist;
    }

    @SneakyThrows
    @Override
    public void upload(String path, String fileName, InputStream inputStream) {
        getMinioClient().putObject(
                PutObjectArgs.builder().bucket(minioProperties.getBucketName()).object(path + fileName).stream(
                                inputStream, inputStream.available(), -1)
                        .build());
    }

    @Override
    public String getFileAccessUrl(String filePath) {
        return minioProperties.getUrl() + filePath;
    }

    private MinioClient getMinioClient() {
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }

    /**
     * 列出bucket中的所有objects
     * @return 所有object的名称列表
     */
    @SneakyThrows
    public List<String> listAllObjects() {
        List<String> objectNames = new ArrayList<>();
        Iterable<Result<Item>> results = getMinioClient().listObjects(
            ListObjectsArgs.builder()
                .bucket(minioProperties.getBucketName())
                .recursive(true)
                .build()
        );
        
        for (Result<Item> result : results) {
            Item item = result.get();
            objectNames.add(item.objectName());
        }
        return objectNames;
    }

    /**
     * 根据前缀列出objects
     * @param prefix 对象名前缀
     * @return 匹配前缀的object名称列表
     */
    @SneakyThrows
    public List<String> listObjectsWithPrefix(String prefix) {
        List<String> objectNames = new ArrayList<>();
        Iterable<Result<Item>> results = getMinioClient().listObjects(
            ListObjectsArgs.builder()
                .bucket(minioProperties.getBucketName())
                .prefix(prefix)
                .recursive(true)
                .build()
        );
        
        for (Result<Item> result : results) {
            Item item = result.get();
            objectNames.add(item.objectName());
        }
        return objectNames;
    }

    /**
     * 列出bucket中所有objects的详细信息
     * @return 包含详细信息的Item列表
     */
    @SneakyThrows
    public List<Item> listAllObjectsWithDetails() {
        List<Item> objects = new ArrayList<>();
        Iterable<Result<Item>> results = getMinioClient().listObjects(
            ListObjectsArgs.builder()
                .bucket(minioProperties.getBucketName())
                .recursive(true)
                .build()
        );
        
        for (Result<Item> result : results) {
            objects.add(result.get());
        }
        return objects;
    }

}
