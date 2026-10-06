package ru.alfastrah.site.avto.adapter.contract.signed.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.cxf.binding.soap.SoapFault;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Arrays;

import static org.apache.cxf.interceptor.Fault.FAULT_CODE_SERVER;

@Service
@Slf4j
public class CreateSoapFault {

    public SoapFault createSoapFault(Throwable exception, String message) {

        log.error("CreateFault catch exception {}", ExceptionUtils.getRootCauseMessage(exception));
        SoapFault soapFault = new SoapFault(message,
                FAULT_CODE_SERVER);
//        Element detailElement = soapFault.getOrCreateDetail();
//        Document doc = detailElement.getOwnerDocument();

//        Element exceptionElement = doc.createElement("stackTrace");
//        exceptionElement.setTextContent(Arrays.toString(exception.getStackTrace()));
//        detailElement.appendChild(exceptionElement);
        return soapFault;
    }

}