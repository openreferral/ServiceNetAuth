package org.benetech.servicenet.service;

import io.github.jhipster.config.JHipsterProperties;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.benetech.servicenet.config.UaaProperties;
import org.benetech.servicenet.domain.Client;
import org.benetech.servicenet.repository.ClientRepository;
import org.benetech.servicenet.security.AuthoritiesConstants;
import org.benetech.servicenet.service.dto.ClientDTO;
import org.mapstruct.ap.internal.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class ClientService {

    private final Logger log = LoggerFactory.getLogger(ClientService.class);
    /**
     * Access tokens will not expire any earlier than this.
     */
    private static final int MIN_ACCESS_TOKEN_VALIDITY_SECS = 60;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private JHipsterProperties jHipsterProperties;

    @Autowired
    private UaaProperties uaaProperties;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ClientDTO createExternalClient(ClientDTO clientDTO) {
        if (StringUtils.isBlank(clientDTO.getClientSecret())) {
            throw new InvalidPasswordException();
        }
        int tokenValidity = Math.max(clientDTO.getTokenValiditySeconds(), MIN_ACCESS_TOKEN_VALIDITY_SECS);
        Client client = new Client(
            clientDTO.getClientId(),
            passwordEncoder.encode(clientDTO.getClientSecret()),
            "external",
            "client_credentials",
            true,
            tokenValidity,
            tokenValidity,
            AuthoritiesConstants.EXTERNAL
        );
        return toDto(clientRepository.save(client));
    }

    public Optional<ClientDTO> updateExternalClient(ClientDTO clientDTO) {
        Optional<Client> clientOptional = clientRepository.findById(clientDTO.getClientId());
        if (clientOptional.isPresent() && !getInitialClientIds().contains(clientDTO.getClientId())) {
            Client client = clientOptional.get();
            int tokenValidity = Math
                .max(clientDTO.getTokenValiditySeconds(), MIN_ACCESS_TOKEN_VALIDITY_SECS);
            if (StringUtils.isNotBlank(clientDTO.getClientSecret())) {
                client.setClientSecret(passwordEncoder.encode(clientDTO.getClientSecret()));
            }
            client.setAccessTokenValiditySeconds(tokenValidity);
            client.setRefreshTokenValiditySeconds(tokenValidity);
            return Optional.of(toDto(clientRepository.save(client)));
        }
        return Optional.empty();
    }

    public Optional<ClientDTO> findById(String id) {
        Optional<Client> client = clientRepository.findById(id);
        return client.map(this::toDto);
    }

    public Page<Client> getExternalClients(Pageable pageable) {
        return clientRepository.findAllByClientIdNotIn(getInitialClientIds(), pageable);
    }

    public void deleteExternalClient(String id) {
        if (!getInitialClientIds().contains(id)) {
            clientRepository.deleteById(id);
        }
    }

    public Set<String> getInitialClientIds() {
        return Collections.asSet(uaaProperties.getWebClientConfiguration().getClientId(),
            jHipsterProperties.getSecurity().getClientAuthorization().getClientId());
    }

    public void createInitialClients() {
        int accessTokenValidity = uaaProperties.getWebClientConfiguration().getAccessTokenValidityInSeconds();
        accessTokenValidity = Math.max(accessTokenValidity, MIN_ACCESS_TOKEN_VALIDITY_SECS);
        int refreshTokenValidity = uaaProperties.getWebClientConfiguration().getRefreshTokenValidityInSecondsForRememberMe();
        refreshTokenValidity = Math.max(refreshTokenValidity, accessTokenValidity);

        createOrUpdateClient(
            uaaProperties.getWebClientConfiguration().getClientId(),
            passwordEncoder.encode(uaaProperties.getWebClientConfiguration().getSecret()),
            "openid",
            "implicit,refresh_token,password,authorization_code",
            accessTokenValidity,
            refreshTokenValidity,
            null
        );
        createOrUpdateClient(
            jHipsterProperties.getSecurity().getClientAuthorization().getClientId(),
            passwordEncoder.encode(jHipsterProperties.getSecurity().getClientAuthorization().getClientSecret()),
            "web-app",
            "client_credentials",
            (int) jHipsterProperties.getSecurity().getAuthentication().getJwt().getTokenValidityInSeconds(),
            (int) jHipsterProperties.getSecurity().getAuthentication().getJwt().getTokenValidityInSecondsForRememberMe(),
            "ROLE_ADMIN"
        );
    }

    public ClientDTO toDto(Client client) {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setClientId(client.getClientId());
        clientDTO.setTokenValiditySeconds(client.getAccessTokenValiditySeconds());
        return clientDTO;
    }

    private void createOrUpdateClient(String clientId, String clientSecret, String scope,
        String authorizedGrantTypes,
        int accessTokenValidity, int refreshTokenValidity, String authorities) {
        Optional<Client> clientOptional = clientRepository.findById(clientId);
        Client client;
        if (clientOptional.isPresent()) {
            client = clientOptional.get();
        } else {
            client = new Client();
            client.setClientId(clientId);
        }
        client.setClientSecret(passwordEncoder.encode(clientSecret));
        client.setScope(scope);
        client.setAuthorizedGrantTypes(authorizedGrantTypes);
        client.setAutoApprove(Boolean.toString(true));
        client.setAccessTokenValiditySeconds(accessTokenValidity);
        client.setRefreshTokenValiditySeconds(refreshTokenValidity);
        client.setAuthorities(authorities);
        clientRepository.save(client);
    }
}
