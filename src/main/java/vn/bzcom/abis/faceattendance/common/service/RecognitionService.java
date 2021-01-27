package vn.bzcom.abis.faceattendance.common.service;

import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NMatchingResult;
import com.neurotec.biometrics.NSubject;
import com.neurotec.images.NImage;
import vn.bzcom.abis.faceattendance.common.model.ImageFormat;

import java.util.List;
import java.util.Map;

public interface RecognitionService {

    public NSubject extractTemplate(String encodedString, ImageFormat format);

    public int verify(byte[] refTemplate,  NImage candidateFaceImage);

    public NSubject verify(String probImage, ImageFormat format, byte[] candidateTemplate);

    public List<NMatchingResult> identify(String base64image, ImageFormat imageFormat, Map<Long, byte[]> templates);

    public NSubject identify(NSubject probSubject, List<NSubject> referenceSubjects);

    public NBiometricStatus enrollOnServer(NSubject subject);

    public List<NMatchingResult> identifyOnServer(NSubject subject);

}
