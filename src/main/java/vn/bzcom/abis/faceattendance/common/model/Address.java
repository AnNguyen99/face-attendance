package vn.bzcom.abis.faceattendance.common.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    @Column(name = "street", length = 100)
    private String street;

    @Column(name = "city", length = 50)
    private String city;

    @Column(name = "district", length = 50)
    private String district;

    @Column(name = "province", length = 50)
    private String province;

    @NotEmpty
    @Column(name = "country", length = 50)
    private String country;

    @NotEmpty
    @Column(name = "zip", length = 10)
    private String zip;

    @Builder
    public Address(String street, String city, String district, String province, String country, String zip) {
        this.street = street;
        this.city = city;
        this.district = district;
        this.province = province;
        this.country = country;
        this.zip = zip;
    }

    public void updateAddress(Address address) {
        this.street = address.getStreet();
        this.city = address.getCity();
        this.district = address.getDistrict();
        this.province = address.getProvince();
        this.country = address.getCountry();
        this.zip = address.getZip();
    }
}
