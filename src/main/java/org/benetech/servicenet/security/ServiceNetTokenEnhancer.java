package org.benetech.servicenet.security;

import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Adds "user_id" to the token so we can identify users in other microservices.
 * Adds the standard "iat" claim to tokens so we know when they have been created.
 * This is needed for a session timeout due to inactivity (ignored in case of "remember-me").
 */
@Component
public class ServiceNetTokenEnhancer implements TokenEnhancer {

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        addClaims((DefaultOAuth2AccessToken) accessToken, authentication.getPrincipal());
        return accessToken;
    }

    private void addClaims(DefaultOAuth2AccessToken accessToken, Object principal) {
        Map<String, Object> additionalInformation = accessToken.getAdditionalInformation();
        if (additionalInformation.isEmpty()) {
            additionalInformation = new LinkedHashMap<String, Object>();
        }
        if (principal instanceof CustomSpringSecurityUser) {
            additionalInformation.put("user_id", ((CustomSpringSecurityUser) principal).getId());
        }
        //add "iat" claim with current time in secs
        //this is used for an inactive session timeout
        additionalInformation.put("iat", new Integer((int)(System.currentTimeMillis()/1000L)));
        accessToken.setAdditionalInformation(additionalInformation);
    }
}
