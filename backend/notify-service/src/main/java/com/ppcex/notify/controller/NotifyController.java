package com.ppcex.notify.controller;

import com.ppcex.notify.dto.NotifyRequestDTO;
import com.ppcex.notify.dto.NotifyResponseDTO;
import com.ppcex.notify.service.NotifyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/notify")
@Tag(name = "通知管理", description = "通知发送和管理接口")
public class NotifyController {

    @Autowired
    private NotifyService notifyService;

    @PostMapping("/send")
    @Operation(summary = "发送通知", description = "发送单个通知")
    public ResponseEntity<NotifyResponseDTO> sendNotify(@Validated @RequestBody NotifyRequestDTO requestDTO) {
        log.info("收到发送通知请求: {}", requestDTO);

        NotifyResponseDTO response = notifyService.sendNotify(requestDTO);

        log.info("发送通知完成: {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/batch-send")
    @Operation(summary = "批量发送通知", description = "批量发送多个通知")
    public ResponseEntity<List<NotifyResponseDTO>> batchSendNotify(@Validated @RequestBody List<NotifyRequestDTO> requestDTOList) {
        log.info("收到批量发送通知请求: {}条", requestDTOList.size());

        List<NotifyResponseDTO> responseList = notifyService.batchSendNotify(requestDTOList);

        log.info("批量发送通知完成: {}条", responseList.size());
        return ResponseEntity.ok(responseList);
    }

    @PostMapping("/retry/{notifyNo}")
    @Operation(summary = "重试通知", description = "重试发送失败的通知")
    public ResponseEntity<String> retryNotify(@PathVariable String notifyNo) {
        log.info("收到重试通知请求: {}", notifyNo);

        boolean success = notifyService.retryNotify(notifyNo);

        if (success) {
            return ResponseEntity.ok("重试请求已提交");
        } else {
            return ResponseEntity.badRequest().body("重试失败");
        }
    }

    @GetMapping("/status/{notifyNo}")
    @Operation(summary = "查询通知状态", description = "查询指定通知的发送状态")
    public ResponseEntity<Integer> getNotifyStatus(@PathVariable String notifyNo) {
        log.info("收到查询通知状态请求: {}", notifyNo);

        Integer status = notifyService.getNotifyStatus(notifyNo);

        if (status != null) {
            return ResponseEntity.ok(status);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}