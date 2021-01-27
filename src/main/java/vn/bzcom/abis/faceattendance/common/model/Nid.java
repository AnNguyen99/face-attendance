package vn.bzcom.abis.faceattendance.common.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.Size;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Nid {

    @Column(name = "nid", length = 12, nullable = false, unique = true)
    @Size(min = 9, max = 12)
    private String value;

    @Builder
    public Nid(String value) {
        this.value = value;
    }

    public void updateNid(String value) {
        this.value = value;
    }
}
