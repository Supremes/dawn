package com.dawn.service;

import com.dawn.model.dto.*;
import com.dawn.entity.Article;
import com.dawn.model.vo.*;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


public interface ArticleService extends IService<Article> {

    TopAndFeaturedArticlesDTO listTopAndFeaturedArticles();

    PageResultDTO<ArticleCardDTO> listArticles();

    PageResultDTO<ArticleCardDTO> listArticlesByCategoryId(Integer categoryId);

    ArticleDTO getArticleById(Integer articleId);

    void accessArticle(ArticlePasswordVO articlePasswordVO);

    PageResultDTO<ArticleCardDTO> listArticlesByTagId(Integer tagId);

    PageResultDTO<ArchiveDTO> listArchives();

    PageResultDTO<ArticleAdminDTO> listArticlesAdmin(ConditionVO conditionVO);

    void saveOrUpdateArticle(ArticleVO articleVO);

    void updateArticleTopAndFeatured(ArticleTopFeaturedVO articleTopFeaturedVO);

    void updateArticleDelete(DeleteVO deleteVO);

    void deleteArticles(List<Integer> articleIds);

    ArticleAdminViewDTO getArticleByIdAdmin(Integer articleId);

    List<String> exportArticles(List<Integer> articleIdList);

    List<ArticleSearchDTO> listArticlesBySearch(ConditionVO condition);

}
