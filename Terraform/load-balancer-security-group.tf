# Load Balancer Security Group
resource "aws_security_group" "lb_security_group" {
  name        = "${var.lb_security_group_name}-${var.profile}"
  description = "Security group for application load balancer"
  vpc_id      = aws_vpc.main_vpc.id

  # HTTP access from anywhere
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # HTTPS access from anywhere
  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Allow all outbound traffic
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(var.tags, {
    Name = "${var.lb_security_group_name}-${var.profile}"
  })
}