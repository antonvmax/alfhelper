package ru.alfastrah.site.avto.ws.partners.interaction.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import ru.alfastrah.site.avto.ws.partners.interaction.dto.PartnerCalculation;

import java.util.Optional;
import java.util.UUID;

@Mapper
public interface PartnersPostgresMapper {

    @Insert("""
        INSERT INTO partners_interaction.partner_identifiers (upid, date_insert, caller_code)
        VALUES (#{upid}, CURRENT_TIMESTAMP, #{callerCode})
        """)
    void insertPartnerIdentifier(@Param("upid") UUID upid, @Param("callerCode") String callerCode);

    @Insert("""
        INSERT INTO partners_interaction.partner_calculation (upid, calc_id, date_insert)
        VALUES (#{upid}, #{calcId}, CURRENT_TIMESTAMP)
        """)
    @Options(useGeneratedKeys = true, keyProperty = "calculationId")
    void insertPartnerCalculation(PartnerCalculation partnerCalculation);

    @Select("""
        SELECT
        calculation_id calculationId,
        calc_id calcId,
        contract_id contractId
        FROM partners_interaction.partner_calculation
        WHERE upid = #{upid} AND calc_id = #{calcId}
        LIMIT 1
        """)
    Optional<PartnerCalculation> findByUpidAndCalcId(@Param("upid") UUID upid, @Param("calcId") String calcId);

    @Update("""
        UPDATE partners_interaction.partner_calculation
        SET contract_id = #{contractId}
        WHERE calculation_id = #{calculationId}
        """)
    void updateContractId(@Param("calculationId") Long calculationId, @Param("contractId") Long contractId);

    @Select("""
        SELECT contract_id
        FROM partners_interaction.partner_calculation
        WHERE upid = #{upid} AND contract_id IS NOT NULL
        LIMIT 1
        """)
    Optional<Long> findContractIdByUpid(@Param("upid") UUID upid);

    @Select("""
        SELECT contract_id
        FROM partners_interaction.partner_calculation
        WHERE upid = #{upid} AND contract_id = #{contract_id}
        LIMIT 1
        """)
    Optional<Long> findByUpidAndContractId(@Param("upid") UUID upid, @Param("contract_id") Long contractId);
}