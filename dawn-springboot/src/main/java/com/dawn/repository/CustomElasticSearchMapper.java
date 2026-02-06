package com.dawn.repository;

import com.dawn.model.dto.ArticleSearchDTO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;


/**
 * @author Supremes
 * elasticsearch
 */
@Repository
public interface CustomElasticSearchMapper extends ElasticsearchRepository<ArticleSearchDTO,Integer> {

}
