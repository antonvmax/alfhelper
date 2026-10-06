package ru.alfastrah.site.avto.ws.contact.signed.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.Valid;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * RealContractResponse
 */

public class RealContractResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Статус договора
     */
    public enum StatusEnum {
        TIME_OUT("TIME_OUT"), IN_PROGRESS("IN_PROGRESS"), FAKE_SAVE("FAKE_SAVE"), FAIL("FAIL"), SUCCESS_SAVE(
                "SUCCESS_SAVE");

        private String value;

        StatusEnum(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static StatusEnum fromValue(String text) {
            for (StatusEnum b : StatusEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

    }

    @JsonProperty("status")
    private StatusEnum status = null;

    @JsonProperty("comment")
    private String comment = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("realContractId")
    private Long realContractId = null;

    @JsonProperty("realContractSeria")
    private String realContractSeria = null;

    @JsonProperty("realContractNumber")
    private String realContractNumber = null;

    @JsonProperty("criticalErrors")
    private List criticalErrors = null;

    public RealContractResponse status(StatusEnum status) {
        this.status = status;
        return this;
    }

    /**
     * Статус договора
     *
     * @return status
     **/
    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public RealContractResponse comment(String comment) {
        this.comment = comment;
        return this;
    }

    /**
     * Комментарий
     *
     * @return comment
     **/
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public RealContractResponse description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Описание ошибки
     *
     * @return description
     **/
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RealContractResponse realContractId(Long realContractId) {
        this.realContractId = realContractId;
        return this;
    }

    /**
     * Идентификатор реально загруженного контракта
     *
     * @return realContractId
     **/
    public Long getRealContractId() {
        return realContractId;
    }

    public void setRealContractId(Long realContractId) {
        this.realContractId = realContractId;
    }

    public RealContractResponse realContractSeria(String realContractSeria) {
        this.realContractSeria = realContractSeria;
        return this;
    }

    /**
     * Серия реально загруженного контракта
     *
     * @return realContractSeria
     **/
    public String getRealContractSeria() {
        return realContractSeria;
    }

    public void setRealContractSeria(String realContractSeria) {
        this.realContractSeria = realContractSeria;
    }

    public RealContractResponse realContractNumber(String realContractNumber) {
        this.realContractNumber = realContractNumber;
        return this;
    }

    /**
     * Номер реально загруженного контракта
     *
     * @return realContractNumber
     **/
    public String getRealContractNumber() {
        return realContractNumber;
    }

    public void setRealContractNumber(String realContractNumber) {
        this.realContractNumber = realContractNumber;
    }

    public RealContractResponse criticalErrors(List criticalErrors) {
        this.criticalErrors = criticalErrors;
        return this;
    }

    /**
     * Критические ошибки ответа шлюза
     *
     * @return criticalErrors
     **/
    @Valid
    public List getCriticalErrors() {
        return criticalErrors;
    }

    public void setCriticalErrors(List criticalErrors) {
        this.criticalErrors = criticalErrors;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RealContractResponse realContractResponse = (RealContractResponse) o;
        return Objects.equals(this.status, realContractResponse.status)
                && Objects.equals(this.comment, realContractResponse.comment)
                && Objects.equals(this.description, realContractResponse.description)
                && Objects.equals(this.realContractId, realContractResponse.realContractId)
                && Objects.equals(this.realContractSeria, realContractResponse.realContractSeria)
                && Objects.equals(this.realContractNumber, realContractResponse.realContractNumber)
                && Objects.equals(this.criticalErrors, realContractResponse.criticalErrors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, comment, description, realContractId, realContractSeria, realContractNumber,
                criticalErrors);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RealContractResponse {\n");

        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    comment: ").append(toIndentedString(comment)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    realContractId: ").append(toIndentedString(realContractId)).append("\n");
        sb.append("    realContractSeria: ").append(toIndentedString(realContractSeria)).append("\n");
        sb.append("    realContractNumber: ").append(toIndentedString(realContractNumber)).append("\n");
        sb.append("    criticalErrors: ").append(toIndentedString(criticalErrors)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}
