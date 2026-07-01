package in.nearkart.delivery.repository;

import in.nearkart.delivery.entity.LiveLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LiveLocationRepository extends JpaRepository<LiveLocation, Long> {
    Optional<LiveLocation> findByPartnerId(Long partnerId);
}
