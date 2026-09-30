package ru.alfastrah.site.avto.ws.partners.interaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Результат поиска договора, полностью совместимый с Oracle процедурой P_GET_CONTRACT_ID
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractResult {

    /**
     * ID договора (возвращается только если договор найден и оплачен)
     */
    private Long contractId;

    /**
     * Код статуса:
     * - null - успех (договор найден и оплачен)
     * - 0 - договор еще не создан в системе
     * - 1 - договор создан, но еще не оплачен
     */
    private Integer code;

    /**
     * Текстовое сообщение об ошибке (null при успехе)
     */
    private String message;
}