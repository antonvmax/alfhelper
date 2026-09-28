package ru.alfastrah.site.avto.ws.contact.signed.logging;

import feign.Logger;
import feign.Request;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class NormalizedFeignLogger extends Logger {

    private final ThreadLocal<String> methodName = new ThreadLocal<>();

    private final ThreadLocal<Map<String, List<String>>> logsRequest = new ThreadLocal<>();

    private final ThreadLocal<Map<String, List<String>>> logsResponse = new ThreadLocal<>();

    private final ThreadLocal<Map<String, Boolean>> isResponse = new ThreadLocal<>();

    public NormalizedFeignLogger() {
    }

    protected void logRequest(String configKey, Level logLevel, Request request) {
        init();
        super.logRequest(configKey, logLevel, request);
    }

    protected void log(String configKey, String format, Object... args) {
        if (format.startsWith("--->") && !format.startsWith("---> END")) {
            clean(configKey);
        }

        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof String && ((String) args[i]).length() > 10000) {
                args[i] = "<deleted>";
            }
        }

        if (!isResponse.get().getOrDefault(configKey, false)) {
            log(logsRequest, configKey, format, args);
        } else {
            log(logsResponse, configKey, format, args);
            if (format.startsWith("<--- END")) {
                showLogs(configKey);
                dispose();

            }
        }

        if (format.startsWith("---> END")) {
            isResponse.get().put(configKey, true);
        }
    }

    private void init() {
        if (isResponse.get() == null) {
            isResponse.set(new ConcurrentHashMap<>());
        }
        if (methodName.get() == null) {
            methodName.set("");
        }
        if (logsRequest.get() == null) {
            logsRequest.set(new ConcurrentHashMap<>());
        }
        if (logsResponse.get() == null) {
            logsResponse.set(new ConcurrentHashMap<>());
        }
    }

    private void clean(String configKey) {
        isResponse.get().put(configKey, false);
        methodName.set(configKey);
        logsRequest.get().put(configKey, new ArrayList<>());
        logsResponse.get().put(configKey, new ArrayList<>());
    }

    private void log(ThreadLocal<Map<String, List<String>>> container, String configKey, String format, Object... args) {
        extractList(container, configKey).add(String.format(format, args));
    }

    private void showLogs(String configKey) {
        log.info("feign request " + methodName.get() + ": [\n" +
                collectionToDelimitedString(logsRequest.get().getOrDefault(configKey, Collections.emptyList())) +
                "\n] has response [\n" +
                collectionToDelimitedString(logsResponse.get().getOrDefault(configKey, Collections.emptyList())) +
                "\n]"
        );
    }

    private List<String> extractList(ThreadLocal<Map<String, List<String>>> container, String configKey) {
        return container.get().get(configKey);
    }

    private String collectionToDelimitedString(Collection<String> collection) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> iter = collection.iterator();

        for (int i = 0; iter.hasNext(); sb.append(iter.next())) {
            if (i++ > 0) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private void dispose() {
        isResponse.remove();
        methodName.remove();
        logsRequest.remove();
        logsResponse.remove();
    }
}
