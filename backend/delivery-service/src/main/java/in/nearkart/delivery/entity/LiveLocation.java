package in.nearkart.delivery.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "live_locations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID partnerId;

    @Column(nullable = false)
    private UUID assignmentId;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private Double speedKmph;
    private Double headingDegrees;

    @CreationTimestamp
    private LocalDateTime recordedAt;
}
