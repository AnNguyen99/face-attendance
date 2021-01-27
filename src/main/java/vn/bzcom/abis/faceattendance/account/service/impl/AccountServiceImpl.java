package vn.bzcom.abis.faceattendance.account.service.impl;

import com.neurotec.biometrics.NSubject;
import com.neurotec.images.NImageFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.bzcom.abis.faceattendance.account.entity.Account;
import vn.bzcom.abis.faceattendance.account.dto.AccountDto;
import vn.bzcom.abis.faceattendance.account.exception.AccountNotFoundException;
import vn.bzcom.abis.faceattendance.common.dto.BioType;
import vn.bzcom.abis.faceattendance.common.model.ImageFormat;
import vn.bzcom.abis.faceattendance.common.model.ImageInfo;
import vn.bzcom.abis.faceattendance.account.repository.AccountRepository;
import vn.bzcom.abis.faceattendance.account.service.AccountService;
import vn.bzcom.abis.faceattendance.common.model.Pose;
import vn.bzcom.abis.faceattendance.common.service.RecognitionService;
import vn.bzcom.abis.faceattendance.subject.entity.SubjectImage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j

@Service(value = "accountService")
@Transactional
public class AccountServiceImpl implements AccountService, UserDetailsService {

    private final AccountRepository accountRepository;

    private final RecognitionService recognitionService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsername(username);
        log.info("loadByUsername(): account: " + account.toString());
        if (account == null) {
            throw new UsernameNotFoundException("Invalid username or password");
        }
        return new User(account.getUsername(), account.getPassword().getValue(), getAuthority(account));
    }

    private Set<SimpleGrantedAuthority> getAuthority(Account account) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        account.getAuthorities().forEach(authority -> {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + authority.getRole().toString()));
        });
        log.info("loaded roles from database: " + authorities.toString());
        return authorities;
    }

    @Override
    @Transactional(readOnly = true)
    public Account findById(long id) {
        Optional<Account> result = accountRepository.findById(id);
        if (!result.isPresent()) {
            throw new AccountNotFoundException(id);
        }
        return result.get();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Account getAccountByUsername(String username) {
        Account account = accountRepository.findByUsername(username);
        return account;
    }

    @Override
    public Account changePassword(long id, AccountDto.PasswordChangeReq dto) {
        Account account = findById(id);

        account.updatePassword(dto);
        account = accountRepository.save(account);

        return account;
    }

    @Override
    public Account updateAccount(long id, AccountDto.SignUpReq dto) throws IOException {
        Account account = findById(id);
        // update account info
        account.updateAccount(dto.getAuth());
        account.getSubject().updateSubject(dto.getBioGraphy(), dto.getContact());

        // ToDO: move to conroller followed codes which is not related to persistent loggic
        List<SubjectImage> subjectImages = account.getSubject().getSubjectImages();
        subjectImages = subjectImages.stream()
                .map(subjectImage -> {
                    subjectImage.getImageInfo().setDisable();
                    return subjectImage;
                })
                .collect(Collectors.toList());
        String base64Image = dto.getImage().getBase64Image();
        NSubject faceSubject = recognitionService.extractTemplate(base64Image, dto.getImage().getFormat());
        String imagePath = generateFaceImagePath();
        String filePath = "./uploads/" + imagePath;
        Path path = Paths.get(filePath);
        if (Files.notExists(path)) {
            Files.createDirectories(Paths.get(filePath));
        }
        String fileName = dto.getBioGraphy().getNid() + "_FACE.png";
        faceSubject.getFaces().get(1).getImage().save(filePath +fileName , NImageFormat.getPNG());
        int quality = (int) faceSubject.getFaces().get(1).getObjects().get(0).getQuality();
        byte[] faceTemplate = faceSubject.getTemplateBuffer().toByteArray();
        ImageInfo imageInfo = ImageInfo.builder()
                .imageFormat(ImageFormat.PNG)
                .imageUrl("/" + imagePath + fileName)
                .imageQuality(quality)
                .bioType(BioType.FACE)
                .pose(Pose.FACE_FRONT)
                .enabled(true)
                .build();
        //

        // add new subject image instead of updating existed subject image
        subjectImages.add(SubjectImage.builder()
                .imageInfo(imageInfo)
                .subject(account.getSubject())
                .build());

        account.getSubject().getBioTemplate()
                .updateBioTemplate(
                        faceTemplate,
                        dto.getBioGraphy().getGender(),
                        dto.getBioGraphy().getBirthDate().getYear(),
                        dto.getContact().getAddress().getProvince(),
                        dto.getContact().getAddress().getCountry(),
                        dto.getImage().getBioType());

        accountRepository.save(account);
        return account;
    }

    @Override
    public void deleteAccount(long id) {
        Account account = accountRepository.findById(id).get();
        accountRepository.delete(account);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isExistedUserId(String username) {
        return accountRepository.findByUsername(username) != null;
    }

    @Override
    public long count() {
        return accountRepository.count();
    }

    private String generateFaceImagePath() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd/HH/mm/");
        String imagePath = LocalDateTime.now().format(formatter);
        return imagePath;
    }

}
