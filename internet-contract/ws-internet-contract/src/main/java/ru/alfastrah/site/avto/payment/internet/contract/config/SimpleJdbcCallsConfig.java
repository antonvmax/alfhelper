package ru.alfastrah.site.avto.payment.internet.contract.config;

import oracle.jdbc.OracleTypes;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

@Configuration
public class SimpleJdbcCallsConfig {

    @Bean("pF2AmountMessageJdbcCall")
    public SimpleJdbcCall pF2AmountMessageJdbcCall(@Qualifier("jdbcTemplate") JdbcTemplate jdbcTemplate) {
        return new SimpleJdbcCall(jdbcTemplate)
                .withSchemaName("staff")
                .withCatalogName("inet_card_pak")
                .withProcedureName("p_f2_amount_message")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_contract_id", OracleTypes.DECIMAL),
                        new SqlParameter("p_order", OracleTypes.VARCHAR),
                        new SqlParameter("p_amount", OracleTypes.DECIMAL),
                        new SqlParameter("p_payment_dict_id", OracleTypes.DECIMAL),
                        new SqlOutParameter("p_error", OracleTypes.VARCHAR)
                );
    }

    @Bean("pF2M1MessageJdbcCall")
    public SimpleJdbcCall pF2M1MessageJdbcCall(@Qualifier("jdbcTemplate") JdbcTemplate jdbcTemplate) {
        return new SimpleJdbcCall(jdbcTemplate)
                .withSchemaName("staff")
                .withCatalogName("inet_card_pak")
                .withProcedureName("p_f2m1_message")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_contract_id", OracleTypes.DECIMAL),
                        new SqlParameter("p_order", OracleTypes.VARCHAR),
                        new SqlParameter("p_xml", OracleTypes.VARCHAR),
                        new SqlOutParameter("p_success", OracleTypes.VARCHAR),
                        new SqlOutParameter("p_message", OracleTypes.VARCHAR)
                );
    }

    @Bean("f2m1MessageWithoutEmailJdbcCall")
    public SimpleJdbcCall f2m1MessageWithoutEmailJdbcCall(@Qualifier("jdbcTemplate") JdbcTemplate jdbcTemplate) {
        return new SimpleJdbcCall(jdbcTemplate)
                .withSchemaName("staff")
                .withCatalogName("inet_card_pak")
                .withProcedureName("p_f2m1_message_without_email")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_contract_id", OracleTypes.DECIMAL),
                        new SqlParameter("p_order", OracleTypes.VARCHAR),
                        new SqlParameter("p_xml", OracleTypes.VARCHAR),
                        new SqlOutParameter("p_success", OracleTypes.VARCHAR),
                        new SqlOutParameter("p_message", OracleTypes.VARCHAR)
                );
    }

    @Bean("setPaymentDictJdbcCall")
    public SimpleJdbcCall setPaymentDictJdbcCall(@Qualifier("jdbcTemplate") JdbcTemplate jdbcTemplate) {
        return new SimpleJdbcCall(jdbcTemplate)
                .withSchemaName("staff")
                .withCatalogName("inet_card_pak")
                .withProcedureName("p_set_payment_dict")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_contract_id", OracleTypes.DECIMAL),
                        new SqlParameter("p_order", OracleTypes.VARCHAR),
                        new SqlParameter("p_payment_id", OracleTypes.DECIMAL)
                );
    }

    @Bean("updateStatusLastTransactJdbcCall")
    public SimpleJdbcCall updateStatusLastTransactJdbcCall(@Qualifier("jdbcTemplate") JdbcTemplate jdbcTemplate) {
        return new SimpleJdbcCall(jdbcTemplate)
                .withSchemaName("staff")
                .withCatalogName("loyal_pkg")
                .withProcedureName("p_update_status_last_transact")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_contract_id", OracleTypes.NUMERIC),
                        new SqlParameter("p_status_id", OracleTypes.NUMERIC)
                );
    }

    @Bean("setContractStatusJdbcCall")
    public SimpleJdbcCall setContractStatusJdbcCall(@Qualifier("jdbcTemplate") JdbcTemplate jdbcTemplate) {
        return new SimpleJdbcCall(jdbcTemplate)
                .withSchemaName("staff")
                .withCatalogName("lifecycle_pak")
                .withProcedureName("set_contract_status")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_contract_id", OracleTypes.DECIMAL),
                        new SqlParameter("p_new_stype_id", OracleTypes.INTEGER)
                );
    }

    @Bean("fixInternetSaleJdbcCall")
    public SimpleJdbcCall fixInternetSaleJdbcCall(@Qualifier("jdbcTemplate") JdbcTemplate jdbcTemplate) {
        return new SimpleJdbcCall(jdbcTemplate)
                .withSchemaName("staff")
                .withCatalogName("kasko_imp_utils")
                .withProcedureName("fix_internet_sale");
    }
}
