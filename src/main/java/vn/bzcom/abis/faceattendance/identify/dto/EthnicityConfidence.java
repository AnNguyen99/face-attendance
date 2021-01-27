package vn.bzcom.abis.faceattendance.identify.dto;

import com.neurotec.biometrics.NLAttributes;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class EthnicityConfidence {

    private int asian;

    private int black;

    private int hispanic;

    private int indian;

    private int white;

//    @Builder
//    public EthnicityConfidence(byte asian, byte black, byte hispanic, byte indian, byte white) {
//        this.asian = byteToInt(asian);
//        this.black = byteToInt(black);
//        this.hispanic = byteToInt(hispanic);
//        this.indian = byteToInt(indian);
//        this.white = byteToInt(white);
//    }

    @Builder
    public EthnicityConfidence(NLAttributes nlAttributes) {
        this.asian = byteToInt(nlAttributes.getEthnicityAsianConfidence());
        this.black = byteToInt(nlAttributes.getEthnicityBlackConfidence());
        this.hispanic = byteToInt(nlAttributes.getEthnicityHispanicConfidence());
        this.indian = byteToInt(nlAttributes.getEthnicityIndianConfidence());
        this.white = byteToInt(nlAttributes.getEthnicityWhiteConfidence());
    }

    private int byteToInt(byte value) {
        return Byte.toUnsignedInt(value);
    }
}
