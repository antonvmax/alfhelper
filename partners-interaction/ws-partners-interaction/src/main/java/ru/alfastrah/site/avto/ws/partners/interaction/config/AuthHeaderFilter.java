package ru.alfastrah.site.avto.ws.partners.interaction.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.alfastrah.alfadigital.auth.oauth2.UserPrincipalAuthentication;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Фильтр, который читает заголовок {@code auth-context} и восстанавливает
 * {@link Authentication} в {@link SecurityContextHolder}.
 *
 * <p>Заголовок {@code auth-context} содержит base64-encoded JSON объекта
 * {@link UserPrincipalAuthentication} (класс из ws-auth-client-lib). Фильтр
 * декодирует его и кладёт в SecurityContext, чтобы логин пользователя был доступен
 * через {@code Authentication.getName()} для последующей обработки интерсептором
 * {@link JwtPrincipalInterceptor}.</p>
 */
public class AuthHeaderFilter implements Filter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        String header = ((HttpServletRequest) servletRequest).getHeader("auth-context");
        if (header != null) {
            byte[] decoded = Base64.getDecoder().decode(header);
            String json = new String(decoded, StandardCharsets.UTF_8);
            Authentication authentication = OBJECT_MAPPER.readValue(json, UserPrincipalAuthentication.class);
            if (authentication != null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
}