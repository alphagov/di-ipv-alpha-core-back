package uk.gov.di.ipv.core.back.domain;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AttributeName {

    GIVEN_NAMES("givenNames"),
    SURNAME("surname"),
    DATE_OF_BIRTH("dob"),

    ADDRESS_LINE_1("addressLine1"),
    ADDRESS_LINE_2("addressLine2"),
    ADDRESS_TOWN("addressTown"),
    ADDRESS_COUNTY("addressCounty"),
    ADDRESS_POSTCODE("addressPostcode"),
    ADDRESS("address"),

    PHONE_NUMBER("phoneNumber"),
    PASSPORT_NUMBER("passportNumber");

    private final String name;

    AttributeName(final String name) {
        this.name = name;
    }

    @JsonValue
    @Override
    public String toString() {
        return name;
    }

    public static AttributeName fromString(String text) {
        for (AttributeName attributeName : AttributeName.values()) {
            if (attributeName.name.equals(text)) {
                return attributeName;
            }
        }
        return null;
    }
}
