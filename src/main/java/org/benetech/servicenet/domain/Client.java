package org.benetech.servicenet.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "oauth_client_details")
public class Client {

    @Id
    @Getter
    @Setter
    @Column(name = "client_id")
    private String clientId;

    @Column(name = "client_secret")
    @Getter
    @Setter
    private String clientSecret;

    @Column(name = "scope")
    @Getter
    @Setter
    private String scope;

    @Column(name = "resource_ids")
    @Getter
    @Setter
    private String resourceIds;

    @Column(name = "authorized_grant_types")
    @Getter
    @Setter
    private String authorizedGrantTypes;

    @Column(name = "autoapprove")
    @Getter
    @Setter
    private String autoApprove;

    @Column(name = "authorities")
    @Getter
    @Setter
    private String authorities;

    @Column(name = "access_token_validity")
    @Getter
    @Setter
    private Integer accessTokenValiditySeconds;

    @Column(name = "refresh_token_validity")
    @Getter
    @Setter
    private Integer refreshTokenValiditySeconds;

    @Column(name = "additional_information")
    @Getter
    @Setter
    private String additionalInformation;

    @Column(name = "web_server_redirect_uri")
    @Getter
    @Setter
    private String webServerRedirectUri;

    public Client() { }

    /**
     * Creates new instance of {@link Client}.
     */
    public Client(String clientId, String clientSecret, String scope,
        String authorizedGrantTypes, Boolean autoApprove, int accessTokenValiditySeconds,
        int refreshTokenValiditySeconds, String authorities) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
        this.authorizedGrantTypes = authorizedGrantTypes;
        this.autoApprove = Boolean.toString(autoApprove);
        this.authorities = authorities;
        this.accessTokenValiditySeconds = accessTokenValiditySeconds;
        this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
    }
}
