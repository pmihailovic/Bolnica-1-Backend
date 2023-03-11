package raf.bolnica1.patient.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import raf.bolnica1.patient.domain.MedicalRecord;


import java.util.List;

import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    Optional<List<MedicalRecord>> findByPatientLbp(String lbp);
    Optional<MedicalRecord> findByPatient_Lbp(String  lbp);
}