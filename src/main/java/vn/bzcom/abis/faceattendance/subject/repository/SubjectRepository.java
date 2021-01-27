package vn.bzcom.abis.faceattendance.subject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.bzcom.abis.faceattendance.subject.entity.Subject;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    Optional<Subject> findByNid(String nid);

}
