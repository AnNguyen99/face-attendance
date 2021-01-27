package vn.bzcom.abis.faceattendance.subject.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.bzcom.abis.faceattendance.account.dto.AccountDto;
import vn.bzcom.abis.faceattendance.account.entity.Authority;
import vn.bzcom.abis.faceattendance.account.exception.UserIdDuplicationException;
import vn.bzcom.abis.faceattendance.account.repository.AuthorityRepository;
import vn.bzcom.abis.faceattendance.account.service.AccountService;
import vn.bzcom.abis.faceattendance.common.model.ImageInfo;
import vn.bzcom.abis.faceattendance.subject.dto.SubjectDto;
import vn.bzcom.abis.faceattendance.subject.entity.Subject;
import vn.bzcom.abis.faceattendance.subject.entity.SubjectImage;
import vn.bzcom.abis.faceattendance.subject.exception.SubjectNotFoundException;
import vn.bzcom.abis.faceattendance.subject.repository.SubjectRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service(value = "subjectService")
@Transactional
public class SubjectService {

    private final AuthorityRepository authorityRepository;
    private final SubjectRepository subjectRepository;
    private final AccountService accountService;

    public Subject create(AccountDto.SignUpReq dto, ImageInfo imageInfo, byte[] bioTemplate) {
        if (accountService.isExistedUserId(dto.getAuth().getUsername())) {
            throw new UserIdDuplicationException(dto.getAuth().getUsername());
        }
        Subject subject = dto.toSubjectEntity();
        subject.addSubjectImage(imageInfo);
        subject.addBioTemplate(bioTemplate);
        Set<Authority> authorities = new HashSet<>();
        dto.getAuth().getRoles().forEach(role -> {
            authorities.add(authorityRepository.findByRole(role));
        });
        subject.addAccount(dto.getAuth(), authorities);

        return subjectRepository.save(subject);
    }

    public Subject create(SubjectDto.CreateReq dto, ImageInfo imageInfo, byte[] bioTemplate) {

        Subject subject = dto.toEntity();
        subject.addSubjectImage(imageInfo);
        subject.addBioTemplate(bioTemplate);


        return subjectRepository.save(subject);
    }

    public Subject update(long id, SubjectDto.CreateReq dto, ImageInfo imageInfo, byte[] bioTemplate) {
        Subject subject = subjectRepository.findById(id)
                            .orElseThrow(() -> new SubjectNotFoundException(id));
        subject.updateSubject(dto.getBioGraphy(), dto.getContact());
        // update subjectImage or add new subjectImage
        int birthYear = dto.getBioGraphy().getBirthDate().getYear();
        subject.getBioTemplate()
                .updateBioTemplate(
                        bioTemplate,
                        dto.getBioGraphy().getGender(),
                        birthYear,
                        dto.getContact().getAddress().getProvince(),
                        dto.getContact().getAddress().getCountry(),
                        dto.getImage().getBioType());

        SubjectImage subjectImage = SubjectImage.builder()
                .imageInfo(imageInfo)
                .subject(subject)
                .build();

        subject.getSubjectImages().stream().forEach(subjectImage1 -> {
            subjectImage1.getImageInfo().setDisable();
        });

        subject.getSubjectImages().add(subjectImage);

        return subjectRepository.save(subject);
    }

    public Subject findById(long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("cannot found subject by given id: " + id));
        return subject;
    }

    public List<Subject> findAll() {
        return subjectRepository.findAll();
    }

    public Subject update(Subject subject) {
        return subjectRepository.save(subject);
    }

    public void delete(Subject subject) {
        subjectRepository.delete(subject);
    }

    public long count() {
        return subjectRepository.count();
    }
}
