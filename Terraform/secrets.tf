# Generate a random password for the RDS database
resource "random_password" "db_password" {
  length           = 16
  special          = true
  override_special = "!#$%&*()-_=+[]{}<>:?"
}

# Store the database credentials in AWS Secrets Manager
resource "aws_secretsmanager_secret" "db_credentials" {
  name        = "db-credentials-${var.profile}-${local.timestamp}"
  description = "RDS PostgreSQL Database Credentials"
  kms_key_id  = aws_kms_key.secrets_key.arn

  tags = merge(var.tags, {
    Name = "db-credentials-${var.profile}"
  })
}

# Store the database credentials as a JSON object
resource "aws_secretsmanager_secret_version" "db_credentials_version" {
  secret_id = aws_secretsmanager_secret.db_credentials.id
  secret_string = jsonencode({
    username = var.db_username
    password = random_password.db_password.result
    dbname   = var.db_name
    engine   = "postgres"
    port     = 5432
    host     = aws_db_instance.app_db.address
  })
}

# Output the secret ARN for reference
output "db_secret_arn" {
  description = "ARN of the database credentials secret"
  value       = aws_secretsmanager_secret.db_credentials.arn
  sensitive   = true
}