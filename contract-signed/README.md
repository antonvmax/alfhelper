# contract-signed


[[_TOC_]]

## Конфигурация для Ops-ов

<mark>Перед тем как разворачивать приложение в новом контуре нужно в *logback-spring.xml* добавить описание профиля новой среды!</mark>

### Переменные окружения

Для настройки через пайплайн/helm

| Название | Описание | Примеры значений |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Название активного профиля | `test` |
| `JAVA_OPTS` | Конфигурация JVM | `"-XX:InitialRAMPercentage=50.0 -XX:MaxRAMPercentage=70.0"` |

### Properties

Для настройки через configserver/локальный properties-файл

| Название | Описание | Примеры значений |
|---|---|---|
| `spring.rabbitmq.host` | - | `localhost:1234,10.10.10.2:1234` |
| `server.port` | Конфигурация приложения | `8080` |
| `spring.application.name` | Конфигурация приложения | `contract-signed` |
| `management.endpoint.health.enabled` | Конфигурация актуатора| `true` |
| `eosago.ok.double.mail.list` | Список email'ов для дублирования писем | ` - ` |
| `notification.kasko.email.list` | Email для уведомительного пиьсма | `KatorginMI@alfastrah.ru` |
| `notification.general.email.list` | Email для уведомительного письма | `KatorginMI@alfastrah.ru` |
| `use.altkraft.for.osago` | Флаг использования altkraft для отправки писем осаго| `true` |
| `spring.activemq.broker-url` | Конфигурация activeMq | `tcp://interplat4site.vesta.ru:61112` |
| `spring.activemq.user` | Конфигурация activeMq | `admin` |
| `spring.activemq.password` | Конфигурация activeMq | `*****` |
| `spring.activemq.packages.trusted` | Конфигурация activeMq | `ru.alfastrah.schemas.interplat4.send_contract_signed,java.math,java.lang` |
| `spring.mail.host` | Конфигурация smtp | `hpmail.vesta.ru` |
| `spring.mail.port` | Конфигурация smtp | `587` |
| `spring.mail.username` | Конфигурация smtp | `epolicy` |
| `spring.mail.password` | Конфигурация smtp | `*****` |
| `spring.mail.properties.mail.smtp.auth` | Конфигурация smtp | `true` |
| `mail.sender` | Конфигурация smtp | `epolicy` |
| `oracle.datasource.jdbc-url` | Конфигурация oracelDb | `jdbc:oracle:thin:@p260unc4.vesta.ru:1566:uncunc` |
| `oracle.datasource.driver-class-name` | Конфигурация oracelDb | `oracle.jdbc.OracleDriver` |
| `oracle.datasource.username` | Конфигурация oracelDb | `INTERPLAT` |
| `oracle.datasource.password` | | `*****` |
| `oracle.datasource.poolName` | Конфигурация oracelDb | `"OracleDb Pool"` |
| `oracle.datasource.maximumPoolSize` | Конфигурация oracelDb | `20` |
| `oracle.datasource.wait.connect` | Конфигурация oracelDb | `1000` |
| `oracle.datasource.pool.checkout.time` | Конфигурация oracelDb | `6000000` |
| `oracle.datasource.autoCommit` | Конфигурация oracelDb | `false` |
| `postgresql.datasource.jdbc-url` | Конфигурация postrgeSQL | `jdbc:postgresql://10.96.4.120:5000/adsite` |
| `postgresql.datasource.driver-class-name` | Конфигурация postrgeSQL | `org.postgresql.Driver` |
| `postgresql.datasource.username` | Конфигурация postrgeSQL | `site` |
| `postgresql.datasource.password` | Конфигурация postrgeSQL | `*****` |
| `postgresql.datasource.pool-name` | Конфигурация postrgeSQL | `"PostgresDb Pool"` |
| `postgresql.datasource.minimumIdle` | Конфигурация postrgeSQL | `1` |
| `postgresql.datasource.maximumPoolSize` | Конфигурация postrgeSQL | `10` |
| `spring.mssql.url` | Конфигурация MSSQL | `jdbc:jtds:sqlserver://z14-3657-dbt.vesta.ru:1433/fuse_test` |
| `spring.mssql.username` | Конфигурация MSSQL | `bus` |
| `spring.mssql.password` | Конфигурация MSSQL | `*****` |
| `spring.mssql.driver` | Конфигурация MSSQL | `net.sourceforge.jtds.jdbc.Driver` |
| `spring.mssql.pool-name` | Конфигурация MSSQL | `"SQLServer Pool"` |
| `mybatis.mapper-locations` | Конфигурация MyBatis | `classpath*:/mybatis/*Mapper.xml` |
| `mybatis.type-handlers-package` | Конфигурация MyBatis  | `ru.alfastrah.ws.contact.signed.mappers.typehandlers` |
| `print-form.rest.service.url` | Адрес сервиса ms-report | `http://ms-adt-inner.vesta.ru/report` |
| `altcraft.rest.service.url` | Адрес сервиса altkraft | `https://akd.alfastrahmail.ru:7443` |
| `cblogger.rest.service.url` | Адрес сервиса CbLogs | `http://z14-1449-j8.vesta.ru:8181/cxf/logCBEosago` |
| `cblogger.rest.service.authorization` | Данные авторищации для CbLogs | `*****` |
| `osago.replace.rest.service.url` | Адрес сервиса подмены | `http://ms-adt-inner.vesta.ru/osago-replace/partner/info` |
| `loyalty.rest.service.url` | Адрес сервиса ms-loyalty | `http://ms-adt-inner.vesta.ru/loyalty` |
| `contract-signed.rest.service.url` | Адрес рест-утдпоинта contract-signed | `http://contract-signed.vesta.ru/` |
| `eosago.file` | Конфигурация ЭЦП | `Gorin_ver2.pfx` |
| `easago.pswd` | Конфигурация ЭЦП | `*****` |
| `easago.path` | Конфигурация ЭЦП | `../` |
| `unicus.service.url` | Кофигурация сервиса Unicus | `http://uniapp.vesta.ru:7788/uncunc` |
| `unicus.connection-timeout` | Кофигурация сервиса Unicus | `60000` |
| `unicus.read-timeout` | Кофигурация сервиса Unicus | `60000` |
| `bus.service.printedForms.url` | Адрес сервиса PrintedFormsLocal | `http://z14-1449-j8.vesta.ru:8181/cxf/PrintedFormsLocal` |
| `bus.service.asContracts.url` | Адрес сервиса AsContracts | `http://z14-1448-j8.vesta.ru:8181/cxf/AsContracts` |
| `logging.level.org.zalando.logbook` | Конфигурация логирования | `TRACE` |
| `logging.level.org.springframework.ws` | Конфигурация логирования | `TRACE` |
| `spring.cloud.compatibility-verifier.enabled` | Конфигурация sleuth| `false` |
