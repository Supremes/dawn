package com.dawn.service;

import com.dawn.model.dto.LabelOptionDTO;
import com.dawn.model.dto.MenuDTO;
import com.dawn.model.dto.UserMenuDTO;
import com.dawn.entity.Menu;
import com.dawn.model.vo.ConditionVO;
import com.dawn.model.vo.IsHiddenVO;
import com.dawn.model.vo.MenuVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface MenuService extends IService<Menu> {

    List<MenuDTO> listMenus(ConditionVO conditionVO);

    void saveOrUpdateMenu(MenuVO menuVO);

    void updateMenuIsHidden(IsHiddenVO isHiddenVO);

    void deleteMenu(Integer menuId);

    List<LabelOptionDTO> listMenuOptions();

    List<UserMenuDTO> listUserMenus();

}
