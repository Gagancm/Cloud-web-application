# # EC2 Instance
# resource "aws_instance" "web_app_instance" {
#   ami                    = var.custom_ami_id
#   instance_type          = var.instance_type
#   subnet_id              = aws_subnet.public_subnet[0].id
#   vpc_security_group_ids = [aws_security_group.app_security_group.id]
#   iam_instance_profile   = aws_iam_instance_profile.ec2_instance_profile.name # Added IAM profile for CloudWatch

#   # Assign public IP address
#   associate_public_ip_address = true

#   # Root volume configuration
#   root_block_device {
#     volume_size           = var.root_volume_size
#     volume_type           = var.root_volume_type
#     delete_on_termination = true
#   }

#   # User data script to configure the application
#   user_data = templatefile(var.user_data_template_path, {
#     db_username = var.db_username
#     db_password = var.db_password
#     db_name     = var.db_name
#     db_hostname = aws_db_instance.app_db.address
#     bucket_name = aws_s3_bucket.app_bucket.id
#     AWS_REGION  = var.aws_region
#     profile     = var.profile
#     log_group_name = aws_cloudwatch_log_group.webapp_log_group.name 
#   })

#   # Disable termination protection
#   disable_api_termination = false

#   tags = merge(var.tags, {
#     Name = "${var.ec2_instance_name}-${var.profile}"
#   })
# }