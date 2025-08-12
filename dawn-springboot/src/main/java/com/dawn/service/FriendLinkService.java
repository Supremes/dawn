package com.dawn.service;

import com.dawn.model.dto.FriendLinkAdminDTO;
import com.dawn.model.dto.FriendLinkDTO;
import com.dawn.entity.FriendLink;
import com.dawn.model.vo.ConditionVO;
import com.dawn.model.vo.FriendLinkVO;
import com.dawn.model.dto.PageResultDTO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface FriendLinkService extends IService<FriendLink> {

    List<FriendLinkDTO> listFriendLinks();

    PageResultDTO<FriendLinkAdminDTO> listFriendLinksAdmin(ConditionVO conditionVO);

    void saveOrUpdateFriendLink(FriendLinkVO friendLinkVO);

}
