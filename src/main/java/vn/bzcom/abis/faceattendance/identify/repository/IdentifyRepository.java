package vn.bzcom.abis.faceattendance.identify.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.bzcom.abis.faceattendance.identify.entity.CaseType;
import vn.bzcom.abis.faceattendance.identify.entity.Identify;

import java.util.List;

public interface IdentifyRepository extends JpaRepository<Identify, Long> {

    List<Identify> findAllByCaseType(CaseType caseType);

}
