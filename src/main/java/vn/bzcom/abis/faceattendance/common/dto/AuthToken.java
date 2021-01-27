package vn.bzcom.abis.faceattendance.common.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthToken {

    private String token;

    @Builder
    public AuthToken(String token) {
        this.token = token;
    }

}
