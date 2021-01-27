package vn.bzcom.abis.faceattendance.account.entity;

import lombok.*;
import vn.bzcom.abis.faceattendance.account.dto.AccountDto;
import vn.bzcom.abis.faceattendance.common.dto.Auth;
import vn.bzcom.abis.faceattendance.common.dto.Role;
import vn.bzcom.abis.faceattendance.common.model.Auditable;
import vn.bzcom.abis.faceattendance.common.model.Password;
import vn.bzcom.abis.faceattendance.subject.entity.Subject;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "account")
@ToString
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Embedded
    private Password password;

    @Column(name = "enabled", columnDefinition = "boolean default true")
    private boolean enabled;

    @OneToOne( cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "account_authorities", joinColumns = {
            @JoinColumn(name = "account_id") }, inverseJoinColumns = {
            @JoinColumn(name = "authority_id") })
    private Set<Authority> authorities = new HashSet<>();

    @Builder
    public Account(
            String username,
            Password password, Subject subject, Set<Authority> authorities) {
        this.username = username;
        this.password = password;
        this.subject = subject;
        this.authorities = authorities;
    }

//    public void addRole(Role role) {
//        this.authorities.add(buildRole(role));
//    }
//
//
//    private Authority buildRole(Role role) {
//        return Authority.builder()
//                .role(role)
//                .build();
//    }

    public void updatePassword(AccountDto.PasswordChangeReq dto) {
        this.password.changePassword(dto.getNewPassword(), dto.getOldPassword());
    }

    public void updateAccount(Auth auth) {
        this.username = auth.getUsername();
        this.password.changePassword(auth.getPassword(), this.password.getValue());
    }
}
