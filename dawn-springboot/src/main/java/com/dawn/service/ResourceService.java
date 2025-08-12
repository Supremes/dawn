package com.dawn.service;

import com.dawn.model.dto.LabelOptionDTO;
import com.dawn.model.dto.ResourceDTO;
import com.dawn.entity.Resource;
import com.dawn.model.vo.ConditionVO;
import com.dawn.model.vo.ResourceVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ResourceService extends IService<Resource> {

    void importSwagger();

    void saveOrUpdateResource(ResourceVO resourceVO);

    void deleteResource(Integer resourceId);

    List<ResourceDTO> listResources(ConditionVO conditionVO);

    List<LabelOptionDTO> listResourceOption();

}
