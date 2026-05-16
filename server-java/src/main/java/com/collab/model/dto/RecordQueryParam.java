package com.collab.model.dto;

import lombok.Data;

@Data
public class RecordQueryParam {
    private String ledgerId;
    private String type;
    private String categoryId;
    private String startDate;
    private String endDate;
    private int page = 1;
    private int pageSize = 20;

    public int getOffset() {
        return (page - 1) * pageSize;
    }
}
