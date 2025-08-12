package com.dawn.service;

import com.dawn.model.dto.RoleDTO;
import com.dawn.model.dto.UserRoleDTO;
import com.dawn.entity.Role;
import com.dawn.model.vo.ConditionVO;
import com.dawn.model.dto.PageResultDTO;
import com.dawn.model.vo.RoleVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface RoleService extends IService<Role> {

    List<UserRoleDTO> listUserRoles();

    PageResultDTO<RoleDTO> listRoles(ConditionVO conditionVO);

    void saveOrUpdateRole(RoleVO roleVO);

    void deleteRoles(List<Integer> ids);

}
