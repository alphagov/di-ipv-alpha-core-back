package uk.gov.di.ipv.core.back.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Profile {

    @JsonProperty("fullName")
    private List<String> fullName;

    @JsonProperty("dateOfBirth")
    private List<String> dateOfBirth;

    @JsonProperty("address")
    private List<String> address;

    @JsonProperty("phoneNumber")
    private List<String> phoneNumber;

    @JsonProperty("passportNumber")
    private List<String> passportNumber;
}
