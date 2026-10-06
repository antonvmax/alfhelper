package ru.alfastrah.site.avto.adapter.contract.signed.config;

import jakarta.xml.ws.Endpoint;
import org.apache.cxf.Bus;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.metrics.MetricsFeature;
import org.apache.cxf.metrics.MetricsProvider;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import ru.alfastrah.schemas.interplat4.send_contract_signed.GetAnyContractSignedPortType;
import ru.alfastrah.schemas.interplat4.send_contract_signed.GetContractSignedPortType;
import ru.alfastrah.schemas.interplat4.send_contract_signed.SendContractSignedPortType;

import javax.xml.namespace.QName;

@Configuration
@EnableWs
@ComponentScan
@ImportResource("classpath:META-INF/cxf/cxf.xml")
public class SoapAdapterConfiguration extends WsConfigurerAdapter {

    private static final String NAMESPACE_URI = "http://schemas.alfastrah.ru/interplat4/send-contract-signed";
    private final Bus cxfBus;

    public SoapAdapterConfiguration(Bus cxfBus) {
        this.cxfBus = cxfBus;
    }

    @Bean
    public ServletRegistrationBean<org.apache.cxf.transport.servlet.CXFServlet> cxfServlet() {
        org.apache.cxf.transport.servlet.CXFServlet cxfServlet = new org.apache.cxf.transport.servlet.CXFServlet();
        ServletRegistrationBean<org.apache.cxf.transport.servlet.CXFServlet> servletDef = new ServletRegistrationBean<>(cxfServlet, "/cxf/*");
        servletDef.setLoadOnStartup(1);
        return servletDef;
    }

    @Bean
    public Endpoint sendContractSigned(SendContractSignedPortType sendContractSignedPortType,
                                       MetricsProvider metricsProvider) {
        EndpointImpl endpoint = new EndpointImpl(cxfBus, sendContractSignedPortType);
        endpoint.getFeatures().add(new MetricsFeature(metricsProvider));
        endpoint.setAddress("/SendContractSigned");
        endpoint.setWsdlLocation("contract-signed/SendContractSigned.wsdl");
        endpoint.setServiceName(new QName(NAMESPACE_URI, "sendContractSignedService"));
        endpoint.publish();
        return endpoint;
    }

    @Bean
    public Endpoint getContractSigned(GetContractSignedPortType getContractSignedPortType,
                                      Interceptor<? extends Message> soapSecurityInterceptor,
                                      MetricsProvider metricsProvider) {
        EndpointImpl endpoint = new EndpointImpl(cxfBus, getContractSignedPortType);
        endpoint.getFeatures().add(new MetricsFeature(metricsProvider));
        endpoint.setAddress("/GetContractSigned");
        endpoint.setWsdlLocation("contract-signed/GetContractSigned.wsdl");
        endpoint.setServiceName(new QName(NAMESPACE_URI, "getContractSignedService"));
        endpoint.getInInterceptors().add(soapSecurityInterceptor);
        endpoint.publish();
        return endpoint;
    }

    @Bean
    public Endpoint getContractSignedLocal(GetContractSignedPortType getContractSignedPortType,
                                           MetricsProvider metricsProvider) {
        EndpointImpl endpoint = new EndpointImpl(cxfBus, getContractSignedPortType);
        endpoint.getFeatures().add(new MetricsFeature(metricsProvider));
        endpoint.setAddress("/GetContractSignedLocal");
        endpoint.setWsdlLocation("contract-signed/GetContractSigned.wsdl");
        endpoint.setServiceName(new QName(NAMESPACE_URI, "getContractSignedService"));
        endpoint.publish();
        return endpoint;
    }

    @Bean
    public Endpoint getAnyContractSignedLocal(GetAnyContractSignedPortType getAnyContractSignedPortType,
                                              MetricsProvider metricsProvider) {
        EndpointImpl endpoint = new EndpointImpl(cxfBus, getAnyContractSignedPortType);
        endpoint.getFeatures().add(new MetricsFeature(metricsProvider));
        endpoint.setAddress("/GetAnyContractSignedLocal");
        endpoint.setWsdlLocation("contract-signed/GetAnyContractSigned.wsdl");
        endpoint.setServiceName(new QName(NAMESPACE_URI, "getAnyContractSignedService"));
        endpoint.publish();
        return endpoint;
    }
}
