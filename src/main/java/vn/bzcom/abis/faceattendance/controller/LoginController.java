package vn.bzcom.abis.faceattendance.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.bzcom.abis.faceattendance.account.entity.Account;
import vn.bzcom.abis.faceattendance.account.dto.AccountDto;
import vn.bzcom.abis.faceattendance.account.service.impl.AccountServiceImpl;
import vn.bzcom.abis.faceattendance.common.dto.ApiResponse;

import java.util.ResourceBundle;

@RequiredArgsConstructor
@Slf4j

@CrossOrigin(value = "*", maxAge = 300)
@RestController
public class LoginController {

    private final AccountServiceImpl accountServiceImpl;

    @Deprecated
    @PostMapping("/login")
    @ResponseStatus(value = HttpStatus.OK)
    public ApiResponse<Void> login(@RequestBody AccountDto.Login loginDto) {
        Account account = accountServiceImpl.getAccountByUsername(loginDto.getUsername());
        ResourceBundle resourceBundle;
        boolean matched = account.getPassword().isMatched(loginDto.getPassword());
        log.info("password is match: " + matched);
        return ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("login successed with username: " + loginDto.getUsername())
                .result(null)
                .build();
    }

}
