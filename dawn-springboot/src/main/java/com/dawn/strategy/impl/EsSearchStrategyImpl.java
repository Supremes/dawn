package com.dawn.strategy.impl;

import com.dawn.model.dto.ArticleSearchDTO;
import com.dawn.strategy.SearchStrategy;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightParameters;
import org.springframework.stereotype.Service;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.dawn.constant.CommonConstant.*;
import static com.dawn.enums.ArticleStatusEnum.PUBLIC;

@Log4j2
@Service("esSearchStrategyImpl")
public class EsSearchStrategyImpl implements SearchStrategy {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public List<ArticleSearchDTO> searchArticle(String keywords) {
        if (StringUtils.isBlank(keywords)) {
            return new ArrayList<>();
        }
        return search(buildQuery(keywords));
    }

    private NativeQuery buildQuery(String keywords) {
        // 构建布尔查询
        Query matchTitle = Query.of(q -> q.match(m -> m.field("articleTitle").query(keywords)));
        Query matchContent = Query.of(q -> q.match(m -> m.field("articleContent").query(keywords)));
        Query isDeleteQuery = Query.of(q -> q.term(t -> t.field("isDelete").value(FALSE)));
        Query statusQuery = Query.of(q -> q.term(t -> t.field("status").value(PUBLIC.getStatus())));

        BoolQuery boolQuery = BoolQuery.of(b -> b
                .must(Query.of(q -> q.bool(bb -> bb.should(matchTitle).should(matchContent))))
                .must(isDeleteQuery)
                .must(statusQuery));

        // 构建高亮
        List<HighlightField> highlightFields = List.of(
                new HighlightField("articleTitle"),
                new HighlightField("articleContent")
        );
        HighlightParameters highlightParameters = HighlightParameters.builder()
                .withPreTags(PRE_TAG)
                .withPostTags(POST_TAG)
                .build();
        Highlight highlight = new Highlight(highlightParameters, highlightFields);

        return NativeQuery.builder()
                .withQuery(Query.of(q -> q.bool(boolQuery)))
                .withHighlightQuery(new HighlightQuery(highlight, ArticleSearchDTO.class))
                .build();
    }

    private List<ArticleSearchDTO> search(NativeQuery nativeQuery) {
        try {
            SearchHits<ArticleSearchDTO> search = elasticsearchTemplate.search(nativeQuery, ArticleSearchDTO.class);
            return search.getSearchHits().stream().map(hit -> {
                ArticleSearchDTO article = hit.getContent();
                List<String> titleHighLightList = hit.getHighlightFields().get("articleTitle");
                if (CollectionUtils.isNotEmpty(titleHighLightList)) {
                    article.setArticleTitle(titleHighLightList.get(0));
                }
                List<String> contentHighLightList = hit.getHighlightFields().get("articleContent");
                if (CollectionUtils.isNotEmpty(contentHighLightList)) {
                    article.setArticleContent(contentHighLightList.get(contentHighLightList.size() - 1));
                }
                return article;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return new ArrayList<>();
    }

}

