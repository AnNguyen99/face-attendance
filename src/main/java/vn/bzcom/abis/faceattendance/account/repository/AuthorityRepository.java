package vn.bzcom.abis.faceattendance.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.bzcom.abis.faceattendance.account.entity.Authority;
import vn.bzcom.abis.faceattendance.common.dto.Role;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {
  Authority findByRole(Role role);
}
