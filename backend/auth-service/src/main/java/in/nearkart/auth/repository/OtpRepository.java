package in.nearkart.auth.repository;

import in.nearkart.auth.entity.OtpPurpose;
import in.nearkart.auth.entity.OtpRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpRecord, Long> {

    Optional<OtpRecord> findTopByPhoneAndPurposeAndIsUsedFalseOrderByCreatedAtDesc(
            String phone, OtpPurpose purpose
    );

    @Modifying
    @Transactional
    @Query("UPDATE OtpRecord o SET o.isUsed = true WHERE o.phone = :phone AND o.purpose = :purpose")
    void invalidateAllOtps(String phone, OtpPurpose purpose);

    @Modifying
    @Transactional
    @Query("DELETE FROM OtpRecord o WHERE o.expiresAt < :now")
    void deleteExpiredOtps(LocalDateTime now);
}
