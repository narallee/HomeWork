package com.collab.controller;

import com.collab.common.Result;
import com.collab.model.dto.CreateLedgerRequest;
import com.collab.model.dto.InviteMemberRequest;
import com.collab.model.dto.JoinLedgerRequest;
import com.collab.model.entity.Ledger;
import com.collab.service.LedgerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ledgers")
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    /**
     * 创建账本
     */
    @PostMapping
    public Result<Map<String, String>> createLedger(HttpServletRequest request,
                                                     @Valid @RequestBody CreateLedgerRequest body) {
        String userId = (String) request.getAttribute("userId");
        Map<String, String> data = ledgerService.createLedger(userId, body.getName(), body.getDescription());
        return Result.success(data, "创建成功");
    }

    /**
     * 获取账本列表
     */
    @GetMapping
    public Result<List<Ledger>> getLedgers(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        List<Ledger> ledgers = ledgerService.getLedgers(userId);
        return Result.success(ledgers);
    }

    /**
     * 获取账本详情
     */
    @GetMapping("/{ledgerId}")
    public Result<Map<String, Object>> getLedgerDetail(HttpServletRequest request,
                                                       @PathVariable String ledgerId) {
        String userId = (String) request.getAttribute("userId");
        Map<String, Object> data = ledgerService.getLedgerDetail(ledgerId, userId);
        return Result.success(data);
    }

    /**
     * 邀请成员
     */
    @PostMapping("/{ledgerId}/invite")
    public Result<Void> inviteMember(HttpServletRequest request,
                                      @PathVariable String ledgerId,
                                      @Valid @RequestBody InviteMemberRequest body) {
        String userId = (String) request.getAttribute("userId");
        ledgerService.inviteMember(ledgerId, userId, body.getTargetUserId());
        return Result.success("邀请成功");
    }

    /**
     * 生成邀请码
     */
    @PostMapping("/{ledgerId}/invite-code")
    public Result<Map<String, String>> generateInviteCode(HttpServletRequest request,
                                                           @PathVariable String ledgerId) {
        String userId = (String) request.getAttribute("userId");
        Map<String, String> data = ledgerService.generateInviteCode(ledgerId, userId);
        return Result.success(data, "生成成功");
    }

    /**
     * 通过邀请码加入账本
     */
    @PostMapping("/join")
    public Result<Map<String, String>> joinByCode(HttpServletRequest request,
                                                   @Valid @RequestBody JoinLedgerRequest body) {
        String userId = (String) request.getAttribute("userId");
        Map<String, String> data = ledgerService.joinByCode(userId, body.getInviteCode());
        return Result.success(data, "加入成功");
    }
}
