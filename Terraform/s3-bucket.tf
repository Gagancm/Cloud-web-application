# S3 Bucket with UUID name and KMS encryption
resource "random_uuid" "bucket_uuid" {}

# Enable KMS encryption for the S3 bucket
resource "aws_s3_bucket_server_side_encryption_configuration" "bucket_encryption" {
  bucket = aws_s3_bucket.app_bucket.id

  rule {
    apply_server_side_encryption_by_default {
      kms_master_key_id = aws_kms_key.s3_key.arn
      sse_algorithm     = "aws:kms"
    }
  }
}

# Make the bucket private
resource "aws_s3_bucket_public_access_block" "bucket_public_access_block" {
  bucket = aws_s3_bucket.app_bucket.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# Lifecycle policy for the bucket
resource "aws_s3_bucket_lifecycle_configuration" "bucket_lifecycle" {
  bucket = aws_s3_bucket.app_bucket.id

  rule {
    id     = "transition-to-standard-ia"
    status = "Enabled"

    # Fix for the empty filter warning
    filter {
      prefix = "" # This applies to all objects but fixes the warning
    }

    transition {
      days          = 30
      storage_class = "STANDARD_IA"
    }
  }
}

resource "aws_s3_bucket" "app_bucket" {
  bucket        = random_uuid.bucket_uuid.result
  force_destroy = true # Allow Terraform to delete the bucket even if it's not empty

  tags = merge(var.tags, {
    Name = "app-images-bucket-${var.profile}"
  })
}