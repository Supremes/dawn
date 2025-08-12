package com.dawn.service;

import com.dawn.model.dto.CategoryAdminDTO;
import com.dawn.model.dto.CategoryDTO;
import com.dawn.model.dto.CategoryOptionDTO;
import com.dawn.entity.Category;
import com.dawn.model.vo.CategoryVO;
import com.dawn.model.vo.ConditionVO;
import com.dawn.model.dto.PageResultDTO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface CategoryService extends IService<Category> {

    List<CategoryDTO> listCategories();

    PageResultDTO<CategoryAdminDTO> listCategoriesAdmin(ConditionVO conditionVO);

    List<CategoryOptionDTO> listCategoriesBySearch(ConditionVO conditionVO);

    void deleteCategories(List<Integer> categoryIds);

    void saveOrUpdateCategory(CategoryVO categoryVO);

}
