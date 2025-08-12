package com.dawn.mapper;

import com.dawn.model.dto.TagAdminDTO;
import com.dawn.model.dto.TagDTO;
import com.dawn.entity.Tag;
import com.dawn.model.vo.ConditionVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagMapper extends BaseMapper<Tag> {

    List<TagDTO> listTags();

    List<TagDTO> listTopTenTags();

    List<String> listTagNamesByArticleId(Integer articleId);

    List<TagAdminDTO> listTagsAdmin(@Param("current") Long current, @Param("size") Long size, @Param("conditionVO") ConditionVO conditionVO);

}
