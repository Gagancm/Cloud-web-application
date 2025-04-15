# CloudWatch Log Group for Application Logs
resource "aws_cloudwatch_log_group" "webapp_log_group" {
  name              = "/csye6225/webapp-${formatdate("YYYYMMDDHHmmss", timestamp())}"
  retention_in_days = 14

  tags = merge(var.tags, {
    Name = "csye6225-webapp-logs"
  })
}

# Output the Log Group Name
output "log_group_name" {
  description = "Name of the CloudWatch Log Group"
  value       = aws_cloudwatch_log_group.webapp_log_group.name
}