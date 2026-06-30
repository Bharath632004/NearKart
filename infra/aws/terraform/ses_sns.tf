# ─── SES Domain Identity ─────────────────────────────────────────────────────
resource "aws_ses_domain_identity" "nearkart" {
  domain = var.domain_name
}

resource "aws_route53_record" "ses_verification" {
  zone_id = aws_route53_zone.nearkart.zone_id
  name    = "_amazonses.${var.domain_name}"
  type    = "TXT"
  ttl     = 300
  records = [aws_ses_domain_identity.nearkart.verification_token]
}

resource "aws_ses_domain_dkim" "nearkart" {
  domain = aws_ses_domain_identity.nearkart.domain
}

resource "aws_route53_record" "ses_dkim" {
  count   = 3
  zone_id = aws_route53_zone.nearkart.zone_id
  name    = "${aws_ses_domain_dkim.nearkart.dkim_tokens[count.index]}._domainkey.${var.domain_name}"
  type    = "CNAME"
  ttl     = 300
  records = ["${aws_ses_domain_dkim.nearkart.dkim_tokens[count.index]}.dkim.amazonses.com"]
}

# ─── SNS Topics ──────────────────────────────────────────────────────────────
resource "aws_sns_topic" "order_notifications" {
  name = "${var.project}-order-notifications-${var.environment}"
  tags = { Name = "${var.project}-order-sns" }
}

resource "aws_sns_topic" "delivery_updates" {
  name = "${var.project}-delivery-updates-${var.environment}"
  tags = { Name = "${var.project}-delivery-sns" }
}

resource "aws_sns_topic" "alerts" {
  name = "${var.project}-system-alerts-${var.environment}"
  tags = { Name = "${var.project}-alerts-sns" }
}

# Email subscription for system alerts
resource "aws_sns_topic_subscription" "alert_email" {
  topic_arn = aws_sns_topic.alerts.arn
  protocol  = "email"
  endpoint  = var.alert_email
}
