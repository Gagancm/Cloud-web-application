# IAM Role for EC2 instances with expanded CloudWatch permissions
resource "aws_iam_role" "ec2_cloudwatch_s3_role" {
  name = "ec2-cloudwatch-s3-role-${var.profile}-${local.timestamp}"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      },
    ]
  })

  tags = merge(var.tags, {
    Name = "ec2-cloudwatch-s3-role-${var.profile}"
  })
}

# IAM Policy for S3 access
resource "aws_iam_policy" "s3_access_policy" {
  name        = "s3-access-policy-${var.profile}-${local.timestamp}"
  description = "Policy allowing EC2 instances to access the S3 bucket"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = [
          "s3:PutObject",
          "s3:GetObject",
          "s3:DeleteObject",
          "s3:ListBucket"
        ]
        Effect = "Allow"
        Resource = [
          "${aws_s3_bucket.app_bucket.arn}",
          "${aws_s3_bucket.app_bucket.arn}/*"
        ]
      }
    ]
  })
}

# Enhanced CloudWatch IAM Policy
resource "aws_iam_policy" "cloudwatch_policy" {
  name        = "cloudwatch-metrics-logs-policy-${var.profile}-${local.timestamp}"
  description = "Policy allowing EC2 instances to publish CloudWatch logs and metrics"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = [
          "cloudwatch:PutMetricData",
          "cloudwatch:GetMetricStatistics",
          "cloudwatch:ListMetrics",
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents",
          "logs:DescribeLogStreams",
          "ec2:DescribeVolumes",
          "ec2:DescribeTags"
        ]
        Effect   = "Allow"
        Resource = "*"
      },
      {
        Action = [
          "ssm:GetParameter",
          "ssm:PutParameter"
        ]
        Effect   = "Allow"
        Resource = "arn:aws:ssm:*:*:parameter/AmazonCloudWatch-*"
      }
    ]
  })
}

# Secrets Manager IAM Policy
resource "aws_iam_policy" "secrets_manager_policy" {
  name        = "secrets-manager-policy-${var.profile}-${local.timestamp}"
  description = "Policy allowing EC2 instances to access secrets in Secrets Manager"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = [
          "secretsmanager:GetSecretValue",
          "secretsmanager:DescribeSecret"
        ]
        Effect   = "Allow"
        Resource = aws_secretsmanager_secret.db_credentials.arn
      }
    ]
  })
}

# KMS Access Policy
resource "aws_iam_policy" "kms_policy" {
  name        = "kms-access-policy-${var.profile}-${local.timestamp}"
  description = "Policy allowing access to KMS keys"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = [
          "kms:Encrypt",
          "kms:Decrypt",
          "kms:ReEncrypt*",
          "kms:GenerateDataKey*",
          "kms:DescribeKey"
        ]
        Effect   = "Allow"
        Resource = "*" # Allow access to all KMS keys for simplicity
      }
    ]
  })
}

# Data source for AWS Account ID
data "aws_caller_identity" "current" {}

# Attach the S3 access policy to the EC2 role
resource "aws_iam_role_policy_attachment" "s3_access_attachment" {
  role       = aws_iam_role.ec2_cloudwatch_s3_role.name
  policy_arn = aws_iam_policy.s3_access_policy.arn
}

# Attach the CloudWatch policy to the EC2 role
resource "aws_iam_role_policy_attachment" "cloudwatch_policy_attachment" {
  role       = aws_iam_role.ec2_cloudwatch_s3_role.name
  policy_arn = aws_iam_policy.cloudwatch_policy.arn
}

# Attach the Secrets Manager policy to the EC2 role
resource "aws_iam_role_policy_attachment" "secrets_manager_attachment" {
  role       = aws_iam_role.ec2_cloudwatch_s3_role.name
  policy_arn = aws_iam_policy.secrets_manager_policy.arn
}

# Attach the KMS policy to the EC2 role
resource "aws_iam_role_policy_attachment" "kms_attachment" {
  role       = aws_iam_role.ec2_cloudwatch_s3_role.name
  policy_arn = aws_iam_policy.kms_policy.arn
}

# Attach AWS managed CloudWatch Agent policy
resource "aws_iam_role_policy_attachment" "cloudwatch_agent_policy_attachment" {
  role       = aws_iam_role.ec2_cloudwatch_s3_role.name
  policy_arn = "arn:aws:iam::aws:policy/CloudWatchAgentServerPolicy"
}

# Create an instance profile for the EC2 instance
resource "aws_iam_instance_profile" "ec2_instance_profile" {
  name = "ec2-s3-cloudwatch-profile-${var.profile}-${local.timestamp}"
  role = aws_iam_role.ec2_cloudwatch_s3_role.name
}