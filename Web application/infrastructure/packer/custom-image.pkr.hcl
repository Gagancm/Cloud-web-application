packer {
  required_plugins {
    amazon = {
      version = ">= 1.2.8"
      source  = "github.com/hashicorp/amazon"
    }
  }
}

// AWS Source Configuration
source "amazon-ebs" "custom-image" {
  ami_name      = "${var.ami_name}-${formatdate("YYYYMMDDHHmmss", timestamp())}"
  region        = var.aws_region
  instance_type = var.aws_instance_type
  source_ami_filter {
    filters = {
      name                = var.aws_source_ami_filter_name
      virtualization-type = "hvm"
      root-device-type    = "ebs"
    }
    owners      = [var.aws_source_ami_owner]
    most_recent = true
  }
  ssh_username = var.ssh_username
}

build {
  name = "packer"
  sources = [
    "source.amazon-ebs.custom-image"
  ]

  provisioner "file" {
    source      = var.app_binary
    destination = "/tmp/webapp-0.0.1-SNAPSHOT.jar"
  }

  provisioner "file" {
    source      = "scripts/webapp.service"
    destination = "/tmp/webapp.service"
  }

  provisioner "file" {
    source      = "generate-service-file.sh"
    destination = "/tmp/generate-service-file.sh"
  }

  # Run setup script to install dependencies
  provisioner "shell" {
    environment_vars = [
      "DEBIAN_FRONTEND=noninteractive"
    ]
    script = "setup.sh"
  }

  # Setup application user and group
  provisioner "shell" {
    environment_vars = [
      "APP_USER=${var.app_user}",
      "APP_GROUP=${var.app_group}",
      "APP_DIR=${var.app_dir}"
    ]
    script = "usergroup.sh"
  }

  # Install and setup CloudWatch agent
  provisioner "shell" {
    inline = [
      "wget https://amazoncloudwatch-agent.s3.amazonaws.com/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb",
      "sudo dpkg -i amazon-cloudwatch-agent.deb",
      "sudo mkdir -p /var/log/webapp",
      "sudo chown ${var.app_user}:${var.app_group} /var/log/webapp",
      "sudo chmod 755 /var/log/webapp"
    ]
  }

  provisioner "file" {
    source      = "cloudwatch-config.json"
    destination = "/tmp/amazon-cloudwatch-agent.json"
  }

  provisioner "shell" {
    inline = [
      "sudo mv /tmp/amazon-cloudwatch-agent.json /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json",
      "sudo chown root:root /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json",
      "sudo chmod 644 /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json"
    ]
  }

  # Configure application properties
  provisioner "shell" {
    environment_vars = [
      "DB_USER=${var.db_user}",
      "DB_PASSWORD=${var.db_password}",
      "DB_NAME=${var.db_name}",
      "APP_DIR=${var.app_dir}",
      "S3_BUCKET_NAME=${var.s3_bucket_name}",
      "AWS_REGION=${var.aws_region}"
    ]
    script = "startup.sh"
  }

  # Setup systemd service
  provisioner "shell" {
    inline = [
      "chmod +x /tmp/generate-service-file.sh",
      "sudo -E /tmp/generate-service-file.sh",
      "sudo systemctl daemon-reload",
      "sudo systemctl enable webapp.service"
    ]
  }
}