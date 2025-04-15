# RDS Parameter Group
resource "aws_db_parameter_group" "app_db_param_group" {
  name        = "app-db-param-group-${var.profile}-${local.timestamp}"
  family      = var.db_parameter_group_family
  description = "Custom parameter group for ${var.db_parameter_group_family}"

  tags = merge(var.tags, {
    Name = "app-db-param-group-${var.profile}"
  })

  lifecycle {
    create_before_destroy = true
    # Add prevent_destroy to ensure the parameter group isn't deleted while in use
    prevent_destroy = false # Set to true if you want to prevent accidental deletion
  }
}

# RDS Subnet Group
resource "aws_db_subnet_group" "app_db_subnet_group" {
  name       = "app-db-subnet-group-${var.profile}-${local.timestamp}"
  subnet_ids = aws_subnet.private_subnet.*.id

  tags = merge(var.tags, {
    Name = "app-db-subnet-group-${var.profile}"
  })

  lifecycle {
    create_before_destroy = true
    # Add prevent_destroy to ensure the subnet group isn't deleted while in use
    prevent_destroy = false # Set to true if you want to prevent accidental deletion
  }
}