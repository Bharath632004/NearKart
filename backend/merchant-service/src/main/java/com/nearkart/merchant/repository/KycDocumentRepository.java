package com.nearkart.merchant.repository;

import com.nearkart.merchant.entity.KycDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface KycDocumentRepository extends JpaRepository<KycDocument, UUID> {
    List<KycDocument> findByMerchantId(UUID merchantId);
    boolean existsByMerchantIdAndDocumentType(UUID merchantId, String documentType);
    long countByMerchantIdAndVerified(UUID merchantId, boolean verified);
}
