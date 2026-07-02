package com.nearkart.repository;

import com.nearkart.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BannerRepository extends JpaRepository<Banner, UUID> {

    @Query("""
            SELECT b FROM Banner b
            WHERE b.isActive = true
              AND b.placement = :placement
              AND b.validFrom <= :now
              AND (b.validUntil IS NULL OR b.validUntil >= :now)
            ORDER BY b.sortOrder
            """)
    List<Banner> findActiveBanners(
            @Param("placement") String placement,
            @Param("now")       LocalDateTime now);
}
