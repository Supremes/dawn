package com.dawn.controller;

import com.dawn.strategy.impl.MinioUploadStrategyImpl;
import io.minio.messages.Item;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MinIO 对象存储管理控制器
 * @author dawn
 */
@Tag(name = "MinIO对象存储管理")
@RestController
@RequestMapping("/admin/minio")
public class MinioController {

    @Autowired
    private MinioUploadStrategyImpl minioUploadStrategy;

    @Operation(summary = "列出所有对象名称")
    @GetMapping("/objects")
    public List<String> listAllObjects() {
        return minioUploadStrategy.listAllObjects();
    }

    @Operation(summary = "根据前缀列出对象")
    @GetMapping("/objects/prefix/{prefix}")
    public List<String> listObjectsWithPrefix(
            @Parameter(description = "对象名前缀") @PathVariable String prefix) {
        return minioUploadStrategy.listObjectsWithPrefix(prefix);
    }

    @Operation(summary = "列出所有对象的详细信息")
    @GetMapping("/objects/details")
    public List<Map<String, Object>> listAllObjectsWithDetails() {
        List<Item> items = minioUploadStrategy.listAllObjectsWithDetails();
        
        return items.stream().map(item -> {
            Map<String, Object> objectInfo = new HashMap<>();
            objectInfo.put("objectName", item.objectName());
            objectInfo.put("size", item.size());
            objectInfo.put("etag", item.etag());
            objectInfo.put("isDir", item.isDir());
            if (item.lastModified() != null) {
                objectInfo.put("lastModified",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(item.lastModified()));
            }
            objectInfo.put("storageClass", item.storageClass());
            return objectInfo;
        }).collect(Collectors.toList());
    }

    @Operation(summary = "统计对象信息")
    @GetMapping("/objects/statistics")
    public Map<String, Object> getObjectStatistics() {
        List<Item> items = minioUploadStrategy.listAllObjectsWithDetails();
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalObjects", items.size());
        statistics.put("totalSize", items.stream().mapToLong(Item::size).sum());
        
        // 按文件类型分组统计
        Map<String, Long> typeCount = items.stream()
            .filter(item -> !item.isDir())
            .collect(Collectors.groupingBy(
                item -> getFileExtension(item.objectName()),
                Collectors.counting()
            ));
        statistics.put("fileTypeStatistics", typeCount);
        
        return statistics;
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot + 1).toLowerCase();
        }
        return "unknown";
    }

    @Operation(summary = "测试SpringBoot序列化行为")
    @GetMapping("/test/serialize")
    public TestDTO testSerializeDTO() {
        return TestDTO.builder()
                .dateTime(LocalDateTime.now())
                .name("name")
                .build();
    }

    @Builder
    @AllArgsConstructor
    @Data
    public static class TestDTO {
        LocalDateTime dateTime;
        String name;
    }
}
