package vn.bzcom.abis.faceattendance.account.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import vn.bzcom.abis.faceattendance.common.dto.Role;

import javax.persistence.*;

@Entity
@Table(name = "authority")
@Getter
@ToString
@NoArgsConstructor
public class Authority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder
    public Authority(Role role) {
        this.role = role;
    }

    public void updateRole(Role role) {
        this.role = role;
    }
}
