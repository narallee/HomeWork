package com.collab.mapper;

import com.collab.model.entity.Ledger;
import com.collab.model.entity.LedgerMember;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LedgerMapper {

    @Insert("INSERT INTO ledgers (id, name, description, owner_id, created_at) VALUES (#{id}, #{name}, #{description}, #{ownerId}, NOW())")
    void insertLedger(Ledger ledger);

    @Select("SELECT * FROM ledgers WHERE id = #{id}")
    Ledger findById(@Param("id") String id);

    @Select("SELECT * FROM ledgers WHERE invite_code = #{inviteCode}")
    Ledger findByInviteCode(@Param("inviteCode") String inviteCode);

    @Update("UPDATE ledgers SET invite_code = #{inviteCode} WHERE id = #{id}")
    void updateInviteCode(@Param("id") String id, @Param("inviteCode") String inviteCode);

    List<Ledger> findByUserId(@Param("userId") String userId);

    // 成员操作
    @Insert("INSERT INTO ledger_members (ledger_id, user_id, role, joined_at) VALUES (#{ledgerId}, #{userId}, #{role}, NOW())")
    void insertMember(LedgerMember member);

    @Select("SELECT * FROM ledger_members WHERE ledger_id = #{ledgerId} AND user_id = #{userId}")
    LedgerMember findMember(@Param("ledgerId") String ledgerId, @Param("userId") String userId);

    List<LedgerMember> findMembersByLedgerId(@Param("ledgerId") String ledgerId);
}
