package ru.alfastrah.site.avto.ws.partners.interaction.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * REST-интерсептор, аналогичный SOAP {@code HeaderInterceptor}.
 *
 * <p>Устанавливает логин пользователя в общий {@link ThreadLocal} {@code login},
 * из которого он далее читается как {@code callerCode}. Логин берётся из
 * {@link Authentication#getName()}, восстановленного в {@link SecurityContextHolder}
 * фильтром {@link AuthHeaderFilter} из заголовка {@code auth-context} (JWT).</p>
 */
public class JwtPrincipalInterceptor implements HandlerInterceptor {

    private final ThreadLocal<String> login;

    public JwtPrincipalInterceptor(ThreadLocal<String> login) {
        this.login = login;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        login.set(resolveLogin());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        login.remove();
    }

    private String resolveLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String name = authentication.getName();
        return StringUtils.hasText(name) ? name : null;
    }
}