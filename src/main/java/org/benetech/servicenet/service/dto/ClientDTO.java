package org.benetech.servicenet.service.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * A DTO representing a client
 */
public class ClientDTO {

    @Getter
    @Setter
    @NotBlank
    @Size(min = 1, max = 50)
    private String clientId;

    @Getter
    @Setter
    private String clientSecret;

    @Getter
    @Setter
    @NotNull
    @Min(0)
    private Integer tokenValiditySeconds;

    public ClientDTO() {
        // Empty constructor needed for Jackson.
    }
}
