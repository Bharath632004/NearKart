# ─── VPC ────────────────────────────────────────────────────────────────────
resource "aws_vpc" "nearkart" {
  cidr_block           = var.vpc_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = { Name = "${var.project}-vpc-${var.environment}" }
}

# ─── Subnets ────────────────────────────────────────────────────────────────
resource "aws_subnet" "public" {
  count                   = length(var.public_subnet_cidrs)
  vpc_id                  = aws_vpc.nearkart.id
  cidr_block              = var.public_subnet_cidrs[count.index]
  availability_zone       = var.availability_zones[count.index]
  map_public_ip_on_launch = true

  tags = { Name = "${var.project}-public-subnet-${count.index + 1}" }
}

resource "aws_subnet" "private" {
  count             = length(var.private_subnet_cidrs)
  vpc_id            = aws_vpc.nearkart.id
  cidr_block        = var.private_subnet_cidrs[count.index]
  availability_zone = var.availability_zones[count.index]

  tags = { Name = "${var.project}-private-subnet-${count.index + 1}" }
}

# ─── Internet Gateway ───────────────────────────────────────────────────────
resource "aws_internet_gateway" "nearkart" {
  vpc_id = aws_vpc.nearkart.id
  tags   = { Name = "${var.project}-igw" }
}

# ─── NAT Gateway ────────────────────────────────────────────────────────────
resource "aws_eip" "nat" {
  domain = "vpc"
  tags   = { Name = "${var.project}-nat-eip" }
}

resource "aws_nat_gateway" "nearkart" {
  allocation_id = aws_eip.nat.id
  subnet_id     = aws_subnet.public[0].id
  tags          = { Name = "${var.project}-nat-gw" }
  depends_on    = [aws_internet_gateway.nearkart]
}

# ─── Route Tables ───────────────────────────────────────────────────────────
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.nearkart.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.nearkart.id
  }
  tags = { Name = "${var.project}-public-rt" }
}

resource "aws_route_table" "private" {
  vpc_id = aws_vpc.nearkart.id
  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.nearkart.id
  }
  tags = { Name = "${var.project}-private-rt" }
}

resource "aws_route_table_association" "public" {
  count          = length(aws_subnet.public)
  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table_association" "private" {
  count          = length(aws_subnet.private)
  subnet_id      = aws_subnet.private[count.index].id
  route_table_id = aws_route_table.private.id
}

# ─── Security Groups ────────────────────────────────────────────────────────
resource "aws_security_group" "alb" {
  name        = "${var.project}-alb-sg"
  description = "ALB security group"
  vpc_id      = aws_vpc.nearkart.id

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "app" {
  name        = "${var.project}-app-sg"
  description = "App tier security group"
  vpc_id      = aws_vpc.nearkart.id

  ingress {
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "rds" {
  name        = "${var.project}-rds-sg"
  description = "RDS security group"
  vpc_id      = aws_vpc.nearkart.id

  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.app.id]
  }
}

resource "aws_security_group" "redis" {
  name        = "${var.project}-redis-sg"
  description = "Redis security group"
  vpc_id      = aws_vpc.nearkart.id

  ingress {
    from_port       = 6379
    to_port         = 6379
    protocol        = "tcp"
    security_groups = [aws_security_group.app.id]
  }
}
