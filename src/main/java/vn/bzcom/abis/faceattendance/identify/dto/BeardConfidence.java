package vn.bzcom.abis.faceattendance.identify.dto;

import com.neurotec.biometrics.NLAttributes;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class BeardConfidence {

    private int beard;

    private int mustache;

    @Builder
    public BeardConfidence(NLAttributes nlAttributes) {
        this.beard = Byte.toUnsignedInt(nlAttributes.getBeardConfidence());
        this.mustache = Byte.toUnsignedInt(nlAttributes.getMustacheConfidence());
    }
}
