package org.benetech.servicenet.util;

import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class RequestUtils {

    public static Optional<HttpServletRequest> getCurrentHttpRequest() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
            .filter(requestAttributes -> ServletRequestAttributes.class.isAssignableFrom(requestAttributes.getClass()))
            .map(requestAttributes -> ((ServletRequestAttributes) requestAttributes))
            .map(ServletRequestAttributes::getRequest);
    }

    public static String getBaseUrl() {
        Optional<HttpServletRequest> requestOptional = getCurrentHttpRequest();
        if (requestOptional.isPresent()) {
            HttpServletRequest request = requestOptional.get();
            String proto = request.getHeader("x-forwarded-proto");
            if (StringUtils.isBlank(proto)) {
                proto = request.getScheme();
            }
            String host = request.getHeader("x-forwarded-host");
            if (StringUtils.isBlank(host)) {
                host = request.getRemoteHost();
            }
            return proto.split(",")[0] + "://" + host;
        }
        return "";
    }
}
