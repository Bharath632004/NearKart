package in.nearkart.payment.repository;

import in.nearkart.payment.entity.Payment;
import in.nearkart.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByOrderId(UUID orderId);
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);
    List<Payment> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
    List<Payment> findByStatus(PaymentStatus status);
    boolean existsByOrderIdAndStatus(UUID orderId, PaymentStatus status);
}
