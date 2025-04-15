# RDS Instance with encryption
resource "aws_db_instance" "app_db" {
  identifier             = "${var.db_instance_identifier}-${var.profile}-${local.timestamp}"
  allocated_storage      = var.db_allocated_storage
  storage_type           = "gp2"
  engine                 = var.db_engine
  engine_version         = var.db_engine_version
  instance_class         = var.db_instance_class
  db_name                = var.db_name
  username               = var.db_username
  password               = random_password.db_password.result
  parameter_group_name   = aws_db_parameter_group.app_db_param_group.name
  db_subnet_group_name   = aws_db_subnet_group.app_db_subnet_group.name
  vpc_security_group_ids = [aws_security_group.db_security_group.id]

  # Enable storage encryption using AWS default key
  storage_encrypted = true
  # KMS key ID removed to use default AWS KMS key

  multi_az            = false
  publicly_accessible = false
  skip_final_snapshot = true

  # Added lifecycle configuration to prevent subnet and parameter group modifications
  lifecycle {
    ignore_changes = [
      db_subnet_group_name,
      parameter_group_name
    ]
    # Dependencies to ensure proper deletion order
    create_before_destroy = true
  }

  tags = merge(var.tags, {
    Name = "${var.db_instance_identifier}-${var.profile}"
  })
}

# Output the RDS endpoint
output "db_endpoint" {
  description = "The connection endpoint for the RDS instance"
  value       = aws_db_instance.app_db.endpoint
}