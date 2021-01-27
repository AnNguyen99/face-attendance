package vn.bzcom.abis.faceattendance.common.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Auth {

    @NotEmpty
    @javax.validation.constraints.Email
    private String username;

    @NotEmpty
//    @Length(min = 8)
//    @JsonIgnore
    private String password;

    private Set<Role> roles = new HashSet<>();

    @Builder
    public Auth(String username, String password, Set<Role> roles) {
        this.username = username;
        this.password = password;
        this.roles = roles;
    }
}