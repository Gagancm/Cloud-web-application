# Application Load Balancer
resource "aws_lb" "app_load_balancer" {
  name               = "csye6225-lb-${var.profile}"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.lb_security_group.id]
  subnets            = aws_subnet.public_subnet.*.id

  enable_deletion_protection = false

  tags = merge(var.tags, {
    Name = "csye6225-lb-${var.profile}"
  })
}

# Target Group for Application
resource "aws_lb_target_group" "app_target_group" {
  name     = "csye6225-tg-${var.profile}"
  port     = var.app_port
  protocol = "HTTP"
  vpc_id   = aws_vpc.main_vpc.id

  health_check {
    enabled             = true
    path                = "/healthz"
    port                = "traffic-port"
    healthy_threshold   = 2
    unhealthy_threshold = 3
    timeout             = 10
    interval            = 30
    matcher             = "200"
  }

  tags = merge(var.tags, {
    Name = "csye6225-target-group-${var.profile}"
  })
}

# Listener for HTTP Port 80
resource "aws_lb_listener" "http_listener" {
  load_balancer_arn = aws_lb.app_load_balancer.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.app_target_group.arn
  }
}

# Add HTTPS Listener for Port 443 (conditional)
resource "aws_lb_listener" "https_listener" {
  count = var.certificate_arn != "" ? 1 : 0

  load_balancer_arn = aws_lb.app_load_balancer.arn
  port              = 443
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-2016-08"
  certificate_arn   = var.certificate_arn

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.app_target_group.arn
  }
}

# Output the load balancer DNS name
output "load_balancer_dns" {
  description = "The DNS name of the load balancer"
  value       = aws_lb.app_load_balancer.dns_name
}