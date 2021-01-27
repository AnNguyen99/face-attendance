package vn.bzcom.abis.faceattendance.identify.dto;

import com.neurotec.biometrics.NLAttributes;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class GlassesAndHatConfidence {

    private int glasses;

    private int darkglasses;

    private int glassesReflection;

    private int hatConfidence;

    @Builder
    public GlassesAndHatConfidence(NLAttributes nlAttributes) {
        this.glasses = Byte.toUnsignedInt(nlAttributes.getGlassesConfidence());
        this.darkglasses = Byte.toUnsignedInt(nlAttributes.getDarkGlassesConfidence());
        this.glassesReflection = Byte.toUnsignedInt(nlAttributes.getGlassesReflectionConfidence());
        this.hatConfidence = Byte.toUnsignedInt(nlAttributes.getHatConfidence());
    }
}
