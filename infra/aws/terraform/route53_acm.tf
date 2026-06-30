# ─── Route 53 Hosted Zone ────────────────────────────────────────────────────
resource "aws_route53_zone" "nearkart" {
  name = var.domain_name
  tags = { Name = "${var.project}-hosted-zone" }
}

# ─── ACM Certificate ─────────────────────────────────────────────────────────
# Note: CloudFront requires ACM certs in us-east-1
resource "aws_acm_certificate" "nearkart" {
  provider          = aws.us_east_1
  domain_name       = var.domain_name
  subject_alternative_names = ["*.${var.domain_name}"]
  validation_method = "DNS"
  lifecycle { create_before_destroy = true }
  tags = { Name = "${var.project}-cert" }
}

resource "aws_route53_record" "acm_validation" {
  for_each = {
    for dvo in aws_acm_certificate.nearkart.domain_validation_options : dvo.domain_name => {
      name   = dvo.resource_record_name
      type   = dvo.resource_record_type
      record = dvo.resource_record_value
    }
  }
  zone_id         = aws_route53_zone.nearkart.zone_id
  name            = each.value.name
  type            = each.value.type
  ttl             = 60
  records         = [each.value.record]
  allow_overwrite = true
}

resource "aws_acm_certificate_validation" "nearkart" {
  provider                = aws.us_east_1
  certificate_arn         = aws_acm_certificate.nearkart.arn
  validation_record_fqdns = [for record in aws_route53_record.acm_validation : record.fqdn]
}

# ─── DNS Records ─────────────────────────────────────────────────────────────
resource "aws_route53_record" "apex" {
  zone_id = aws_route53_zone.nearkart.zone_id
  name    = var.domain_name
  type    = "A"
  alias {
    name                   = aws_cloudfront_distribution.nearkart.domain_name
    zone_id                = aws_cloudfront_distribution.nearkart.hosted_zone_id
    evaluate_target_health = false
  }
}

resource "aws_route53_record" "www" {
  zone_id = aws_route53_zone.nearkart.zone_id
  name    = "www.${var.domain_name}"
  type    = "A"
  alias {
    name                   = aws_cloudfront_distribution.nearkart.domain_name
    zone_id                = aws_cloudfront_distribution.nearkart.hosted_zone_id
    evaluate_target_health = false
  }
}

# Secondary provider for ACM (must be us-east-1 for CloudFront)
provider "aws" {
  alias  = "us_east_1"
  region = "us-east-1"
}
