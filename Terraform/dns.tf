# DNS Configuration
variable "subdomain" {
  description = "Subdomain prefix to use (dev or demo)"
  type        = string
  default     = "dev"
}

variable "domain_name" {
  description = "Base domain name"
  type        = string
  default     = "overgeared.me"
}

# Data source to fetch the hosted zone for the subdomain
data "aws_route53_zone" "selected" {
  name         = "${var.subdomain}.${var.domain_name}."
  private_zone = false
}

# Route53 record that points to the load balancer
resource "aws_route53_record" "app_record" {
  zone_id = data.aws_route53_zone.selected.zone_id
  name    = "${var.subdomain}.${var.domain_name}"
  type    = "A"

  alias {
    name                   = aws_lb.app_load_balancer.dns_name
    zone_id                = aws_lb.app_load_balancer.zone_id
    evaluate_target_health = true
  }
}

# Output the DNS name of the load balancer
output "lb_dns_name" {
  description = "DNS name of the load balancer"
  value       = aws_lb.app_load_balancer.dns_name
}

# Output the subdomain value
output "subdomain" {
  description = "Subdomain prefix"
  value       = var.subdomain
}

# Output the domain name
output "domain_name" {
  description = "Base domain name"
  value       = var.domain_name
}

# Output the constructed FQDN
output "constructed_fqdn" {
  description = "Constructed FQDN (for reference)"
  value       = "${var.subdomain}.${var.domain_name}"
}

# Output the Route53 record name
output "route53_record" {
  description = "The Route53 record created for the application"
  value       = aws_route53_record.app_record.fqdn
}