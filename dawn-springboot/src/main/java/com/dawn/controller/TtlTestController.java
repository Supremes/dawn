package com.dawn.controller;

import com.dawn.service.TtlMessageService;
import com.dawn.model.vo.ResultVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * TTL 和死信队列测试控制器
 */
@Tag(name = "TTL和死信队列测试")
@RestController
@RequestMapping("/ttl")
public class TtlTestController {

    @Autowired
    private TtlMessageService ttlMessageService;

    @Operation(summary = "发送队列级TTL消息")
    @PostMapping("/queue")
    public ResultVO<?> sendQueueTtlMessage(@Parameter(description = "消息内容") @RequestParam String message) {
        ttlMessageService.sendQueueTtlMessage(message);
        return ResultVO.ok("队列级TTL消息发送成功");
    }

    @Operation(summary = "发送消息级TTL消息")
    @PostMapping("/message")
    public ResultVO<?> sendMessageTtlMessage(
            @Parameter(description = "消息内容") @RequestParam String message,
            @Parameter(description = "TTL时间(毫秒)") @RequestParam long ttl) {
        ttlMessageService.sendMessageTtlMessage(message, ttl);
        return ResultVO.ok("消息级TTL消息发送成功");
    }

    @Operation(summary = "发送延迟消息")
    @PostMapping("/delay")
    public ResultVO<?> sendDelayMessage(
            @Parameter(description = "消息内容") @RequestParam String message,
            @Parameter(description = "延迟时间(毫秒)") @RequestParam long delay) {
        ttlMessageService.sendDelayMessage(message, delay);
        return ResultVO.ok("延迟消息发送成功");
    }

    @Operation(summary = "批量发送测试消息")
    @PostMapping("/test")
    public ResultVO<?> sendTestMessages() {
        ttlMessageService.sendTestMessages();
        return ResultVO.ok("测试消息批量发送成功，请查看控制台日志");
    }
}
