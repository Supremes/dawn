package com.dawn.util;

import com.dawn.model.dto.EmailDTO;
import com.dawn.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import static com.dawn.constant.RedisConstant.CODE_EXPIRE_TIME;
import static com.dawn.constant.RedisConstant.USER_CODE_KEY;

@Slf4j
@Component
public class EmailUtil {

    @Value("${spring.mail.username}")
    private String email;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private RedisService redisService;

    public void sendHtmlMail(EmailDTO emailDTO) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
            Context context = new Context();
            context.setVariables(emailDTO.getCommentMap());
            String process = templateEngine.process(emailDTO.getTemplate(), context);
            mimeMessageHelper.setFrom(email);
            mimeMessageHelper.setTo(emailDTO.getEmail());
            mimeMessageHelper.setSubject(emailDTO.getSubject());
            mimeMessageHelper.setText(process, true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException | MailException e) {
            e.printStackTrace();
            log.error("验证码邮件发送失败: {}", e.getLocalizedMessage());
            return;
        }
        // 发送成功后，redis缓存验证码信息
        redisService.set(USER_CODE_KEY + emailDTO.getEmail(), emailDTO.getVerificationCode(), CODE_EXPIRE_TIME);
        log.debug("已发送邮件给注册邮箱:{}, 验证码为:{}", emailDTO.getEmail(),emailDTO.getCommentMap());
    }

}
