package com.dawn.service;


import com.dawn.model.dto.JobLogDTO;
import com.dawn.entity.JobLog;
import com.dawn.model.vo.JobLogSearchVO;
import com.dawn.model.dto.PageResultDTO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


public interface JobLogService extends IService<JobLog> {

    PageResultDTO<JobLogDTO> listJobLogs(JobLogSearchVO jobLogSearchVO);

    void deleteJobLogs(List<Integer> ids);

    void cleanJobLogs();

    List<String> listJobLogGroups();

}
