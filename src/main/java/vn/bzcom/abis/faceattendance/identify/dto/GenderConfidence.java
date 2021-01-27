package vn.bzcom.abis.faceattendance.identify.dto;

import com.neurotec.biometrics.NLAttributes;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class GenderConfidence {

    private String gender;

    private int confidence;

    @Builder
    public GenderConfidence(NLAttributes nlAttributes) {
        this.gender = nlAttributes.getGender().name();
        this.confidence = Byte.toUnsignedInt(nlAttributes.getGenderConfidence());
    }
}
