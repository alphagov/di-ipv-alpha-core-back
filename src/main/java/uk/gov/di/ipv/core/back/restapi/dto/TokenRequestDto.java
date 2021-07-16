package uk.gov.di.ipv.core.back.restapi.dto;

import lombok.Data;

import java.net.URI;

@Data
public class TokenRequestDto {
    private String code;
    private URI redirect_uri;
    private String grant_type;
    private String client_id;
}
