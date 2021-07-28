package uk.gov.di.ipv.core.back.restapi.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;

import java.util.Map;

@Data
public class UserInfoDto {

    @JsonValue
    private final Map<String, Object> jsonAttributes;
}
