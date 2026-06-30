output "vpc_id" {
  description = "VPC ID"
  value       = aws_vpc.nearkart.id
}

output "public_subnet_ids" {
  description = "Public subnet IDs"
  value       = aws_subnet.public[*].id
}

output "private_subnet_ids" {
  description = "Private subnet IDs"
  value       = aws_subnet.private[*].id
}

output "rds_endpoint" {
  description = "RDS PostgreSQL endpoint"
  value       = aws_db_instance.nearkart.endpoint
  sensitive   = true
}

output "redis_endpoint" {
  description = "ElastiCache Redis primary endpoint"
  value       = aws_elasticache_cluster.nearkart.cache_nodes[0].address
}

output "eks_cluster_name" {
  description = "EKS cluster name"
  value       = aws_eks_cluster.nearkart.name
}

output "eks_cluster_endpoint" {
  description = "EKS API server endpoint"
  value       = aws_eks_cluster.nearkart.endpoint
}

output "s3_media_bucket" {
  description = "S3 bucket name for media files"
  value       = aws_s3_bucket.media.bucket
}

output "s3_kyc_bucket" {
  description = "S3 bucket name for KYC documents"
  value       = aws_s3_bucket.kyc.bucket
}

output "cloudfront_domain" {
  description = "CloudFront distribution domain name"
  value       = aws_cloudfront_distribution.nearkart.domain_name
}

output "route53_zone_id" {
  description = "Route 53 hosted zone ID"
  value       = aws_route53_zone.nearkart.zone_id
}
