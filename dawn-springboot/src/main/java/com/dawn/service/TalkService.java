package com.dawn.service;

import com.dawn.model.dto.TalkAdminDTO;
import com.dawn.model.dto.TalkDTO;
import com.dawn.entity.Talk;
import com.dawn.model.vo.ConditionVO;
import com.dawn.model.dto.PageResultDTO;
import com.dawn.model.vo.TalkVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


public interface TalkService extends IService<Talk> {

    PageResultDTO<TalkDTO> listTalks();

    TalkDTO getTalkById(Integer talkId);

    void saveOrUpdateTalk(TalkVO talkVO);

    void deleteTalks(List<Integer> talkIdList);

    PageResultDTO<TalkAdminDTO> listBackTalks(ConditionVO conditionVO);

    TalkAdminDTO getBackTalkById(Integer talkId);

}
