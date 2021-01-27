package vn.bzcom.abis.faceattendance.account.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import org.hibernate.validator.constraints.Length;
import vn.bzcom.abis.faceattendance.account.entity.Account;
import vn.bzcom.abis.faceattendance.common.dto.*;
import vn.bzcom.abis.faceattendance.common.model.*;
import vn.bzcom.abis.faceattendance.subject.entity.Subject;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class AccountDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class SignUpReq {

        @Valid
        private Auth auth;

        @Valid
        private BioGraphy bioGraphy;

        @Valid
        private Contact contact;

        @Valid
        private Image image;

        @Builder
        public SignUpReq(
                Auth auth,
                BioGraphy bioGraphy,
                Contact contact,
                Image image) {
            this.auth = auth;
            this.bioGraphy = bioGraphy;
            this.contact = contact;
            this.image = image;
        }

        public Subject toSubjectEntity() {

            Subject subject = Subject.builder()
                    .nid(buildNid())
                    .name(buildName())
                    .gender(this.bioGraphy.getGender())
                    .birthDate(this.bioGraphy.getBirthDate())
                    .email(Email.builder().address(this.contact.getEmail()).build())
                    .phoneNumber(this.contact.getPhoneNumber())
                    .address(buildAddress())
                    .build();

            return subject;
        }

        private Nid buildNid() {
            return Nid.builder()
                    .value(this.bioGraphy.getNid())
                    .build();
        }

        private Name buildName() {
            return Name.builder()
                    .firstName(this.bioGraphy.getFirstName())
                    .lastName(this.bioGraphy.getLastName())
                    .build();
        }

        private Address buildAddress() {
            return Address.builder()
                    .street(this.contact.getAddress().getStreet())
                    .city(this.contact.getAddress().getCity())
                    .province(this.contact.getAddress().getProvince())
                    .country(this.contact.getAddress().getCountry())
                    .zip(this.contact.getAddress().getZip())
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class SignUpRes {

        private long subjectId;

        private String username;

        private Set<Role> roles = new HashSet<>();

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-d")
        private LocalDateTime createdDate;

//        @Valid
//        private BioGraphy bioGraphy;
//
//        @Valid
//        private Contact contact;
//
//        @Valid
//        private Image image;

        @Builder
        public SignUpRes(Subject subject) {
            this.subjectId = subject.getId();
            this.username = subject.getAccount().getUsername();
            this.roles = subject.getAccount().getAuthorities().stream()
                    .map(authority -> {
                        Role role = authority.getRole();
                        return role;
                    })
                    .collect(Collectors.toSet());
            this.createdDate = subject.getAccount().getCreatedDate();
//            this.bioGraphy = BioGraphy.builder()
//                    .nid(subject.getNid().getValue())
//                    .gender(subject.getGender())
//                    .firstName(subject.getName().getFirstName())
//                    .lastName(subject.getName().getLastName())
//                    .birthDate(subject.getBirthDate())
//                    .build();
//            this.contact = Contact.builder()
//                    .email(subject.getEmail().getAddress())
//                    .phoneNumber(subject.getPhoneNumber())
//                    .address(subject.getAddress())
//                    .build();
//            this.image = getEnabledImage(subject);
        }

        private Image getEnabledImage(Subject subject) {
            Image image = subject.getSubjectImages().stream()
                    .map(subjectImage -> {
                        Image result = null;
                        if (subjectImage.getImageInfo().isEnabled()) {
                            result = Image.builder()
                                    .bioType(subjectImage.getImageInfo().getBioType())
                                    .format(subjectImage.getImageInfo().getFormat())
                                    .base64Image(subjectImage.getImageInfo().getImageByBase64())
                                    .quality(subjectImage.getImageInfo().getImageQuality())
                                    .build();
                        }
                        return result;
                    })
                    .findFirst().get();
            return image;
        }

    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Login {

        @NotEmpty
        @javax.validation.constraints.Email
        private String username;

        @NotEmpty
        @Length(min = 8)
        private String password;

        @Builder
        public Login(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class LoginFace {

        @NotEmpty
        private String faceImage;

        private ImageFormat imageFormat;


        @Builder
        public LoginFace(String faceImage, ImageFormat imageFormat) {
            this.faceImage = faceImage;
            this.imageFormat = imageFormat;
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class PasswordChangeReq {
        private String oldPassword;
        private String newPassword;

        @Builder
        public PasswordChangeReq(String oldPassword, String newPassword) {
            this.oldPassword = oldPassword;
            this.newPassword = newPassword;
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Res {

        private long accountId;

        private long subjectId;

        private LocalDateTime createdDate;

        private String username;

        private Set<Role> roles = new HashSet<>();

        private BioGraphy bioGraphy;

        private Contact contact;

        private Image image;

        public Res(Account account) {
            this.accountId = account.getId();
            this.subjectId = account.getSubject().getId();
            this.createdDate = account.getCreatedDate();
            this.username = account.getUsername();
            this.roles = account.getAuthorities().stream()
                        .map(authority -> { return authority.getRole(); })
                        .collect(Collectors.toSet());
            this.bioGraphy = BioGraphy.builder()
                    .nid(account.getSubject().getNid().getValue())
                    .firstName(account.getSubject().getName().getFirstName())
                    .lastName(account.getSubject().getName().getLastName())
                    .gender(account.getSubject().getGender())
                    .birthDate(account.getSubject().getBirthDate())
                    .build();
            this.contact = Contact.builder()
                    .email(account.getSubject().getEmail().getAddress())
                    .phoneNumber(account.getSubject().getPhoneNumber())
                    .address(account.getSubject().getAddress())
                    .build();
            this.image = account.getSubject().getEnabledImage();
        }
    }
}
