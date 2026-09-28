package ru.alfastrah.site.avto.ws.contact.signed.db;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MarketName {
    //здесь, не маркетинговое наименование , а доп. программа
    KASKO_500("6912349"),
    //здесь, не маркетинговое наименование , а доп. программа
    KASKO_1("6911969"),
    //здесь, не маркетинговое наименование , а доп. программа
    KASKO_3("6910670"),
    KASKO_5("6906189"),
    KASKO_10("6896806"),
    KASKOGO("6920069"),
    NS_ADDITIONAL_PROTECTION("6894507"),
    NS_CHILD_SPORT("6896007"),
    NS_A_SPORT("6930469"),
    NS_FAMILY_PROTECTION("6894686"),
    NS_NO_CORONAVIRUS("6912589"),
    NS_VIRUS_PROTECTION("6914029"),
    NS_ALFA_VACCINE("6916569"),
    PROPERTY_REPAIR("6901472"),
    // value_text из таблицы contract_add
    ALPHA_REPAIR_OFFLINE("6422368"),
    // value_text из таблицы contract_add
    ALFA_PROPERTY_ONOFF("6912249"),
    // value_text из таблицы contract_add
    ALFA_HOUSE("6890853"),
    // value_text из таблицы contract_add
    VZR_ON_OFF("1"),
    PROPERTY_CAR_OWNER("6907210"),
    UNKNOWN("-1");

    private final String code;

    public static MarketName fromCode(String code) {
        for (MarketName marketName : values()) {
            if (marketName.getCode().equals(code)) {
                return marketName;
            }
        }
        return UNKNOWN;
    }
}
