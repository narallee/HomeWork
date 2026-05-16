package com.collab.controller;

import com.collab.common.PageResult;
import com.collab.common.Result;
import com.collab.model.dto.AddRecordRequest;
import com.collab.model.dto.UpdateRecordRequest;
import com.collab.model.entity.Record;
import com.collab.service.RecordService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/records")
public class RecordController {

    private final RecordService recordService;

    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    /**
     * 添加账目记录
     */
    @PostMapping
    public Result<Map<String, String>> addRecord(HttpServletRequest request,
                                                  @Valid @RequestBody AddRecordRequest body) {
        String userId = (String) request.getAttribute("userId");
        Map<String, String> data = recordService.addRecord(
                userId, body.getLedgerId(), body.getAmount(),
                body.getType(), body.getCategoryId(), body.getDescription(), body.getDate());
        return Result.success(data, "添加成功");
    }

    /**
     * 获取账目记录列表
     */
    @GetMapping("/ledger/{ledgerId}")
    public Result<PageResult<Record>> getRecords(HttpServletRequest request,
                                                  @PathVariable String ledgerId,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "20") int pageSize,
                                                  @RequestParam(required = false) String type,
                                                  @RequestParam(required = false) String categoryId,
                                                  @RequestParam(required = false) String startDate,
                                                  @RequestParam(required = false) String endDate) {
        String userId = (String) request.getAttribute("userId");
        PageResult<Record> data = recordService.getRecords(userId, ledgerId, page, pageSize,
                type, categoryId, startDate, endDate);
        return Result.success(data);
    }

    /**
     * 更新账目记录
     */
    @PutMapping("/{recordId}")
    public Result<Void> updateRecord(HttpServletRequest request,
                                      @PathVariable String recordId,
                                      @Valid @RequestBody UpdateRecordRequest body) {
        String userId = (String) request.getAttribute("userId");
        recordService.updateRecord(userId, recordId, body.getAmount(),
                body.getType(), body.getCategoryId(), body.getDescription(), body.getDate());
        return Result.success("更新成功");
    }

    /**
     * 删除账目记录
     */
    @DeleteMapping("/{recordId}")
    public Result<Void> deleteRecord(HttpServletRequest request, @PathVariable String recordId) {
        String userId = (String) request.getAttribute("userId");
        recordService.deleteRecord(userId, recordId);
        return Result.success("删除成功");
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/statistics/{ledgerId}")
    public Result<Map<String, Object>> getStatistics(HttpServletRequest request,
                                                      @PathVariable String ledgerId,
                                                      @RequestParam(required = false) String startDate,
                                                      @RequestParam(required = false) String endDate) {
        String userId = (String) request.getAttribute("userId");
        Map<String, Object> data = recordService.getStatistics(userId, ledgerId, startDate, endDate);
        return Result.success(data);
    }
}
