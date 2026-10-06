package ru.alfastrah.site.avto.ws.partners.interaction.config;

import io.micrometer.core.annotation.Timed;
import jakarta.xml.ws.Endpoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.Bus;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.metrics.MetricsFeature;
import org.apache.cxf.metrics.MetricsProvider;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.security.SecurityContext;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import ru.alfastrah.interplat4.partners.interaction.PartnersPortType;
import ru.alfastrah.interplat4.partners.interaction.PayedContractRequest;
import ru.alfastrah.interplat4.partners.interaction.PayedContractResponse;
import ru.alfastrah.interplat4.partners.interaction.UPIDRequest;
import ru.alfastrah.interplat4.partners.interaction.UPIDResponse;
import ru.alfastrah.site.avto.model.partners.interaction.dto.LinkActionRequest;
import ru.alfastrah.site.avto.model.partners.interaction.dto.SearchByUpidAndContractIdRequest;
import ru.alfastrah.site.avto.ws.partners.interaction.service.LinkActionService;
import ru.alfastrah.site.avto.ws.partners.interaction.service.PayedContractProcessor;
import ru.alfastrah.site.avto.ws.partners.interaction.service.SearchByUpidAndContractService;
import ru.alfastrah.site.avto.ws.partners.interaction.service.UPIDProcessor;
import ru.alfastrah.site.avto.ws.partners.interaction.validator.RequestValidator;

import javax.xml.namespace.QName;
import java.security.Principal;
import java.util.Optional;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebServiceConfig {

    private static final String NAMESPACE_URI = "http://alfastrah.ru/interplat4/partners/interaction/";

    private final Bus cxfBus;

    @Bean
    public ThreadLocal<String> login() {
        return new ThreadLocal<>();
    }

    @Bean
    public ServletRegistrationBean<CXFServlet> cxfServlet() {
        CXFServlet cxfServlet = new CXFServlet();
        ServletRegistrationBean<CXFServlet> servletDef
                = new ServletRegistrationBean<>(cxfServlet, "/cxf/*");
        servletDef.setLoadOnStartup(1);
        return servletDef;
    }

    @Bean
    public Endpoint partnersInteractionWebService(PartnersPortType partnersPortType,
                                                  Interceptor<? extends Message> soapSecurityInterceptor,
                                                  MetricsProvider metricsProvider) {
        EndpointImpl endpoint = new EndpointImpl(cxfBus, partnersPortType);
        endpoint.setAddress("/PartnersInteraction");
        endpoint.getFeatures().add(new MetricsFeature(metricsProvider));
        endpoint.setServiceName(new QName(NAMESPACE_URI, "PartnersPortTypeService"));
        endpoint.setWsdlLocation("META-INF/wsdl/PartnersInteraction.wsdl");
        endpoint.getInInterceptors().add(soapSecurityInterceptor);
        endpoint.getInInterceptors().add(new HeaderInterceptor(login()));
        endpoint.getInInterceptors().add(new LoggingInInterceptor());
        endpoint.getInFaultInterceptors().add(new LoggingInInterceptor());
        endpoint.getOutInterceptors().add(new LoggingOutInterceptor());
        endpoint.getOutFaultInterceptors().add(new LoggingOutInterceptor());
        endpoint.publish();
        return endpoint;
    }

    @Bean
    public Endpoint partnersInteractionLocalWebService(PartnersPortType partnersPortType,
                                                       MetricsProvider metricsProvider) {
        EndpointImpl endpoint = new EndpointImpl(cxfBus, partnersPortType);
        endpoint.setAddress("/PartnersInteractionLocal");
        endpoint.getFeatures().add(new MetricsFeature(metricsProvider));
        endpoint.setServiceName(new QName(NAMESPACE_URI, "PartnersPortTypeService"));
        endpoint.setWsdlLocation("META-INF/wsdl/PartnersInteraction.wsdl");
        endpoint.getInInterceptors().add(new HeaderInterceptor(login()));
        endpoint.getInInterceptors().add(new LoggingInInterceptor());
        endpoint.getInFaultInterceptors().add(new LoggingInInterceptor());
        endpoint.getOutInterceptors().add(new LoggingOutInterceptor());
        endpoint.getOutFaultInterceptors().add(new LoggingOutInterceptor());
        endpoint.publish();
        return endpoint;
    }

    @Timed(value = "cxf_requests", histogram = true)
    @Component
    @RequiredArgsConstructor
    public static class PartnersInteraction implements PartnersPortType {

        private final ThreadLocal<String> login;
        private final RequestValidator requestValidator;
        private final UPIDProcessor upidProcessor;
        private final PayedContractProcessor payedContractProcessor;
        private final LinkActionService linkActionService;
        private final SearchByUpidAndContractService searchByUpidAndContractId;

        @Override
        public UPIDResponse getUPID(UPIDRequest parameters) {
            requestValidator.validate(parameters);
            return upidProcessor.getUPID(parameters, login.get());
        }

        public void linkActionToUpid(LinkActionRequest parameters) {
            requestValidator.validate(parameters);
            linkActionService.linkActionToUpid(parameters.getUpid(), parameters.getCalculationId(), parameters.getContractId());
        }

        @Override
        public PayedContractResponse getContractId(PayedContractRequest parameters) {
            requestValidator.validate(parameters);
            return payedContractProcessor.getContract(parameters);
        }

        public Long searchByUpidAndContractId(SearchByUpidAndContractIdRequest parameters) {
            requestValidator.validate(parameters);
            return searchByUpidAndContractId.searchByUpidAndContractId(parameters.getUpid(), parameters.getContractId());
        }
    }

    static class HeaderInterceptor extends AbstractSoapInterceptor {

        private final ThreadLocal<String> login;

        public HeaderInterceptor(ThreadLocal<String> login) {
            super(Phase.PRE_INVOKE);
            this.login = login;
        }

        @Override
        public void handleMessage(SoapMessage message) throws Fault {
            String userName = Optional.of(message)
                    .map(m -> m.get("org.apache.cxf.security.SecurityContext"))
                    .map(c -> (SecurityContext) c)
                    .map(SecurityContext::getUserPrincipal)
                    .map(Principal::getName)
                    .orElse("local");

            login.set(userName);
        }
    }
}
