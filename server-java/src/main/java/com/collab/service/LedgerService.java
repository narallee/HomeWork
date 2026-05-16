package com.collab.service;

import com.collab.common.BusinessException;
import com.collab.mapper.LedgerMapper;
import com.collab.model.entity.Ledger;
import com.collab.model.entity.LedgerMember;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class LedgerService {

    private final LedgerMapper ledgerMapper;

    public LedgerService(LedgerMapper ledgerMapper) {
        this.ledgerMapper = ledgerMapper;
    }

    /**
     * 创建账本
     */
    @Transactional
    public Map<String, String> createLedger(String userId, String name, String description) {
        String ledgerId = UUID.randomUUID().toString();

        Ledger ledger = new Ledger();
        ledger.setId(ledgerId);
        ledger.setName(name);
        ledger.setDescription(description != null ? description : "");
        ledger.setOwnerId(userId);
        ledgerMapper.insertLedger(ledger);

        // 将创建者加入成员列表
        LedgerMember member = new LedgerMember();
        member.setLedgerId(ledgerId);
        member.setUserId(userId);
        member.setRole("owner");
        ledgerMapper.insertMember(member);

        Map<String, String> result = new HashMap<>();
        result.put("ledgerId", ledgerId);
        return result;
    }

    /**
     * 获取用户的账本列表
     */
    public List<Ledger> getLedgers(String userId) {
        return ledgerMapper.findByUserId(userId);
    }

    /**
     * 获取账本详情
     */
    public Map<String, Object> getLedgerDetail(String ledgerId, String userId) {
        // 检查用户是否为该账本成员
        LedgerMember memberCheck = ledgerMapper.findMember(ledgerId, userId);
        if (memberCheck == null) {
            throw BusinessException.forbidden("无权访问该账本");
        }

        Ledger ledger = ledgerMapper.findById(ledgerId);
        if (ledger == null) {
            throw BusinessException.notFound("账本不存在");
        }

        List<LedgerMember> members = ledgerMapper.findMembersByLedgerId(ledgerId);

        Map<String, Object> result = new HashMap<>();
        result.put("id", ledger.getId());
        result.put("name", ledger.getName());
        result.put("description", ledger.getDescription());
        result.put("ownerId", ledger.getOwnerId());
        result.put("inviteCode", ledger.getInviteCode());
        result.put("createdAt", ledger.getCreatedAt());
        result.put("members", members);
        return result;
    }

    /**
     * 邀请成员
     */
    public void inviteMember(String ledgerId, String userId, String targetUserId) {
        // 检查操作者是否有权限
        LedgerMember operator = ledgerMapper.findMember(ledgerId, userId);
        if (operator == null || (!"owner".equals(operator.getRole()) && !"admin".equals(operator.getRole()))) {
            throw BusinessException.forbidden("无权邀请成员");
        }

        // 检查目标用户是否已在账本中
        LedgerMember existing = ledgerMapper.findMember(ledgerId, targetUserId);
        if (existing != null) {
            throw BusinessException.badRequest("该用户已是账本成员");
        }

        LedgerMember member = new LedgerMember();
        member.setLedgerId(ledgerId);
        member.setUserId(targetUserId);
        member.setRole("member");
        ledgerMapper.insertMember(member);
    }

    /**
     * 通过邀请码加入账本
     */
    public Map<String, String> joinByCode(String userId, String inviteCode) {
        Ledger ledger = ledgerMapper.findByInviteCode(inviteCode);
        if (ledger == null) {
            throw BusinessException.notFound("邀请码无效");
        }

        // 检查是否已是成员
        LedgerMember existing = ledgerMapper.findMember(ledger.getId(), userId);
        if (existing != null) {
            throw BusinessException.badRequest("你已是该账本成员");
        }

        LedgerMember member = new LedgerMember();
        member.setLedgerId(ledger.getId());
        member.setUserId(userId);
        member.setRole("member");
        ledgerMapper.insertMember(member);

        Map<String, String> result = new HashMap<>();
        result.put("ledgerId", ledger.getId());
        return result;
    }

    /**
     * 生成邀请码
     */
    public Map<String, String> generateInviteCode(String ledgerId, String userId) {
        LedgerMember operator = ledgerMapper.findMember(ledgerId, userId);
        if (operator == null || (!"owner".equals(operator.getRole()) && !"admin".equals(operator.getRole()))) {
            throw BusinessException.forbidden("无权生成邀请码");
        }

        String inviteCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        ledgerMapper.updateInviteCode(ledgerId, inviteCode);

        Map<String, String> result = new HashMap<>();
        result.put("inviteCode", inviteCode);
        return result;
    }
}
