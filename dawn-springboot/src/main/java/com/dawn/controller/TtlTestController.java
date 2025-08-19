package com.dawn.controller;

import com.dawn.service.TtlMessageService;
import com.dawn.model.vo.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * TTL 和死信队列测试控制器
 */
@Api(tags = "TTL和死信队列测试")
@RestController
@RequestMapping("/ttl")
public class TtlTestController {

    @Autowired
    private TtlMessageService ttlMessageService;

    @ApiOperation("发送队列级TTL消息")
    @PostMapping("/queue")
    public ResultVO<?> sendQueueTtlMessage(@ApiParam("消息内容") @RequestParam String message) {
        ttlMessageService.sendQueueTtlMessage(message);
        return ResultVO.ok("队列级TTL消息发送成功");
    }

    @ApiOperation("发送消息级TTL消息")
    @PostMapping("/message")
    public ResultVO<?> sendMessageTtlMessage(
            @ApiParam("消息内容") @RequestParam String message,
            @ApiParam("TTL时间(毫秒)") @RequestParam long ttl) {
        ttlMessageService.sendMessageTtlMessage(message, ttl);
        return ResultVO.ok("消息级TTL消息发送成功");
    }

    @ApiOperation("发送延迟消息")
    @PostMapping("/delay")
    public ResultVO<?> sendDelayMessage(
            @ApiParam("消息内容") @RequestParam String message,
            @ApiParam("延迟时间(毫秒)") @RequestParam long delay) {
        ttlMessageService.sendDelayMessage(message, delay);
        return ResultVO.ok("延迟消息发送成功");
    }

    @ApiOperation("批量发送测试消息")
    @PostMapping("/test")
    public ResultVO<?> sendTestMessages() {
        ttlMessageService.sendTestMessages();
        return ResultVO.ok("测试消息批量发送成功，请查看控制台日志");
    }
}
