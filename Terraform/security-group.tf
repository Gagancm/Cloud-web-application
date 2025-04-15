# Application Security Group
resource "aws_security_group" "app_security_group" {
  name        = "${var.app_security_group_name}-${var.profile}"
  description = "Security group for web application EC2 instances"
  vpc_id      = aws_vpc.main_vpc.id

  # SSH access from anywhere (for admin purposes)
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Application port access
  ingress {
    from_port       = var.app_port
    to_port         = var.app_port
    protocol        = "tcp"
    security_groups = [aws_security_group.lb_security_group.id]
  }

  # Allow all outbound traffic
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(var.tags, {
    Name = "${var.app_security_group_name}-${var.profile}"
  })
}