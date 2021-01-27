package vn.bzcom.abis.faceattendance.common.service.impl;

import com.kbjung.abis.neurotec.biometrics.utils.NImageUtils;
import com.kbjung.abis.neurotec.biometrics.utils.NSubjectUtils;
import com.neurotec.biometrics.*;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.biometrics.client.NClusterBiometricConnection;
import com.neurotec.images.NImage;
import com.neurotec.io.NBuffer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.bzcom.abis.faceattendance.common.model.ImageFormat;
import vn.bzcom.abis.faceattendance.common.service.RecognitionService;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class FaceRecognitionService implements RecognitionService {

    private final NBiometricClient biometricClient;

    @Override
    public NSubject extractTemplate(String encodedString, ImageFormat format) {
        if (encodedString == null) throw new NullPointerException("image is null");

        NBiometricTask task = null;

        NImage image = NImageUtils.base64StringToNImage(encodedString, format.name());

        NFace face = new NFace();
        face.setImage(image);
        NSubject faceSubject = new NSubject();
        faceSubject.getFaces().add(face);
        task = biometricClient.createTask(
                EnumSet.of(
                        NBiometricOperation.DETECT_SEGMENTS,
                        NBiometricOperation.ASSESS_QUALITY,
                        NBiometricOperation.CREATE_TEMPLATE),
                faceSubject);
        biometricClient.performTask(task);
//        NBiometricStatus status = biometricClient.createTemplate(faceSubject);
        if (task.getStatus() == NBiometricStatus.OK) {
            if (faceSubject.getFaces().size() > 1) {
                System.out.printf("Found %d faces\n", faceSubject.getFaces().size() - 1);
            }
            for (NFace nface : faceSubject.getFaces()) {
                for (NLAttributes attribute : nface.getObjects()) {
                    System.out.println("Face:");
                    System.out.format("\tLocation = (%d, %d), width = %d, height = %d\n", attribute.getBoundingRect().getBounds().x, attribute.getBoundingRect().getBounds().y,
                            attribute.getBoundingRect().width, attribute.getBoundingRect().height);

                    if ((attribute.getRightEyeCenter().confidence > 0) || (attribute.getLeftEyeCenter().confidence > 0)) {
                        System.out.println("\tFound eyes:");
                        if (attribute.getRightEyeCenter().confidence > 0) {
                            System.out.format("\t\tRight: location = (%d, %d), confidence = %d%n", attribute.getRightEyeCenter().x, attribute.getRightEyeCenter().y,
                                    attribute.getRightEyeCenter().confidence);
                        }
                        if (attribute.getLeftEyeCenter().confidence > 0) {
                            System.out.format("\t\tLeft: location = (%d, %d), confidence = %d%n", attribute.getLeftEyeCenter().x, attribute.getLeftEyeCenter().y,
                                    attribute.getLeftEyeCenter().confidence);
                        }
                        if (attribute.getNoseTip().confidence > 0) {
                            System.out.println("\tFound nose:");
                            System.out.format("\t\tLocation = (%d, %d), confidence = %d%n", attribute.getNoseTip().x, attribute.getNoseTip().y, attribute.getNoseTip().confidence);
                        }
                        if (attribute.getMouthCenter().confidence > 0) {
                            System.out.println("\tFound mouth:");
                            System.out.printf("\t\tLocation = (%d, %d), confidence = %d%n", attribute.getMouthCenter().x, attribute.getMouthCenter().y, attribute.getMouthCenter().confidence);
                        }
                    }
                }
            }
        }
        return faceSubject;
    }

    public int verify(byte[] refTemplate,  NImage candidateFaceImage) {
        NSubject referenceSubject = createSubject(refTemplate);
        NSubject candidateSubject = createSubject(candidateFaceImage);
        int score = 0;

        try {
            log.info("Matching Threshold for verification: " + biometricClient.getMatchingThreshold());
            NBiometricStatus status = biometricClient.verify(candidateSubject, referenceSubject);
            if (status == NBiometricStatus.OK || status == NBiometricStatus.MATCH_NOT_FOUND) {
                score = referenceSubject.getMatchingResults().get(0).getScore();

                log.info("Image scored {}, verification", score);
                if (status == NBiometricStatus.OK) {
                    log.info("succeed");
                } else {
                    log.info("failed");
                    score = -1;
                }
            } else {
                log.info("Verification failed. Status: %", status);
            }
        } catch (Throwable th) {
            th.printStackTrace();
        } finally {
            if (referenceSubject != null) referenceSubject.dispose();
            if (candidateSubject != null) candidateSubject.dispose();
        }
        return score;
    }

    @Override
    public NSubject verify(String probImage, ImageFormat format, byte[] candidateTemplate) {
        NSubject candidateSubject = createSubject(candidateTemplate);
        NSubject referenceSubject = createSubject(probImage, format);

        int score = 0;

        try {
            NBiometricStatus status = biometricClient.verify(referenceSubject, candidateSubject);
            if (status == NBiometricStatus.OK || status == NBiometricStatus.MATCH_NOT_FOUND) {
                for (NMatchingResult matchResult : referenceSubject.getMatchingResults()) {
                    System.out.format("Matched with ID: '%s' with score %d\n", matchResult.getId(), matchResult.getScore());
                    if (matchResult.getMatchingDetails() != null) {
                        System.out.println(matchingDetailsToString(matchResult.getMatchingDetails()));
                    }
                }
                score = referenceSubject.getMatchingResults().get(0).getScore();
                log.info("Image scored {}, verification", score);
                if (status == NBiometricStatus.OK) {
                    log.info("succeed");
                } else {
                    log.info("failed");
                    score = -1;
                }
            } else {
                log.info("Verification failed. Status: %", status);
            }
        } catch (Throwable th) {
            th.printStackTrace();
        } finally {
//            if (referenceSubject != null) referenceSubject.dispose();
            if (candidateSubject != null) candidateSubject.dispose();
        }
        return referenceSubject;
    }

    @Override
    public List<NMatchingResult> identify(String base64image, ImageFormat imageFormat, Map<Long, byte[]> templates) {
        NSubject probeSubject = null;
        NBiometricTask enrollTask = null;
        NBiometricStatus status = null;
        List<NMatchingResult> results = new ArrayList<>();

        try {
            probeSubject = createSubject(base64image, imageFormat);
            status = biometricClient.createTemplate(probeSubject);
            if (status != NBiometricStatus.OK) {
                System.out.format("Failed to create probe template. Status: %s.\n", status);
                System.exit(-1);
            }

            enrollTask = biometricClient.createTask(EnumSet.of(NBiometricOperation.ENROLL), null);
            for (Map.Entry<Long, byte[]> template : templates.entrySet()) {
                enrollTask.getSubjects().add(createSubject(template.getValue(), String.format("GallerySubject_%d", template.getKey())));
            }

            biometricClient.performTask(enrollTask);
            if (enrollTask.getStatus() != NBiometricStatus.OK) {
                System.out.format("Enrollment was unsuccessful. Status: %s.\n", enrollTask.getStatus());
                if (enrollTask.getError() != null) throw enrollTask.getError();
                System.exit(-1);
            }

            biometricClient.setMatchingThreshold(48);

            biometricClient.setFacesMatchingSpeed(NMatchingSpeed.LOW);

            status = biometricClient.identify(probeSubject);

            if (status == NBiometricStatus.OK) {
                for (NMatchingResult result : probeSubject.getMatchingResults()) {
                    System.out.format("Matched with ID: '%s' with score %d\n", result.getId(), result.getScore());
                    results.add(result);
                }
            } else if (status == NBiometricStatus.MATCH_NOT_FOUND) {
                System.out.format("Match not found");
            } else {
                System.out.format("Identification failed. Status: %s\n", status);
                System.exit(-1);
            }

        } catch (Throwable th) {

        } finally {
            if (enrollTask != null) enrollTask.dispose();
            if (probeSubject != null) probeSubject.dispose();
            if (biometricClient != null) biometricClient.dispose();
        }

        return results;
    }

    @Override
    public NSubject identify(NSubject probSubject, List<NSubject> referenceSubjects) {
        NBiometricTask enrollTask = null;
        NBiometricStatus status = null;
        List<NMatchingResult> results = new ArrayList<>();

        try {
            status = biometricClient.createTemplate(probSubject);
            if (status != NBiometricStatus.OK) {
                System.out.format("Failed to create probe template. Status: %s.\n", status);
                System.exit(-1);
            }

            enrollTask = biometricClient.createTask(EnumSet.of(NBiometricOperation.ENROLL), null);
            enrollTask.getSubjects().addAll(referenceSubjects);

            biometricClient.performTask(enrollTask);
            if (enrollTask.getStatus() != NBiometricStatus.OK) {
                System.out.format("Enrollment was unsuccessful. Status: %s.\n", enrollTask.getStatus());
                if (enrollTask.getError() != null) throw enrollTask.getError();
            }

            biometricClient.setMatchingThreshold(48);

            biometricClient.setFacesMatchingSpeed(NMatchingSpeed.LOW);

            status = biometricClient.identify(probSubject);

            if (status == NBiometricStatus.OK) {
                for (NMatchingResult result : probSubject.getMatchingResults()) {
                    System.out.format("Matched with ID: '%s' with score %d\n", result.getId(), result.getScore());
                    results.add(result);
                }
            } else if (status == NBiometricStatus.MATCH_NOT_FOUND) {
                System.out.format("Match not found");
            } else {
                System.out.format("Identification failed. Status: %s\n", status);
                System.exit(-1);
            }

        } catch (Throwable th) {
            th.printStackTrace();
        } finally {
            if (enrollTask != null) enrollTask.dispose();
        }

        return probSubject;
    }

    @Override
    public NBiometricStatus enrollOnServer(NSubject subject) {
        NBiometricStatus biometricStatus = null;
        NBiometricTask task = null;
        try {
            if (subject.getId() == null || subject.getId().isEmpty()) {
                log.error("Before enroll to server, subject should have a id.");
                return NBiometricStatus.SOURCE_ERROR;
            }

            if (!NSubjectUtils.isSubjectContainTemplate(subject)) {
                log.error("FaceSubject does not include Template!!!");
                return NBiometricStatus.SOURCE_ERROR;
            }
            log.debug("Starting Enroll to MMA.");
            NClusterBiometricConnection conn =
                (NClusterBiometricConnection) biometricClient.getRemoteConnections().get(0);
            log.debug("Cluster Host: " + conn.getHost());
            log.debug("Cluster Port: " + conn.getPort());

            task = biometricClient.createTask(
                EnumSet.of(NBiometricOperation.ENROLL),
                subject);
            biometricClient.performTask(task);

            if (task.getStatus() != NBiometricStatus.OK) {
                log.error(
                    "[id: " +
                        subject.getId() +
                        "] Enrollment was unsuccessful. Status: " +
                        task.getStatus());
                biometricStatus = task.getStatus();
            }
            log.debug("[id: " +
                subject.getId() +
                "] Enrollment was successful.");
            biometricStatus = task.getStatus();
        } catch (Exception ex) {
            log.error(ex.getMessage());
        } finally {
            if (task != null) task.dispose();
        }

        return biometricStatus;
    }

    public List<NMatchingResult> identifyOnServer(NSubject subject) {
        NBiometricTask task = null;
        List<NMatchingResult> matchingResults = null;
        try {
            if (!NSubjectUtils.isSubjectContainTemplate(subject)) {
                log.error("Subject does not include Template!!!");
                matchingResults = new ArrayList<>();
                return matchingResults;
            }

            if (subject.getQueryString() != null && !subject.getQueryString().isEmpty()) {
                log.debug("Query String : " + subject.getQueryString());
            }

            log.debug("Starting Indenfication on Finger MMA");
            NClusterBiometricConnection conn =
                (NClusterBiometricConnection) biometricClient.getRemoteConnections().get(0);
            log.info("Cluster Host: " + conn.getHost());
            log.info("Cluster Port: " + conn.getPort());

            task = biometricClient.createTask(
                EnumSet.of(NBiometricOperation.IDENTIFY),
                subject);
            biometricClient.performTask(task);

            if (task.getStatus() != NBiometricStatus.OK) {
                log.error(
                    "[id: " +
                        subject.getId() +
                        "] Identification was unsuccessful. Status: " +
                        task.getStatus());
            }
            matchingResults = subject.getMatchingResults();
            log.debug("[id: " +
                subject.getId() +
                "] Identification was successful.");
            for(NMatchingResult matchingResult : subject.getMatchingResults()) {
                log.debug("Matched with ID: [" + matchingResult.getId() + "], with score " + matchingResult.getScore());
            }
            return matchingResults;
        } catch (Exception ex) {
            log.error(ex.getMessage());
        } finally {
            if (task != null) task.dispose();
        }
        return matchingResults;
    }

    private NSubject createSubject(String probImage, ImageFormat format) {
        NSubject subject = extractTemplate(probImage, format);
        subject.setId("prob");
        return subject;
    }

    private NSubject createSubject(byte[] refTemplate) {
        NSubject subject = new NSubject();
        NTemplate template = new NTemplate(NBuffer.fromArray(refTemplate));
        if (template.getFaces() != null) {
            for(NLRecord record : template.getFaces().getRecords()) {
                log.info("Quality : " + record.getQuality());
            }
        }
        subject.setTemplate(template);
        subject.setId("candidate");
        return subject;
    }

    private NSubject createSubject(byte[] refTemplate, String subjectId) {
        NSubject subject = new NSubject();
        NTemplate template = new NTemplate(NBuffer.fromArray(refTemplate));
        if (template.getFaces() != null) {
            for(NLRecord record : template.getFaces().getRecords()) {
                log.info("Quality : " + record.getQuality());
            }
        }
        subject.setTemplate(template);
        subject.setId(subjectId);
        return subject;
    }

    private NSubject createSubject(NImage faceImage) {
        NSubject subject = new NSubject();
        NFace face = new NFace();
        face.setImage(faceImage);
        subject.getFaces().add(face);
        return subject;
    }

    private NSubject createSubject(String base64Image, String imageFormat) {
        NSubject subject = new NSubject();
        NImage faceImage = NImageUtils.base64StringToNImage(base64Image, imageFormat);
        NFace face = new NFace();
        face.setImage(faceImage);
        subject.getFaces().add(face);
        return subject;
    }

    private String matchingDetailsToString(NMatchingDetails details) {
        StringBuffer sb = new StringBuffer();
        if (details.getBiometricType().contains(NBiometricType.FINGER)) {
            sb.append("    Fingerprint match details: ");
            sb.append(String.format(" score = %d%n", details.getFingersScore()));
            for (NFMatchingDetails fngrDetails : details.getFingers()) {
                sb.append(String.format("    fingerprint index: %d; score: %d;%n", fngrDetails.getMatchedIndex(), fngrDetails.getScore()));
            }
        }

        if (details.getBiometricType().contains(NBiometricType.FACE)) {
            sb.append("    Face match details: ");
            sb.append(String.format(" score = %d%n", details.getFacesScore()));
            for (NLMatchingDetails faceDetails : details.getFaces()) {
                sb.append(String.format("    face index: %d; score: %d;%n", faceDetails.getMatchedIndex(), faceDetails.getScore()));
            }
        }

        if (details.getBiometricType().contains(NBiometricType.IRIS)) {
            sb.append("    Irises match details: ");
            sb.append(String.format(" score = %d%n", details.getIrisesScore()));
            for (NEMatchingDetails irisesDetails : details.getIrises()) {
                sb.append(String.format("    irises index: %d; score: %d;%n", irisesDetails.getMatchedIndex(), irisesDetails.getScore()));
            }
        }

        if (details.getBiometricType().contains(NBiometricType.PALM)) {
            sb.append("    Palmprint match details: ");
            sb.append(String.format(" score = %d%n", details.getPalmsScore()));
            for (NFMatchingDetails fngrDetails : details.getPalms()) {
                sb.append(String.format("    palmprint index: %d; score: %d;%n", fngrDetails.getMatchedIndex(), fngrDetails.getScore()));
            }
        }

        if (details.getBiometricType().contains(NBiometricType.VOICE)) {
            sb.append("    Voice match details: ");
            sb.append(String.format(" score = %d%n", details.getVoicesScore()));
            for (NSMatchingDetails voicesDetails : details.getVoices()) {
                sb.append(String.format("    voices index: %d; score: %d;%n", voicesDetails.getMatchedIndex(), voicesDetails.getScore()));
            }
        }
        return sb.toString();
    }

}
