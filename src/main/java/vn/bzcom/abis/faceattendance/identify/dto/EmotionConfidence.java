package vn.bzcom.abis.faceattendance.identify.dto;

import com.neurotec.biometrics.NLAttributes;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class EmotionConfidence {

    private int anger;

    private int contempt;

    private int disgust;

    private int fear;

    private int happiness;

    private int neutral;

    private int sadness;

    private int suprise;

    @Builder
    public EmotionConfidence(NLAttributes nlAttributes) {
        this.anger = byteToInt(nlAttributes.getEmotionAngerConfidence());
        this.contempt = byteToInt(nlAttributes.getEmotionContemptConfidence());
        this.disgust = byteToInt(nlAttributes.getEmotionDisgustConfidence());
        this.fear = byteToInt(nlAttributes.getEmotionFearConfidence());
        this.happiness = byteToInt(nlAttributes.getEmotionHappinessConfidence());
        this.neutral = byteToInt(nlAttributes.getEmotionNeutralConfidence());
        this.sadness = byteToInt(nlAttributes.getEmotionSadnessConfidence());
        this.suprise = byteToInt(nlAttributes.getEmotionSurpriseConfidence());
    }

    private int byteToInt(byte value) {
        return Byte.toUnsignedInt(value);
    }
}
