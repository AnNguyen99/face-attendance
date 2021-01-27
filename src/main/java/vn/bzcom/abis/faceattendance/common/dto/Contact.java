package vn.bzcom.abis.faceattendance.common.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.bzcom.abis.faceattendance.common.model.Address;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Contact {

    @NotEmpty
    private String email;

    @NotEmpty
    private String phoneNumber;

    @Valid
    private Address address;

    @Builder
    public Contact(String email, String phoneNumber, Address address) {
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }
}