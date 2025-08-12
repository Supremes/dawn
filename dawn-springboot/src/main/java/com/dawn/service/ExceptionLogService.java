package com.dawn.service;

import com.dawn.model.dto.ExceptionLogDTO;
import com.dawn.entity.ExceptionLog;
import com.dawn.model.vo.ConditionVO;
import com.dawn.model.dto.PageResultDTO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ExceptionLogService extends IService<ExceptionLog> {

    PageResultDTO<ExceptionLogDTO> listExceptionLogs(ConditionVO conditionVO);

}
