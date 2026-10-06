package ru.alfastrah.site.avto.ws.contact.signed.db.signing.stamp;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.type.JdbcType;
import ru.alfastrah.site.avto.ws.contact.signed.db.model.StampParams;

import java.math.BigInteger;
import java.util.List;

@Mapper
public interface SignatureMapper {
    @Results(value = {
            @Result(column = "llx", property = "llx", jdbcType = JdbcType.FLOAT),
            @Result(column = "lly", property = "lly", jdbcType = JdbcType.FLOAT),
            @Result(column = "urx", property = "urx", jdbcType = JdbcType.FLOAT),
            @Result(column = "ury", property = "ury", jdbcType = JdbcType.FLOAT),
            @Result(column = "font_size", property = "fontSize", jdbcType = JdbcType.INTEGER),
            @Result(column = "page", property = "page", jdbcType = JdbcType.INTEGER),
            @Result(column = "is_stamp_visible", property = "isStampVisible", jdbcType = JdbcType.BOOLEAN)
    })
    @Select("SELECT sp.llx, sp.lly, sp.urx, sp.ury, sp.font_size, sp.page, sp.is_stamp_visible " +
            "FROM public.product_policy_signature_params sp " +
            "WHERE sp.product_id = #{productId} and sp.form_id = #{formId}")
    List<StampParams> getStampParams(@Param("productId") String productId, @Param("formId") String formId);


    @Select("Select val from public.params where param=#{param}")
    String getSigningParam(@Param("param") String paramName);

    @Insert("insert into public.not_sent_policies(contract_id, insert_date_time) values (#{contractId}, now())")
    void insertData(@Param("contractId") BigInteger contractId);
}
