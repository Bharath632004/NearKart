# ─── CloudWatch Log Groups ───────────────────────────────────────────────────
resource "aws_cloudwatch_log_group" "backend" {
  name              = "/nearkart/${var.environment}/backend"
  retention_in_days = 30
}

resource "aws_cloudwatch_log_group" "eks" {
  name              = "/nearkart/${var.environment}/eks"
  retention_in_days = 14
}

# ─── RDS CPU Alarm ───────────────────────────────────────────────────────────
resource "aws_cloudwatch_metric_alarm" "rds_cpu" {
  alarm_name          = "${var.project}-rds-high-cpu"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "CPUUtilization"
  namespace           = "AWS/RDS"
  period              = 300
  statistic           = "Average"
  threshold           = 80
  alarm_description   = "RDS CPU utilization > 80%"
  alarm_actions       = [aws_sns_topic.alerts.arn]

  dimensions = {
    DBInstanceIdentifier = aws_db_instance.nearkart.identifier
  }
}

# ─── ElastiCache CPU Alarm ───────────────────────────────────────────────────
resource "aws_cloudwatch_metric_alarm" "redis_cpu" {
  alarm_name          = "${var.project}-redis-high-cpu"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "CPUUtilization"
  namespace           = "AWS/ElastiCache"
  period              = 300
  statistic           = "Average"
  threshold           = 75
  alarm_description   = "Redis CPU utilization > 75%"
  alarm_actions       = [aws_sns_topic.alerts.arn]

  dimensions = {
    CacheClusterId = aws_elasticache_cluster.nearkart.cluster_id
  }
}

# ─── CloudWatch Dashboard ────────────────────────────────────────────────────
resource "aws_cloudwatch_dashboard" "nearkart" {
  dashboard_name = "${var.project}-${var.environment}"
  dashboard_body = jsonencode({
    widgets = [
      {
        type = "metric"
        properties = {
          title  = "RDS CPU Utilization"
          metrics = [["AWS/RDS", "CPUUtilization", "DBInstanceIdentifier", aws_db_instance.nearkart.identifier]]
          period = 300
          stat   = "Average"
        }
      },
      {
        type = "metric"
        properties = {
          title  = "Redis CPU Utilization"
          metrics = [["AWS/ElastiCache", "CPUUtilization", "CacheClusterId", aws_elasticache_cluster.nearkart.cluster_id]]
          period = 300
          stat   = "Average"
        }
      }
    ]
  })
}
