package vn.bzcom.abis.faceattendance.subject.dto;

import com.kbjung.abis.neurotec.biometrics.utils.NImageUtils;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.bzcom.abis.faceattendance.account.entity.Authority;
import vn.bzcom.abis.faceattendance.common.dto.Auth;
import vn.bzcom.abis.faceattendance.common.dto.BioGraphy;
import vn.bzcom.abis.faceattendance.common.dto.Contact;
import vn.bzcom.abis.faceattendance.common.dto.Image;
import vn.bzcom.abis.faceattendance.common.model.*;
import vn.bzcom.abis.faceattendance.subject.entity.Subject;
import vn.bzcom.abis.faceattendance.subject.entity.SubjectImage;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public class SubjectDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class CreateReq {

        @Valid
        private BioGraphy bioGraphy;

        @Valid
        private Contact contact;

        @Valid
        private Image image;

        @Builder
        public CreateReq(
                BioGraphy bioGraphy,
                Contact contact,
                Image image) {
            this.bioGraphy = bioGraphy;
            this.contact = contact;
            this.image = image;
        }

        public Subject toEntity() {
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//            Date bod= null;
//            try {
//                bod = sdf.parse(this.bioGraphy.getBirthDate());
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }

            Subject subject = Subject.builder()
                    .nid(Nid.builder().value(this.bioGraphy.getNid()).build())
                    .name(buildName())
                    .gender(this.bioGraphy.getGender())
                    .birthDate(this.bioGraphy.getBirthDate())
                    .email(Email.builder().address(this.contact.getEmail()).build())
                    .phoneNumber(this.contact.getPhoneNumber())
                    .address(buildAddress())
                    .build();

            return subject;
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
    public static class SubjectRes {
        private long subjectId;
        private LocalDateTime createdAt;
        private BioGraphy bioGraphy;
        private Contact contact;
        private Image image;

        @Builder
        public SubjectRes(Subject subject) {
//            // Date to String
//            LocalDate birthDate = subject.getBirthDate();
//            SimpleDateFormat transformat = new SimpleDateFormat("dd/MM/yyyy");
//            String bd = transformat.format(birthDate);

            this.subjectId = subject.getId();
            this.createdAt = subject.getCreatedDate();

            // build Biograpy
            this.bioGraphy = BioGraphy.builder()
                    .nid(subject.getNid().getValue())
                    .firstName(subject.getName().getFirstName())
                    .lastName(subject.getName().getLastName())
                    .gender(subject.getGender())
//                    .birthDate(bd)
                    .birthDate(subject.getBirthDate())  // Change Date to LocalDate
                    .build();

            // build Contact
            this.contact = Contact.builder()
                    .email(subject.getEmail().getAddress())
                    .phoneNumber(subject.getPhoneNumber())
                    .address(subject.getAddress())
                    .build();

            // return only one image which is enabled.
            List<SubjectImage> subjectImages = subject.getSubjectImages();
//            subjectImages.forEach((i) -> {
//                if (i.getImageInfo().isEnabled()) {
//                    String imageUrl = "./uploads" + i.getImageInfo().getImageUrl();
//                    this.faceImage = NImageUtils.imageFileToBase64String(imageUrl);
//                    this.faceImageQulaity = i.getImageInfo().getImageQuality();
//
//                }
//            });
            for(SubjectImage subjectImage : subject.getSubjectImages()) {
                if (subjectImage.getImageInfo().isEnabled()) {
                    String imageUrl = "./uploads" + subjectImage.getImageInfo().getImageUrl();
                    this.image = Image.builder()
                            .base64Image(NImageUtils.imageFileToBase64String(imageUrl))
                            .bioType(subjectImage.getImageInfo().getBioType())
                            .format(subjectImage.getImageInfo().getFormat())
                            .quality(subjectImage.getImageInfo().getImageQuality())
                            .build();
                }
            }

        }
    }
}
