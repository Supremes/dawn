package com.dawn.consumer;

import com.alibaba.fastjson.JSON;
import com.dawn.model.dto.EmailDTO;
import com.dawn.util.EmailUtil;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import static com.dawn.constant.RabbitMQConstant.EMAIL_QUEUE;

@Component
@RabbitListener(queues = EMAIL_QUEUE)
public class CommentNoticeConsumer {

    @Autowired
    private EmailUtil emailUtil;

    @RabbitHandler
    public void process(byte[] data) {
        EmailDTO emailDTO = JSON.parseObject(new String(data), EmailDTO.class);
        emailUtil.sendHtmlMail(emailDTO);
    }

}
