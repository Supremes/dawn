package com.dawn.mapper;

import com.dawn.model.dto.UniqueViewDTO;
import com.dawn.entity.UniqueView;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface UniqueViewMapper extends BaseMapper<UniqueView> {

    List<UniqueViewDTO> listUniqueViews(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

}
