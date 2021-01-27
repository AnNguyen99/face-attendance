package vn.bzcom.abis.faceattendance.subject.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.bzcom.abis.faceattendance.common.dto.BioType;
import vn.bzcom.abis.faceattendance.common.model.Gender;

import javax.persistence.*;

@Entity
@Table(name = "bio_template")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BioTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "template")
    private byte[] template;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    @Column(name = "birth_year")
    private int birthYear;

    @Column(name = "province", length = 50)
    private String province;

    @Column(name = "country", length = 50)
    private String country;

    @Enumerated
    @Column(name = "type", length = 10)
    private BioType bioType;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Builder
    public BioTemplate(byte[] template, Gender gender, int birthYear, String province, String country, BioType bioType, Subject subject) {
        this.template = template;
        this.gender = gender;
        this.birthYear = birthYear;
        this.province = province;
        this.country = country;
        this.bioType = bioType;
        this.subject = subject;
    }

    public void updateBioTemplate(byte[] template, Gender gender, int birthYear, String province, String country, BioType bioType) {
        this.template = template;
        this.gender = gender;
        this.birthYear = birthYear;
        this.province = province;
        this.country = country;
        this.bioType = bioType;
    }
}
