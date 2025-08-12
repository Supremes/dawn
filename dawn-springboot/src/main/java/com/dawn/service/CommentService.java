package com.dawn.service;

import com.dawn.model.dto.CommentAdminDTO;
import com.dawn.model.dto.CommentDTO;
import com.dawn.model.dto.ReplyDTO;
import com.dawn.entity.Comment;
import com.dawn.model.vo.CommentVO;
import com.dawn.model.vo.ConditionVO;
import com.dawn.model.dto.PageResultDTO;
import com.dawn.model.vo.ReviewVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface CommentService extends IService<Comment> {

    void saveComment(CommentVO commentVO);

    PageResultDTO<CommentDTO> listComments(CommentVO commentVO);

    List<ReplyDTO> listRepliesByCommentId(Integer commentId);

    List<CommentDTO> listTopSixComments();

    PageResultDTO<CommentAdminDTO> listCommentsAdmin(ConditionVO conditionVO);

    void updateCommentsReview(ReviewVO reviewVO);

}
