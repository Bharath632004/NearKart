package in.nearkart.notification.service;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import in.nearkart.notification.config.TwilioConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    private final TwilioConfig twilioConfig;

    @Async("notificationExecutor")
    public void sendSms(String toPhoneNumber, String messageBody) {
        try {
            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(twilioConfig.getFromNumber()),
                    messageBody
            ).create();

            log.info("SMS sent: to={}, sid={}, status={}",
                    toPhoneNumber, message.getSid(), message.getStatus());
        } catch (Exception e) {
            log.error("SMS send failed to {}: {}", toPhoneNumber, e.getMessage());
        }
    }

    /**
     * Builds the standard NearKart SMS format.
     */
    public String buildOrderSms(String orderNumber, String status, String amount) {
        return String.format(
                "NearKart: Your order #%s is now %s. Amount: ₹%s. Track in app.",
                orderNumber, status, amount);
    }
}
