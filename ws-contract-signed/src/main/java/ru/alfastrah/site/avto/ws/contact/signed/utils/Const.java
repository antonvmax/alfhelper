package ru.alfastrah.site.avto.ws.contact.signed.utils;


public class Const {

    public static final int POLICY_PURCHASE_EVENT_ID = 1;
    public static final int SPECIAL_OFFER_EVENT_ID = 6;

    public static final String ATTACHMENT_FILENAME = "Полис.pdf";

    public static final Integer ALTCRAFT_DB_ID = 15;
    public static final Integer ALTCRAFT_RESOURCE_ID = 1;
    public static final Integer ALTCRAFT_STATUS = 0;
    public static final String ALTCRAFT_MATCHING = "custom";
    public static final String ALTCRAFT_FIELD_NAME = "IDAS";
    public static final String ALTCRAFT_CHANNEL = "email";
    public static final String ALTCRAFT_PRODUCT = "osago_thanks";
    public static final String UNICUS_SUBJECT = "UNICUS_";
    public static final String LK_PRODUCT = "lk_thanks";
    public static final Integer ALTCRAFT_PERSONAL_TRIGGER_ID = 1659;
    public static final String ALCRAFT_FORMAT_BASE64 = "data:application/pdf;base64,";

    public static final String PASSBOOK = "http://www.alfastrah.ru/online/passbook/email.php?contract_id=%s&key=%s";


    private Const() {
    }
}
