package com.dawn.model.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;


/**
 * @author Supremes
 * 文章编辑页码DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "文章")
public class ArticleAdminViewDTO {

    private Integer id;

    private String articleTitle;

    private String articleContent;

    private String articleCover;

    private String categoryName;

    private List<String> tagNames;

    private Integer isTop;

    private Integer isFeatured;

    private Integer status;

    private Integer type;

    private String originalUrl;

    private String password;

}
