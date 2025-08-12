package com.dawn.service.impl;

import com.dawn.entity.UserRole;
import com.dawn.mapper.UserRoleMapper;
import com.dawn.service.UserRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {

}
