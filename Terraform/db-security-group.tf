# Database Security Group
resource "aws_security_group" "db_security_group" {
  name        = "${var.db_security_group_name}-${var.profile}"
  description = "Security group for RDS database instances"
  vpc_id      = aws_vpc.main_vpc.id

  # PostgreSQL access from the application security group
  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.app_security_group.id]
  }

  # Allow all outbound traffic
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(var.tags, {
    Name = "${var.db_security_group_name}-${var.profile}"
  })
}