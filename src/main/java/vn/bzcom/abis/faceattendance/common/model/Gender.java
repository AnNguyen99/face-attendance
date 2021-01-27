package vn.bzcom.abis.faceattendance.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Gender {
    @JsonProperty("UNKNOWN")
    UNKNOWN,
    @JsonProperty("MALE")
    MALE,
    @JsonProperty("FEMALE")
    FEMALE;
}
