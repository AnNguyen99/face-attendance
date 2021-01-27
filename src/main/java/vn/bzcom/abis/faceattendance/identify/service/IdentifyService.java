package vn.bzcom.abis.faceattendance.identify.service;

import com.neurotec.biometrics.NMatchingResult;
import vn.bzcom.abis.faceattendance.common.model.ImageFormat;
import vn.bzcom.abis.faceattendance.common.model.ImageInfo;
import vn.bzcom.abis.faceattendance.identify.entity.Candidate;
import vn.bzcom.abis.faceattendance.identify.entity.CaseType;
import vn.bzcom.abis.faceattendance.identify.entity.Identify;
import vn.bzcom.abis.faceattendance.subject.entity.Subject;

import java.util.List;

public interface IdentifyService {

    public Identify createVerification(ImageFormat imageFormat, int imageQuality, byte[] prob, Subject subject);

    public Identify createVerification(ImageFormat imageFormat, int imageQuality, byte[] prob, Subject subject, int score);

    public Identify createIdentification(ImageFormat imageFormat, int imageQuality, byte[] prob, List<NMatchingResult> nMatchingResults);

    public Identify findOne(long id);

    public List<Identify> findAll();

    public List<Identify> findAllByCaseType(CaseType caseType);

    public Identify update(long id, Identify identify);

    public void delete(long id);
}
