package com.dawn.service;

import com.dawn.model.dto.OperationLogDTO;
import com.dawn.entity.OperationLog;
import com.dawn.model.vo.ConditionVO;
import com.dawn.model.dto.PageResultDTO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface OperationLogService extends IService<OperationLog> {

    PageResultDTO<OperationLogDTO> listOperationLogs(ConditionVO conditionVO);

}
