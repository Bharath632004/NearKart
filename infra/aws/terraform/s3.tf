# ─── Media Bucket (product images, banners) ─────────────────────────────────
resource "aws_s3_bucket" "media" {
  bucket = "${var.project}-media-${var.environment}"
}

resource "aws_s3_bucket_versioning" "media" {
  bucket = aws_s3_bucket.media.id
  versioning_configuration { status = "Enabled" }
}

resource "aws_s3_bucket_public_access_block" "media" {
  bucket                  = aws_s3_bucket.media.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_cors_configuration" "media" {
  bucket = aws_s3_bucket.media.id
  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["GET", "PUT", "POST"]
    allowed_origins = ["https://${var.domain_name}"]
    max_age_seconds = 3000
  }
}

# ─── KYC Documents Bucket (sensitive, versioned, no public access) ───────────
resource "aws_s3_bucket" "kyc" {
  bucket = "${var.project}-kyc-docs-${var.environment}"
}

resource "aws_s3_bucket_versioning" "kyc" {
  bucket = aws_s3_bucket.kyc.id
  versioning_configuration { status = "Enabled" }
}

resource "aws_s3_bucket_public_access_block" "kyc" {
  bucket                  = aws_s3_bucket.kyc.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_server_side_encryption_configuration" "kyc" {
  bucket = aws_s3_bucket.kyc.id
  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "aws:kms"
    }
  }
}

# ─── Lifecycle: auto-delete incomplete multipart uploads ─────────────────────
resource "aws_s3_bucket_lifecycle_configuration" "media" {
  bucket = aws_s3_bucket.media.id
  rule {
    id     = "abort-incomplete-multipart"
    status = "Enabled"
    abort_incomplete_multipart_upload { days_after_initiation = 7 }
  }
}
