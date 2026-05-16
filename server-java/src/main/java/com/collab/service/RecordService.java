package com.collab.service;

import com.collab.common.BusinessException;
import com.collab.common.PageResult;
import com.collab.mapper.LedgerMapper;
import com.collab.mapper.RecordMapper;
import com.collab.model.dto.RecordQueryParam;
import com.collab.model.entity.LedgerMember;
import com.collab.model.entity.Record;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RecordService {

    private final RecordMapper recordMapper;
    private final LedgerMapper ledgerMapper;

    public RecordService(RecordMapper recordMapper, LedgerMapper ledgerMapper) {
        this.recordMapper = recordMapper;
        this.ledgerMapper = ledgerMapper;
    }

    /**
     * 添加账目记录
     */
    public Map<String, String> addRecord(String userId, String ledgerId, BigDecimal amount,
                                          String type, String categoryId, String description, String date) {
        // 检查权限
        LedgerMember member = ledgerMapper.findMember(ledgerId, userId);
        if (member == null) {
            throw BusinessException.forbidden("无权操作该账本");
        }

        Record record = new Record();
        record.setId(UUID.randomUUID().toString());
        record.setLedgerId(ledgerId);
        record.setUserId(userId);
        record.setAmount(amount);
        record.setType(type);
        record.setCategoryId(categoryId);
        record.setDescription(description != null ? description : "");

        if (date != null && !date.isEmpty()) {
            record.setRecordDate(LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE));
        } else {
            record.setRecordDate(LocalDate.now());
        }

        recordMapper.insert(record);

        Map<String, String> result = new HashMap<>();
        result.put("recordId", record.getId());
        return result;
    }

    /**
     * 获取账目记录列表
     */
    public PageResult<Record> getRecords(String userId, String ledgerId, int page, int pageSize,
                                          String type, String categoryId, String startDate, String endDate) {
        // 检查权限
        LedgerMember member = ledgerMapper.findMember(ledgerId, userId);
        if (member == null) {
            throw BusinessException.forbidden("无权访问该账本");
        }

        RecordQueryParam param = new RecordQueryParam();
        param.setLedgerId(ledgerId);
        param.setPage(page);
        param.setPageSize(pageSize);
        param.setType(type);
        param.setCategoryId(categoryId);
        param.setStartDate(startDate);
        param.setEndDate(endDate);

        List<Record> records = recordMapper.findByQuery(param);
        long total = recordMapper.countByQuery(param);

        return new PageResult<>(records, total, page, pageSize);
    }

    /**
     * 更新账目记录
     */
    public void updateRecord(String userId, String recordId, BigDecimal amount,
                              String type, String categoryId, String description, String date) {
        Record existingRecord = recordMapper.findById(recordId);
        if (existingRecord == null) {
            throw BusinessException.notFound("记录不存在");
        }

        // 检查权限：只有记录创建者或账本管理员可以编辑
        if (!existingRecord.getUserId().equals(userId)) {
            LedgerMember member = ledgerMapper.findMember(existingRecord.getLedgerId(), userId);
            if (member == null || (!"owner".equals(member.getRole()) && !"admin".equals(member.getRole()))) {
                throw BusinessException.forbidden("无权编辑该记录");
            }
        }

        Record record = new Record();
        record.setId(recordId);
        record.setAmount(amount);
        record.setType(type);
        record.setCategoryId(categoryId);
        record.setDescription(description != null ? description : "");
        if (date != null && !date.isEmpty()) {
            record.setRecordDate(LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE));
        }
        recordMapper.update(record);
    }

    /**
     * 删除账目记录
     */
    public void deleteRecord(String userId, String recordId) {
        Record existingRecord = recordMapper.findById(recordId);
        if (existingRecord == null) {
            throw BusinessException.notFound("记录不存在");
        }

        // 检查权限
        if (!existingRecord.getUserId().equals(userId)) {
            LedgerMember member = ledgerMapper.findMember(existingRecord.getLedgerId(), userId);
            if (member == null || (!"owner".equals(member.getRole()) && !"admin".equals(member.getRole()))) {
                throw BusinessException.forbidden("无权删除该记录");
            }
        }

        recordMapper.deleteById(recordId);
    }

    /**
     * 获取统计信息
     */
    public Map<String, Object> getStatistics(String userId, String ledgerId, String startDate, String endDate) {
        LedgerMember member = ledgerMapper.findMember(ledgerId, userId);
        if (member == null) {
            throw BusinessException.forbidden("无权访问该账本");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("typeStat", recordMapper.statisticsByType(ledgerId, startDate, endDate));
        result.put("categoryStat", recordMapper.statisticsByCategory(ledgerId, startDate, endDate));
        result.put("memberStat", recordMapper.statisticsByMember(ledgerId, startDate, endDate));
        result.put("dailyStat", recordMapper.statisticsByDate(ledgerId, startDate, endDate));
        return result;
    }
}
