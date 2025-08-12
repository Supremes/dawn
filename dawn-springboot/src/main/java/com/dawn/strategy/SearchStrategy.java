package com.dawn.strategy;

import com.dawn.model.dto.ArticleSearchDTO;

import java.util.List;

public interface SearchStrategy {

    List<ArticleSearchDTO> searchArticle(String keywords);

}
