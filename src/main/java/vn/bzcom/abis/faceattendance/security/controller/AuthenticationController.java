package vn.bzcom.abis.faceattendance.security.controller;

import com.kbjung.abis.neurotec.biometrics.utils.NSubjectUtils;
import com.neurotec.biometrics.NMatchingResult;
import com.neurotec.biometrics.NSubject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import vn.bzcom.abis.faceattendance.account.entity.Account;
import vn.bzcom.abis.faceattendance.common.service.RecognitionService;
import vn.bzcom.abis.faceattendance.event.entity.MatchingResult;
import vn.bzcom.abis.faceattendance.security.jwt.JwtTokenProvider;
import vn.bzcom.abis.faceattendance.account.dto.AccountDto;
import vn.bzcom.abis.faceattendance.account.service.AccountService;
import vn.bzcom.abis.faceattendance.common.dto.ApiResponse;
import vn.bzcom.abis.faceattendance.common.dto.AuthToken;
import vn.bzcom.abis.faceattendance.subject.entity.BioTemplate;

import java.util.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/token")
@Slf4j
public class AuthenticationController {

    @Autowired
    private RecognitionService recognitionService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenUtil;

    @Autowired
    private AccountService accountService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/generate-token")
    public ApiResponse<AuthToken> logInAndCreateJwtToken(@RequestBody AccountDto.Login loginDto) throws AuthenticationException {
        log.debug("Start login with " + loginDto.getUsername() + ", " + loginDto.getPassword());
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getUsername(),
                        loginDto.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        final String token = jwtTokenUtil.generateToken(authentication);
        final AuthToken authToken = AuthToken.builder().token(token).build();

        return ApiResponse.<AuthToken>builder()
                .status(HttpStatus.OK.value())
                .message("Jwt token created for " + loginDto.getUsername())
                .result(authToken)
                .build();
    }

    @PostMapping("/generate-token-by-face")
    public ApiResponse<AuthToken> logInByFaceAndCreateJwtToken(@RequestBody AccountDto.LoginFace loginFaceDto) throws AuthenticationException {
        log.debug("Start login with " + loginFaceDto.getFaceImage().substring(0, 10) + ", " + loginFaceDto.getImageFormat());

        Map<Long, byte[]> templates = new HashMap<>();

        List<Account> accounts = accountService.findAll();
        accounts.forEach(account -> {
            templates.put(account.getId(), account.getSubject().getBioTemplate().getTemplate());
        });

//        List<NMatchingResult> matchingResults = recognitionService.identify(loginFaceDto.getFaceImage(),
//                                                                            loginFaceDto.getImageFormat(),
//                                                                            templates);
        NSubject subject = NSubjectUtils.createSubject(loginFaceDto.getFaceImage(), loginFaceDto.getImageFormat().name());
        List<NMatchingResult> matchingResults = recognitionService.identifyOnServer(subject);
        NMatchingResult matchingResult = matchingResults.get(0);
        System.out.println(matchingResult.toString());
        String matchedId = matchingResults.get(0).getId();
        Long accountId = Long.parseLong(matchedId.substring(matchingResults.get(0).getId().indexOf("_") + 1));

        Account account = accountService.findById(accountId);
        Set<GrantedAuthority> authorities = new HashSet<>();
        account.getAuthorities().forEach(authority -> {
            authorities.add(new GrantedAuthority() {
                @Override
                public String getAuthority() {
                    return authority.getRole().name();
                }
            });
        });


        final Authentication authentication = new UsernamePasswordAuthenticationToken(
                account.getUsername(),
                account.getPassword(),
                authorities
            );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        final String token = jwtTokenUtil.generateToken(authentication);
        final AuthToken authToken = AuthToken.builder().token(token).build();

        return ApiResponse.<AuthToken>builder()
            .status(HttpStatus.OK.value())
            .message("Jwt token created for " + account.getUsername())
            .result(authToken)
            .build();
    }

}
