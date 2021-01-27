package vn.bzcom.abis.faceattendance.event.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum DeviceType {
    @JsonProperty("CCTV")
    CCTV,
    @JsonProperty("SMARTPHONE")
    SMARTPHONE;
}
