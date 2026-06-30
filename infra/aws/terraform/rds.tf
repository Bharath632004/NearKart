resource "aws_db_subnet_group" "nearkart" {
  name       = "${var.project}-db-subnet-group"
  subnet_ids = aws_subnet.private[*].id
  tags       = { Name = "${var.project}-db-subnet-group" }
}

resource "aws_db_instance" "nearkart" {
  identifier        = "${var.project}-postgres-${var.environment}"
  engine            = "postgres"
  engine_version    = "15.4"
  instance_class    = var.db_instance_class
  allocated_storage = 20
  storage_type      = "gp3"
  storage_encrypted = true

  db_name  = var.db_name
  username = var.db_username
  password = var.db_password

  db_subnet_group_name   = aws_db_subnet_group.nearkart.name
  vpc_security_group_ids = [aws_security_group.rds.id]

  multi_az               = var.environment == "prod" ? true : false
  publicly_accessible    = false
  deletion_protection    = var.environment == "prod" ? true : false
  skip_final_snapshot    = var.environment == "prod" ? false : true
  final_snapshot_identifier = var.environment == "prod" ? "${var.project}-final-snapshot" : null

  backup_retention_period = 7
  backup_window           = "03:00-04:00"
  maintenance_window      = "Mon:04:00-Mon:05:00"

  tags = { Name = "${var.project}-rds" }
}
