package com.nearkart.merchant.repository;

import com.nearkart.merchant.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, UUID> {
    List<Promotion> findByShopId(UUID shopId);

    @Query("SELECT p FROM Promotion p WHERE p.shop.id = :shopId AND p.active = true " +
            "AND p.startDate <= :now AND p.endDate >= :now")
    List<Promotion> findActivePromotionsByShop(@Param("shopId") UUID shopId,
                                                @Param("now") LocalDateTime now);

    Optional<Promotion> findByPromoCode(String promoCode);

    boolean existsByPromoCode(String promoCode);
}
