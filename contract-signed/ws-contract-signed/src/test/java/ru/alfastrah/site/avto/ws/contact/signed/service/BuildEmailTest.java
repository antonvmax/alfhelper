package ru.alfastrah.site.avto.ws.contact.signed.service;

import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import ru.alfastrah.site.avto.client.unicus.db.samplePak.SamplePakClient;
import ru.alfastrah.site.avto.model.contract.signed.exception.BadDataException;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoException;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoExceptionMailNoStacktrace;
import ru.alfastrah.site.avto.model.contract.signed.exception.EOsagoSaveException;
import ru.alfastrah.site.avto.model.contract.signed.model.rest.SendContractSignedRequest;
import ru.alfastrah.site.avto.model.unicus.db.dto.contract.ContractInfoDto;
import ru.alfastrah.site.avto.model.unicus.db.dto.samplePak.EmailTemplateDto;
import ru.alfastrah.site.avto.unicus.services.UnicusSubjectService;
import ru.alfastrah.site.avto.unicus.services.UnicusUsrService;
import ru.alfastrah.site.avto.ws.contact.signed.client.PdfClient;
import ru.alfastrah.site.avto.ws.contact.signed.db.MarketName;
import ru.alfastrah.site.avto.ws.contact.signed.db.PartnersDb;
import ru.alfastrah.site.avto.ws.contact.signed.model.MessageProperies;
import ru.alfastrah.site.avto.ws.contact.signed.service.printform.id.impl.OsagoPrintFormIdRetriever;
import ru.alfastrah.site.avto.ws.contact.signed.service.signing.ErrorToleranceSigningService;
import ru.alfastrah.site.avto.ws.contact.signed.service.signing.stamp.StampService;
import tops.unicus.subject.RJuridicalPerson;
import tops.unicus.subject.RPhysicalPerson;
import tops.unicus.usr.RContractRestrictionUser;
import tops.unicus.usr.RContractVariant;
import tops.unicus.usr.RSaleContract;

import java.math.BigInteger;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.noInteractions;
import static ru.alfastrah.interplat4.bus.utils.print.form.Constant.JASPER_PRINTED_FORM_ID_1;
import static ru.alfastrah.site.avto.unicus.constant.UnicusContractOption.INITIAL_CONTRACT;
import static ru.alfastrah.site.avto.unicus.constant.UnicusContractOption.PROLONGATION;
import static ru.alfastrah.site.avto.unicus.constant.UnicusContractStatusType.CONCLUDED;
import static ru.alfastrah.site.avto.unicus.constant.UnicusContractStatusType.CONFIRMED;
import static ru.alfastrah.site.avto.unicus.constant.UnicusContractStatusType.STATEMENT;
import static ru.alfastrah.site.avto.ws.contact.signed.db.MarketName.ALPHA_REPAIR_OFFLINE;
import static ru.alfastrah.site.avto.ws.contact.signed.db.MarketName.NS_ADDITIONAL_PROTECTION;
import static ru.alfastrah.site.avto.ws.contact.signed.db.MarketName.NS_ALFA_VACCINE;
import static ru.alfastrah.site.avto.ws.contact.signed.db.MarketName.NS_CHILD_SPORT;
import static ru.alfastrah.site.avto.ws.contact.signed.db.MarketName.NS_FAMILY_PROTECTION;
import static ru.alfastrah.site.avto.ws.contact.signed.db.MarketName.NS_NO_CORONAVIRUS;
import static ru.alfastrah.site.avto.ws.contact.signed.db.MarketName.NS_VIRUS_PROTECTION;
import static ru.alfastrah.site.avto.ws.contact.signed.db.MarketName.PROPERTY_REPAIR;
import static ru.alfastrah.site.avto.ws.contact.signed.db.MarketName.UNKNOWN;
import static ru.alfastrah.site.avto.ws.contact.signed.service.BuildEmail.NO_EMAIL_FOR_SENDING;
import static ru.alfastrah.site.avto.ws.contact.signed.service.Product.GOOD_NEIGHBORS;
import static ru.alfastrah.site.avto.ws.contact.signed.service.Product.KASKO;
import static ru.alfastrah.site.avto.ws.contact.signed.service.Product.NS397;
import static ru.alfastrah.site.avto.ws.contact.signed.service.Product.NS398;
import static ru.alfastrah.site.avto.ws.contact.signed.service.Product.OSAGO;
import static ru.alfastrah.site.avto.ws.contact.signed.service.Product.PRODUCT_MORTGAGE_LIFE;
import static ru.alfastrah.site.avto.ws.contact.signed.service.Product.PRODUCT_MORTGAGE_PROPERTY;

@ExtendWith(MockitoExtension.class)
class BuildEmailTest {

    @Mock
    private PartnersDb partnersDbBean;
    @Mock
    private SamplePakClient samplePakClient;
    @Mock
    private MarketNameService marketNameBean;
    @Mock
    private PdfClient selfMadePdfClient;
    @Mock
    private UnicusUsrService unicusUsrClient;
    @Mock
    private UnicusSubjectService unicusSubjectClient;
    @Mock
    private MessageProperies messageProperies;
    @Mock
    private OsagoPrintFormIdRetriever osagoPrintFormIdRetriever;
    @Mock
    private StampService stampService;
    @Mock
    private ErrorToleranceSigningService signingService;

    private String sendNotificationKaskoEmailList;
    private String sendNotificationGeneralEmailList;

    private BuildEmail underTest;

    @BeforeEach
    void setUp() {
        sendNotificationKaskoEmailList = "kasko@mail.ru";
        sendNotificationGeneralEmailList = "general@mail.ru";

        underTest = new BuildEmail(partnersDbBean,
                samplePakClient,
                marketNameBean,
                selfMadePdfClient,
                unicusUsrClient,
                unicusSubjectClient,
                messageProperies,
                osagoPrintFormIdRetriever,
                stampService, signingService,
                sendNotificationKaskoEmailList,
                sendNotificationGeneralEmailList);
    }

    @Test
    void shouldCreateEmailBody() throws MessagingException, EOsagoSaveException {
        MimeMessageHelper helper = Mockito.mock(MimeMessageHelper.class);
        SendContractSignedRequest request = new SendContractSignedRequest();
        request.setEmail("KatorginMi@alfastrah.ru");
        request.setContractId(BigInteger.TEN);
        RSaleContract contract = new RSaleContract();
        given(unicusUsrClient.getContract(BigInteger.TEN.longValue())).willReturn(contract);
        RPhysicalPerson physicalPerson = new RPhysicalPerson();
        physicalPerson.setFirstName("first");
        physicalPerson.setLastName("last");
        physicalPerson.setMiddleName("middle");
        physicalPerson.setBirthDate(LocalDate.now().minusYears(23));
        given(unicusSubjectClient.getPhysicalPerson(0L)).willReturn(physicalPerson);

        // Mock для SamplePakClient
        EmailTemplateDto mockResponse = new EmailTemplateDto();
        mockResponse.setLetterText("Email Body Text");
        mockResponse.setLetterSubject("Email Subject");
        given(samplePakClient.getBigEmailLetter(any())).willReturn(mockResponse);

        underTest.createEmailBody(helper, request);

        verify(samplePakClient, times(1)).getBigEmailLetter(any());
    }

    @Test
    void shouldThrowExceptionWhenNoContractFound() {
        MimeMessageHelper helper = Mockito.mock(MimeMessageHelper.class);
        SendContractSignedRequest request = new SendContractSignedRequest();
        request.setEmail("Email");
        request.setContractId(BigInteger.TEN);

        EmailTemplateDto mockResponse = new EmailTemplateDto();
        mockResponse.setLetterText("Test Body");
        mockResponse.setLetterSubject("Test Subject");
        given(samplePakClient.getBigEmailLetter(any())).willReturn(mockResponse);

        assertThatThrownBy(() -> underTest.createEmailBody(helper, request))
                .isInstanceOf(EOsagoSaveException.class)
                .hasMessageContaining("Договор не найден");
    }


    @Test
    void shouldThrowExceptionWhenEmptyEmailGiven() {
        MimeMessageHelper helper = Mockito.mock(MimeMessageHelper.class);
        SendContractSignedRequest request = new SendContractSignedRequest();

        assertThatThrownBy(() -> underTest.createEmailBody(helper, request))
                .isInstanceOf(BadDataException.class)
                .hasMessageContaining(NO_EMAIL_FOR_SENDING);
    }


    @Test
    void shouldNotCreateAdditionalEmailBodyWhenItIsEmpty() throws MessagingException, EOsagoExceptionMailNoStacktrace {
        JavaMailSenderImpl jms = new JavaMailSenderImpl();
        MimeMessage mimeMessage = jms.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        SendContractSignedRequest request = new SendContractSignedRequest();
        request.setEmail("KatorginMi@alfastrah.ru");
        request.setContractId(BigInteger.TEN);
        // Mock для SamplePakClient возвращает результат с пустыми полями
        EmailTemplateDto mockResponse = new EmailTemplateDto();
        mockResponse.setLetterText("");
        mockResponse.setLetterSubject("");
        given(samplePakClient.getEmailLetter(any())).willReturn(mockResponse);

        underTest.createAdditionalEmailBody(helper, request);

        verify(samplePakClient, times(1)).getEmailLetter(any());
    }

    @Test
    void shouldCreateAdditionalEmailBody() throws MessagingException, EOsagoExceptionMailNoStacktrace {
        JavaMailSenderImpl jms = new JavaMailSenderImpl();
        MimeMessage mimeMessage = jms.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        SendContractSignedRequest request = new SendContractSignedRequest();
        request.setEmail("KatorginMi@alfastrah.ru");
        request.setContractId(BigInteger.TEN);
        // Mock для SamplePakClient возвращает результат с контентом
        EmailTemplateDto mockResponse = new EmailTemplateDto();
        mockResponse.setLetterText("LetterText");
        mockResponse.setLetterSubject("LetterSubject");
        given(samplePakClient.getEmailLetter(any())).willReturn(mockResponse);

        underTest.createAdditionalEmailBody(helper, request);

        verify(samplePakClient, times(1)).getEmailLetter(any());
    }

    @Test
    void shouldThrowExceptionWhenEmptyEmailGivenForAdditionalEmail() {
        MimeMessageHelper helper = Mockito.mock(MimeMessageHelper.class);
        SendContractSignedRequest request = new SendContractSignedRequest();

        assertThatThrownBy(() -> underTest.createAdditionalEmailBody(helper, request))
                .isInstanceOf(BadDataException.class)
                .hasMessageContaining(NO_EMAIL_FOR_SENDING);
    }

    @Test
    void shouldCreateNotificationEmailForPhysycalPerson() throws MessagingException, EOsagoExceptionMailNoStacktrace {
        JavaMailSenderImpl jms = new JavaMailSenderImpl();
        MimeMessageHelper helper = new MimeMessageHelper(jms.createMimeMessage(), true);
        SendContractSignedRequest request = new SendContractSignedRequest();
        request.setContractId(BigInteger.TEN);
        RSaleContract contractInfo = createContract(KASKO.getProductId());
        given(partnersDbBean.getContractInfo(BigInteger.TEN)).willReturn(contractInfo);
        given(marketNameBean.getMarketName(contractInfo)).willReturn(MarketName.KASKO_1);
        RSaleContract contract = new RSaleContract();
        contract.setSubjectId(12);
        given(unicusUsrClient.getFullSaleContract(BigInteger.TEN.longValue())).willReturn(contract);
        given(unicusSubjectClient.getPhysicalPerson(12)).willReturn(new RPhysicalPerson());

        underTest.createNotificationEmail(helper, request);

        assertThatNoException().isThrownBy(() -> underTest.createNotificationEmail(helper, request));
    }

    @Test
    void shouldCreateNotificationEmailForJuridicalPerson() throws MessagingException, EOsagoExceptionMailNoStacktrace {
        JavaMailSenderImpl jms = new JavaMailSenderImpl();
        MimeMessageHelper helper = new MimeMessageHelper(jms.createMimeMessage(), true);
        SendContractSignedRequest request = new SendContractSignedRequest();
        request.setContractId(BigInteger.TEN);
        RSaleContract contractInfo = createContract(KASKO.getProductId());
        given(partnersDbBean.getContractInfo(BigInteger.TEN)).willReturn(contractInfo);
        given(marketNameBean.getMarketName(contractInfo)).willReturn(MarketName.KASKO_1);
        RSaleContract contract = new RSaleContract();
        contract.setSubjectId(12);
        given(unicusUsrClient.getFullSaleContract(BigInteger.TEN.longValue())).willReturn(contract);
        given(unicusSubjectClient.getPhysicalPerson(12)).willReturn(null);
        given(unicusSubjectClient.getJuridicalPerson(12)).willReturn(new RJuridicalPerson());

        underTest.createNotificationEmail(helper, request);

        assertThatNoException().isThrownBy(() -> underTest.createNotificationEmail(helper, request));
    }

    @Test
    void shouldNotCreateNonNotificationEmail() throws MessagingException, EOsagoExceptionMailNoStacktrace {
        JavaMailSenderImpl jms = new JavaMailSenderImpl();
        MimeMessageHelper helper = new MimeMessageHelper(jms.createMimeMessage(), true);
        SendContractSignedRequest request = new SendContractSignedRequest();
        request.setContractId(BigInteger.TEN);
        given(partnersDbBean.getContractInfo(BigInteger.TEN)).willReturn(null);


        underTest.createNotificationEmail(helper, request);

        verify(unicusUsrClient, noInteractions()).getFullSaleContract(any());
    }

    @Test
    void shouldNotCreateEmailWhenEmailIsEmpty() throws MessagingException, EOsagoExceptionMailNoStacktrace {
        sendNotificationKaskoEmailList = "";
        sendNotificationGeneralEmailList = "";
        underTest = new BuildEmail(partnersDbBean,
                samplePakClient,
                marketNameBean,
                selfMadePdfClient,
                unicusUsrClient,
                unicusSubjectClient,
                messageProperies,
                osagoPrintFormIdRetriever, stampService, signingService,
                sendNotificationKaskoEmailList,
                sendNotificationGeneralEmailList);
        JavaMailSenderImpl jms = new JavaMailSenderImpl();
        MimeMessageHelper helper = new MimeMessageHelper(jms.createMimeMessage(), true);
        SendContractSignedRequest request = new SendContractSignedRequest();
        request.setContractId(BigInteger.TEN);
        RSaleContract contractInfo = createContract(KASKO.getProductId());
        given(partnersDbBean.getContractInfo(BigInteger.TEN)).willReturn(contractInfo);
        given(marketNameBean.getMarketName(contractInfo)).willReturn(MarketName.KASKO_1);

        underTest.createNotificationEmail(helper, request);

        verify(unicusUsrClient, noInteractions()).getFullSaleContract(any());
    }

    @Test
    void shouldThrowExceptionWhenNoRSaleContractFound() throws MessagingException {
        JavaMailSenderImpl jms = new JavaMailSenderImpl();
        MimeMessageHelper helper = new MimeMessageHelper(jms.createMimeMessage(), true);
        SendContractSignedRequest request = new SendContractSignedRequest();
        request.setContractId(BigInteger.TEN);
        RSaleContract contractInfo = createContract(KASKO.getProductId());
        given(partnersDbBean.getContractInfo(BigInteger.TEN)).willReturn(contractInfo);
        given(marketNameBean.getMarketName(contractInfo)).willReturn(MarketName.KASKO_1);
        given(unicusUsrClient.getFullSaleContract(BigInteger.TEN.longValue())).willReturn(null);

        assertThatThrownBy(() -> underTest.createNotificationEmail(helper, request))
                .isInstanceOf(EOsagoExceptionMailNoStacktrace.class)
                .hasMessageContaining("Не удалось получить доп. информацию");
    }

    @ParameterizedTest
    @MethodSource("isSendNotificationEmail")
    void isSendNotificationEmail(String productId, MarketName marketName, boolean result) {
        BigInteger contractId = BigInteger.ONE;
        RSaleContract contractInfo = createContract(productId);
        given(marketNameBean.getMarketName(contractInfo)).willReturn(marketName);

        boolean actualResult = underTest.isSendNotificationEmail(contractId, contractInfo);

        assertThat(actualResult).isEqualTo(result);

    }

    private static Stream<Arguments> isSendNotificationEmail() {
        return Stream.of(
                Arguments.of(KASKO.getProductId(), MarketName.KASKO_1, true),
                Arguments.of(KASKO.getProductId(), MarketName.KASKO_500, true),
                Arguments.of(KASKO.getProductId(), MarketName.KASKO_3, true),
                Arguments.of(KASKO.getProductId(), MarketName.KASKO_10, false),
                Arguments.of(NS397.getProductId(), NS_FAMILY_PROTECTION, true),
                Arguments.of(NS397.getProductId(), NS_FAMILY_PROTECTION, true),
                Arguments.of(NS397.getProductId(), NS_NO_CORONAVIRUS, true),
                Arguments.of(NS397.getProductId(), NS_ADDITIONAL_PROTECTION, true),
                Arguments.of(NS397.getProductId(), NS_VIRUS_PROTECTION, true),
                Arguments.of(NS397.getProductId(), NS_ALFA_VACCINE, true),
                Arguments.of(NS397.getProductId(), NS_CHILD_SPORT, true),
                Arguments.of(NS397.getProductId(), MarketName.ALFA_HOUSE, false),
                Arguments.of(PRODUCT_MORTGAGE_LIFE.getProductId(), null, true),
                Arguments.of(PRODUCT_MORTGAGE_PROPERTY.getProductId(), null, true),
                Arguments.of("123", null, false)
        );
    }

    @Test
    void shouldReturnFalseOnNotificationCheckWhenContractIdOrInfoIsNull() {
        BigInteger contractId = BigInteger.TEN;
        RSaleContract contractInfo = new RSaleContract();

        boolean result = underTest.isSendNotificationEmail(null, contractInfo);
        assertThat(result).isFalse();
        result = underTest.isSendNotificationEmail(contractId, null);
        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnKaskoEmailListWhenKaskoProductId() {
        RSaleContract contractInfo = createContract(KASKO.getProductId());

        String actualResult = underTest.getSendNotificationDestination(contractInfo);

        assertThat(actualResult).isEqualTo(this.sendNotificationKaskoEmailList);
    }

    @Test
    void shouldReturnGeneralEmailListWhenNotKaskoProductId() {
        RSaleContract contractInfo = createContract(OSAGO.getProductId());

        String actualResult = underTest.getSendNotificationDestination(contractInfo);

        assertThat(actualResult).isEqualTo(this.sendNotificationGeneralEmailList);
    }

    @Test
    void shouldReturnEmptyWhenNoContractInfoGiven() {
        assertThat(underTest.getSendNotificationDestination(null)).isEmpty();
    }

    @Test
    void shouldReturnSignatureForAdditionalKasko() {
        ContractInfoDto contractInfo = new ContractInfoDto();
        contractInfo.setStatusId(5);
        contractInfo.setProductId("046");

        given(partnersDbBean.getAdditionalKaskoInfo(any(), any())).willReturn(contractInfo);

        assertDoesNotThrow(() -> underTest.getAdditionalKaskoToOsago(null, null, true));
    }

    @Test
    void shouldReturnSignatureWhenNotAdditionalKaskoButWithUPIDContract() {
        given(partnersDbBean.getAdditionalKaskoInfo(any(), any())).willReturn(null);
//        given(partnersDbBean.isUPIDWithContract(any(), any())).willReturn(true);

        assertDoesNotThrow(() -> underTest.getAdditionalKaskoToOsago(null, null, true));
    }

    @Test
    void shouldThrowExceptionWhenContractIsInWrongStatus() {
        ContractInfoDto contractInfo = new ContractInfoDto();
        contractInfo.setStatusId(1);

        given(partnersDbBean.getAdditionalKaskoInfo(any(), any())).willReturn(new ContractInfoDto());

        assertThatThrownBy(() -> underTest.getAdditionalKaskoToOsago(null, null, true))
                .isInstanceOf(EOsagoExceptionMailNoStacktrace.class);
    }

    @Test
    void shouldThrowExceptionWhenNoProductIdSpecified() {
        ContractInfoDto contractInfo = new ContractInfoDto();
        contractInfo.setStatusId(1);
        given(partnersDbBean.getAdditionalKaskoInfo(any(), any())).willReturn(contractInfo);

        assertThatThrownBy(() -> underTest.getAdditionalKaskoToOsago(null, null, true))
                .isInstanceOf(EOsagoExceptionMailNoStacktrace.class);
    }

    @Test
    void createSignedContent() {
        given(selfMadePdfClient.getPrintedFormByContractId(any(), any(), any())).willReturn(new DataHandler(new FileDataSource(Path.of("src/test/resources/polis.pdf").toFile())));

        assertThatNoException().isThrownBy(() -> underTest.createSignedContent(BigInteger.TEN, "525"));
    }

    @Test
    void shouldThrowExceptionOnContentCreation() {
        given(selfMadePdfClient
                .getPrintedFormByContractId(any(), any(), any()))
                .willReturn(new DataHandler(new ByteArrayDataSource(new byte[0], "type")));

        assertThatThrownBy(() ->
                underTest.createSignedContent(BigInteger.TEN, "525"))
                .isInstanceOf(EOsagoExceptionMailNoStacktrace.class)
                .hasMessageContaining("Отсутствует контент для подписи");
    }

    @Test
    void shouldReturnWhenParamsContainsCustomOrOracle() throws EOsagoException {
        String actualResult1 = underTest.getPrintFormId(new RSaleContract(), "123");
        String actualResult2 = underTest.getPrintFormId(new RSaleContract(), "321");
        assertThat(actualResult1).isEqualTo("123");
        assertThat(actualResult2).isEqualTo("321");
    }

    @Test
    void shouldReturnCorrectFormIdForOsago() throws EOsagoException {
        RSaleContract contractInfo = createContract(OSAGO.getProductId(), CONCLUDED);
        given(osagoPrintFormIdRetriever.retrieve(contractInfo)).willReturn("525").willReturn("526").willReturn("541");

        assertThat(underTest.getPrintFormId(contractInfo, "")).isEqualTo("525");

        contractInfo.setContractStatusTypeId(CONFIRMED);
        assertThat(underTest.getPrintFormId(contractInfo, "")).isEqualTo("526");

        contractInfo.setContractStatusTypeId(STATEMENT);
        assertThat(underTest.getPrintFormId(contractInfo, "")).isEqualTo("541");
    }

    @Test
    void shouldReturnFormForNS398() throws EOsagoException {
        RSaleContract contractInfo = createContract(NS398.getProductId());
        assertThat(underTest.getPrintFormId(contractInfo, "")).isEqualTo("711");

    }

    @ParameterizedTest
    @MethodSource("providerForShouldReturnFormsForNS397")
    void shouldReturnFormsForNS397() throws EOsagoException {
        RSaleContract contractInfo = createContract(Product.NS397.getProductId());
        given(marketNameBean.getNSMarketNameId(any())).willReturn(NS_CHILD_SPORT);
        assertThat(underTest.getPrintFormId(contractInfo, "")).isEqualTo(JASPER_PRINTED_FORM_ID_1);
    }

    private static Stream<Arguments> providerForShouldReturnFormsForNS397() {
        return Stream.of(
                Arguments.of(6896007L),
                Arguments.of(6894686L),
                Arguments.of(6912589L),
                Arguments.of(6914029L),
                Arguments.of(6916569L),
                Arguments.of(6894507L)
        );
    }

    @Test
    void shouldReturnEmptyForUnknownNS397MarketName() throws EOsagoException {
        RSaleContract contractInfo = createContract(NS397.getProductId());
        given(marketNameBean.getNSMarketNameId(any())).willReturn(UNKNOWN);
        assertThat(underTest.getPrintFormId(contractInfo, "")).isBlank();
    }

    @Test
    void shouldReturnPrintFormIdForProlongationNotSamePolicyGoodNeighbours() throws EOsagoException {
        RSaleContract contractInfo = createContract(GOOD_NEIGHBORS.getProductId(), CONFIRMED, PROLONGATION);

        assertThat(underTest.getPrintFormId(contractInfo, "")).isEqualTo("920");
    }

    @Test
    void shouldReturnPrintFormIdForProlongationSamePolicyGoodNeighbours() throws EOsagoException {
        RSaleContract contractInfo = createContract(GOOD_NEIGHBORS.getProductId(), STATEMENT, PROLONGATION);

        assertThat(underTest.getPrintFormId(contractInfo, "")).isEqualTo("921");
    }

    @Test
    void shouldReturnPrintFormIdForNotProlongationSamePolicyGoodNeighbours() throws EOsagoException {
        RSaleContract contractInfo = createContract(GOOD_NEIGHBORS.getProductId(), STATEMENT, INITIAL_CONTRACT);

        assertThat(underTest.getPrintFormId(contractInfo, "")).isEqualTo("399");
    }

    @Test
    void shouldReturnPrintFormIdForNotProlongationNotSamePolicyGoodNeighbours() throws EOsagoException {
        RSaleContract contractInfo = createContract(GOOD_NEIGHBORS.getProductId(), CONFIRMED, INITIAL_CONTRACT);

        assertThat(underTest.getPrintFormId(contractInfo, "")).isEqualTo("399");
    }

    @Test
    void shouldReturnDriversAmount() {
        Long contractId = 11L;
        List<RContractVariant> variantList = new ArrayList<>();
        RContractVariant variant = new RContractVariant();
        variant.setContractVariantId(12L);
        variantList.add(variant);
        given(unicusUsrClient.getContractVariantList(contractId)).willReturn(variantList);
        List<RContractRestrictionUser> userList = new ArrayList<>();
        RContractRestrictionUser user1 = new RContractRestrictionUser();
        RContractRestrictionUser user2 = new RContractRestrictionUser();
        userList.add(user1);
        userList.add(user2);
        given(unicusUsrClient.getContractRestrictionUserList(variantList.get(0).getContractVariantId())).willReturn(userList);

        int result = underTest.contDriver(contractId);

        assertThat(result).isEqualTo(2);
    }

    private RSaleContract createContract(String productId) {
        RSaleContract contract = new RSaleContract();
        RContractVariant contractVariant = new RContractVariant();
        contractVariant.setProductId(productId);
        contract.withVariants(contractVariant);
        return contract;
    }

    private RSaleContract createContract(String productId, int statusId) {
        RSaleContract contract = createContract(productId);
        contract.setContractStatusTypeId(statusId);
        return contract;
    }

    private RSaleContract createContract(String productId, int statusId, int optionId) {
        RSaleContract contract = createContract(productId, statusId);
        contract.setContractOptionId(optionId);
        return contract;
    }
}