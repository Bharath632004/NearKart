resource "aws_elasticache_subnet_group" "nearkart" {
  name       = "${var.project}-redis-subnet-group"
  subnet_ids = aws_subnet.private[*].id
}

resource "aws_elasticache_cluster" "nearkart" {
  cluster_id           = "${var.project}-redis-${var.environment}"
  engine               = "redis"
  node_type            = var.redis_node_type
  num_cache_nodes      = 1
  parameter_group_name = "default.redis7"
  engine_version       = "7.0"
  port                 = 6379

  subnet_group_name  = aws_elasticache_subnet_group.nearkart.name
  security_group_ids = [aws_security_group.redis.id]

  snapshot_retention_limit = 1
  snapshot_window          = "05:00-06:00"

  tags = { Name = "${var.project}-redis" }
}
