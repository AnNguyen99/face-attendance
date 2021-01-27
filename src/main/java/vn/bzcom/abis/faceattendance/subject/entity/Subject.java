package vn.bzcom.abis.faceattendance.subject.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.bzcom.abis.faceattendance.account.entity.Authority;
import vn.bzcom.abis.faceattendance.common.dto.*;
import vn.bzcom.abis.faceattendance.common.model.*;
import vn.bzcom.abis.faceattendance.account.entity.Account;
import vn.bzcom.abis.faceattendance.subject.exception.EnabledSubjectImageNotFoundException;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "subject")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subject extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Nid nid;

    @Embedded
    private Name name;

    @Column(name = "gender", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name="birth_date", nullable = false)
    private LocalDate birthDate;

    @Embedded
    private Email email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Embedded
    private Address address;

    @OneToOne(mappedBy = "subject", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, optional = true)
    private Account account;

    @OneToOne(mappedBy = "subject", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    private BioTemplate bioTemplate;

    @OneToMany(mappedBy = "subject", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubjectImage> subjectImages = new ArrayList<>();

    @Builder
    public Subject(
            Nid nid,
            Name name,
            Gender gender,
            LocalDate birthDate,
            Email email,
            String phoneNumber,
            Address address
    ) {
        this.nid = nid;
        this.name = name;
        this.gender = gender;
        this.birthDate = birthDate;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    public void updateSubject(BioGraphy bioGraphy, Contact contact) {
        this.nid.updateNid(bioGraphy.getNid());
        this.name.updateName(bioGraphy.getFirstName(), bioGraphy.getLastName());
        this.gender = bioGraphy.getGender();
        this.birthDate = bioGraphy.getBirthDate();
        this.email.updateEmail(contact.getEmail());
        this.phoneNumber = contact.getPhoneNumber();
        this.address.updateAddress(contact.getAddress());
    }

    public Image getEnabledImage() {
        SubjectImage subjectImage = this.subjectImages.stream()
                .filter(subImage -> subImage.getImageInfo().isEnabled())
                .findFirst().orElseThrow(() -> new EnabledSubjectImageNotFoundException(this.getId()));

        Image image = Image.builder()
                    .base64Image(subjectImage.getImageInfo().getImageByBase64())
                    .format(subjectImage.getImageInfo().getFormat())
                    .bioType(subjectImage.getImageInfo().getBioType())
                    .quality(subjectImage.getImageInfo().getImageQuality())
                    .build();
        return image;
    }

    public void addSubjectImage(SubjectImage subjectImage) {
        this.subjectImages.add(subjectImage);
    }

    public void addSubjectImage(ImageInfo imageInfo) {
        this.subjectImages.add(buildSubjectImage(imageInfo));
    }

    public void addBioTemplate(byte[] bioTemplate) {
        this.bioTemplate = BioTemplate.builder()
                .template(bioTemplate)
                .gender(this.getGender())
                .province(this.address.getProvince())
                .country(this.address.getCountry())
                .subject(this)
                .build();
    }

    public void addAccount(Auth auth, Set<Authority> authorities) {
        this.account = Account.builder()
                .username(auth.getUsername())
                .password(Password.builder().value(auth.getPassword()).build())
                .subject(this)
                .authorities(authorities)
                .build();
    }

    private SubjectImage buildSubjectImage(ImageInfo imageInfo) {
        return SubjectImage.builder()
                .imageInfo(imageInfo)
                .subject(this)
                .build();
    }

    private SubjectImage buildSubjectImage(String imageUrl, int imageQuality, ImageFormat format, BioType bioType, Pose pose, boolean enabled) {
        return SubjectImage.builder()
                .imageInfo(buildImageInfo(imageUrl, imageQuality, format, bioType, pose, enabled))
                .subject(this)
                .build();
    }

    private ImageInfo buildImageInfo(String imageUrl, int imageQuality, ImageFormat format, BioType bioType, Pose pose, boolean enabled) {
        return ImageInfo.builder()
                .imageUrl(imageUrl)
                .imageQuality(imageQuality)
                .imageFormat(format)
                .bioType(bioType)
                .pose(pose)
                .enabled(enabled)
                .build();
    }

}
