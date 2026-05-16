package com.collab.mapper;

import com.collab.model.dto.RecordQueryParam;
import com.collab.model.entity.Record;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface RecordMapper {

    @Insert("INSERT INTO records (id, ledger_id, user_id, amount, type, category_id, description, record_date, created_at) " +
            "VALUES (#{id}, #{ledgerId}, #{userId}, #{amount}, #{type}, #{categoryId}, #{description}, #{recordDate}, NOW())")
    void insert(Record record);

    @Select("SELECT * FROM records WHERE id = #{id}")
    Record findById(@Param("id") String id);

    @Update("UPDATE records SET amount = #{amount}, type = #{type}, category_id = #{categoryId}, " +
            "description = #{description}, record_date = #{recordDate}, updated_at = NOW() WHERE id = #{id}")
    void update(Record record);

    @Delete("DELETE FROM records WHERE id = #{id}")
    void deleteById(@Param("id") String id);

    List<Record> findByQuery(RecordQueryParam param);

    long countByQuery(RecordQueryParam param);

    List<Map<String, Object>> statisticsByType(@Param("ledgerId") String ledgerId,
                                               @Param("startDate") String startDate,
                                               @Param("endDate") String endDate);

    List<Map<String, Object>> statisticsByCategory(@Param("ledgerId") String ledgerId,
                                                    @Param("startDate") String startDate,
                                                    @Param("endDate") String endDate);

    List<Map<String, Object>> statisticsByMember(@Param("ledgerId") String ledgerId,
                                                  @Param("startDate") String startDate,
                                                  @Param("endDate") String endDate);

    List<Map<String, Object>> statisticsByDate(@Param("ledgerId") String ledgerId,
                                               @Param("startDate") String startDate,
                                               @Param("endDate") String endDate);
}
