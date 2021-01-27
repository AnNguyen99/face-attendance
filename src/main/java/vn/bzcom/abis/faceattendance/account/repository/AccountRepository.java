package vn.bzcom.abis.faceattendance.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.bzcom.abis.faceattendance.account.entity.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Account findByUsername(String userId);

}
