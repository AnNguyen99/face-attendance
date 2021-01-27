package vn.bzcom.abis.faceattendance.account.service;

import vn.bzcom.abis.faceattendance.account.dto.AccountDto;
import vn.bzcom.abis.faceattendance.account.entity.Account;

import java.io.IOException;
import java.util.List;

public interface AccountService {

    public Account findById(long id);

    public List<Account> findAll();

    public Account getAccountByUsername(String username);

    public Account changePassword(long id, AccountDto.PasswordChangeReq dto);

    public Account updateAccount(long id, AccountDto.SignUpReq dto) throws IOException;

    public void deleteAccount(long id);

    public boolean isExistedUserId(String username);

    public long count();
}
