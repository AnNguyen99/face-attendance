package vn.bzcom.abis.faceattendance.common.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Email {

    @Column(name = "email", nullable = false, unique = true, length = 50)
    @javax.validation.constraints.Email(regexp = "^[a-z][a-z0-9_\\.]{5,32}@[a-z0-9]{2,}(\\.[a-z0-9]{2,4}){1,2}$")
    private String address;

    @Builder
    public Email(String address) {
        this.address = address;
    }

    public void updateEmail(String email) {
        this.address = email;
    }

}
