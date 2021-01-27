package vn.bzcom.abis.faceattendance.identify.dto;

import com.kbjung.abis.neurotec.biometrics.utils.NImageUtils;
import com.neurotec.biometrics.NLAttributes;
import com.neurotec.biometrics.NMatchingResult;
import lombok.*;
import springfox.documentation.service.Contact;
import vn.bzcom.abis.faceattendance.common.dto.BioGraphy;
import vn.bzcom.abis.faceattendance.common.dto.BioType;
import vn.bzcom.abis.faceattendance.common.dto.Image;
import vn.bzcom.abis.faceattendance.common.model.ImageFormat;
import vn.bzcom.abis.faceattendance.common.model.Pose;
import vn.bzcom.abis.faceattendance.identify.entity.Candidate;
import vn.bzcom.abis.faceattendance.identify.entity.CaseType;
import vn.bzcom.abis.faceattendance.identify.entity.Identify;

import javax.swing.plaf.BorderUIResource;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.*;

public class IdentifyDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Req {

        @NotNull
        private String prob;

        @NotNull
        private ImageFormat imageFormat;

        @Builder
        public Req(String prob, ImageFormat imageFormat) {
            this.prob = prob;
            this.imageFormat = imageFormat;
        }

    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class VerificationRes {

        @NotNull
        private boolean matched;

        @NotNull
        private int score;

        @NonNull
        private ImageFormat imageFormat;

        private GenderConfidence genderConfidence;

        private EmotionConfidence emotionConfidence;

        private EthnicityConfidence ethnicityConfidence;

        private GlassesAndHatConfidence glassesAndHatConfidence;

        private BeardConfidence beardConfidence;

        @NotNull
        private String tokenImage;



        @Builder
        public VerificationRes(boolean matched, int score,
                               String tokenImage, ImageFormat imageFormat,
                               NLAttributes nlAttributes) {
            this.matched = matched;
            this.score = score;
            this.tokenImage = tokenImage;
            this.imageFormat = imageFormat;
            this.genderConfidence = GenderConfidence.builder().nlAttributes(nlAttributes).build();
            this.emotionConfidence = EmotionConfidence.builder().nlAttributes(nlAttributes).build();
            this.ethnicityConfidence = EthnicityConfidence.builder().nlAttributes(nlAttributes).build();
            this.glassesAndHatConfidence = GlassesAndHatConfidence.builder().nlAttributes(nlAttributes).build();
            this.beardConfidence = BeardConfidence.builder().nlAttributes(nlAttributes).build();
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class IdentificationRes {

        private Set<CandidateListItem> candidateList = new HashSet<>();

        @NonNull
        private ImageFormat imageFormat;

        private GenderConfidence genderConfidence;

        private EmotionConfidence emotionConfidence;

        private EthnicityConfidence ethnicityConfidence;

        private GlassesAndHatConfidence glassesAndHatConfidence;

        private BeardConfidence beardConfidence;

        @NotNull
        private String tokenImage;

        @Builder
        public IdentificationRes( Set<CandidateListItem> candiateList,
                                  String tokenImage, ImageFormat imageFormat,
                                  NLAttributes nlAttributes) {
            this.candidateList = candiateList;
            this.tokenImage = tokenImage;
            this.imageFormat = imageFormat;
            this.genderConfidence = GenderConfidence.builder().nlAttributes(nlAttributes).build();
            this.emotionConfidence = EmotionConfidence.builder().nlAttributes(nlAttributes).build();
            this.ethnicityConfidence = EthnicityConfidence.builder().nlAttributes(nlAttributes).build();
            this.glassesAndHatConfidence = GlassesAndHatConfidence.builder().nlAttributes(nlAttributes).build();
            this.beardConfidence = BeardConfidence.builder().nlAttributes(nlAttributes).build();
        }

    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Response {

        private long identifyId;

        private CaseType caseType;

        private LocalDateTime createdAt;

        private String createdBy;

        @Builder
        public Response(Identify identify) {
            this.identifyId = identify.getId();
            this.caseType = identify.getCaseType();
            this.createdAt = identify.getCreatedDate();
            this.createdBy = identify.getCreatedBy();
        }

    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ResponseDetail {
        private long identifyId;

        private CaseType caseType;

        private LocalDateTime createdAt;

        private String createdBy;

        private Image probImage;

        private List<CandidateDto> candidates = new ArrayList<>();

        @Builder
        public ResponseDetail(Identify identify) {
            this.identifyId = identify.getId();
            this.caseType = identify.getCaseType();
            this.createdAt = identify.getCreatedDate();
            this.createdBy = identify.getCreatedBy();

            this.probImage = Image.builder()
                    .base64Image(Base64.getEncoder().encodeToString(identify.getProb()))
                    .format(identify.getImageFormat())
                    .bioType(BioType.FACE)
                    .quality(identify.getImageQuality())
                    .pose(Pose.FACE_FRONT)
                    .build();

            for (Candidate candidate : identify.getCandidates()) {
                CandidateDto candidateDto = CandidateDto.builder()
                        .candidate(candidate)
                        .build();
                candidates.add(candidateDto);
            }


        }
    }

}
