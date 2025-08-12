package com.dawn.service;

import com.dawn.model.dto.UniqueViewDTO;
import com.dawn.entity.UniqueView;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface UniqueViewService extends IService<UniqueView> {

    List<UniqueViewDTO> listUniqueViews();

}
