# Launch Template for Auto Scaling Group
resource "aws_launch_template" "app_launch_template" {
  name          = "csye6225_asg_template-${var.profile}"
  image_id      = var.custom_ami_id
  instance_type = var.instance_type

  # IAM Instance Profile
  iam_instance_profile {
    name = aws_iam_instance_profile.ec2_instance_profile.name
  }

  # Network Interfaces
  network_interfaces {
    associate_public_ip_address = true
    security_groups             = [aws_security_group.app_security_group.id]
  }

  # Root Block Device Configuration with KMS encryption
  block_device_mappings {
    device_name = "/dev/sda1"
    ebs {
      volume_size           = var.root_volume_size
      volume_type           = var.root_volume_type
      delete_on_termination = true
      encrypted             = true
      kms_key_id            = aws_kms_key.ec2_key.arn
    }
  }

  # User data for application configuration
  user_data = base64encode(templatefile(var.user_data_template_path, {
    db_secret_arn  = aws_secretsmanager_secret.db_credentials.arn
    bucket_name    = aws_s3_bucket.app_bucket.id
    AWS_REGION     = var.aws_region
    profile        = var.profile
    log_group_name = aws_cloudwatch_log_group.webapp_log_group.name
  }))

  tag_specifications {
    resource_type = "instance"
    tags = merge(var.tags, {
      Name = "${var.ec2_instance_name}-${var.profile}"
    })
  }

  # Add metadata options for IMDSv2
  metadata_options {
    http_endpoint               = "enabled"
    http_tokens                 = "required" # Makes IMDSv2 required
    http_put_response_hop_limit = 1
  }
}