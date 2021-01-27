package vn.bzcom.abis.faceattendance.event.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@Embeddable
public class Location {

    @Column(name = "ip_address", length = 20)
    private String ipAddress;

    @Column(name = "longitude")
    private float longitude;

    @Column(name = "latitude")
    private float latitude;

    @Builder
    public Location(String ipAddress, float longitude, float latitude) {
        this.ipAddress = ipAddress;
        this.longitude = longitude;
        this.latitude = latitude;
    }
}
