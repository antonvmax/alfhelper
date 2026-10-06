package ru.alfastrah.site.avto.ws.partners.interaction.validator;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.alfastrah.interplat4.partners.interaction.PayedContractRequest;
import ru.alfastrah.interplat4.partners.interaction.UPIDRequest;
import ru.alfastrah.site.avto.model.partners.interaction.dto.LinkActionRequest;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class RequestValidatorTest {

    private RequestValidator requestValidatorUnderTest;

    @BeforeEach
    void setUp() {
        requestValidatorUnderTest = new RequestValidator();
    }

    @Test
    void testValidate_PayedContractRequest() {
        final PayedContractRequest request = new PayedContractRequest();
        request.setUPID("value");

        assertThatNoException().isThrownBy(() -> requestValidatorUnderTest.validate(request));
    }

    @Test
    void testValidate_PayedContractRequest_ThrowsValidationException() {
        final PayedContractRequest request = new PayedContractRequest();

        assertThatThrownBy(() -> requestValidatorUnderTest.validate(request)).isInstanceOf(ValidationException.class);
    }

    @Test
    void testValidate_UPIDRequest() {
        final UPIDRequest request = new UPIDRequest();
        request.setCallerCode("value");

        assertThatNoException().isThrownBy(() -> requestValidatorUnderTest.validate(request));
    }

    @Test
    void testValidate_UPIDRequest_ThrowsValidationException() {
        final UPIDRequest request = new UPIDRequest();

        assertThatThrownBy(() -> requestValidatorUnderTest.validate(request)).isInstanceOf(ValidationException.class);
    }

    @Test
    void validateLinkRequest_throwException_whenUpidIsBlank() {
        LinkActionRequest request = new LinkActionRequest();
        assertThatThrownBy(() -> requestValidatorUnderTest.validate(request));
    }

    @Test
    void validateLinkRequest_throwException_whenCalcIdIsBlank() {
        LinkActionRequest request = new LinkActionRequest();
        request.setUpid("UPID");
        assertThatThrownBy(() -> requestValidatorUnderTest.validate(request));
    }

    @Test
    void validateLinkRequest() {
        LinkActionRequest request = new LinkActionRequest();
        request.setUpid("UPID");
        request.setCalculationId("CALCULATION_ID");
        assertDoesNotThrow(() -> requestValidatorUnderTest.validate(request));
    }
}
