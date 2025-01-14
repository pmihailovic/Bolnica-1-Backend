package raf.bolnica1.patient.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import raf.bolnica1.patient.domain.Anamnesis;

@Repository
public interface AnamnesisRepository extends JpaRepository<Anamnesis, Long> {
}
