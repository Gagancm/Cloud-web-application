# Common Variables
variable "ami_name" {
  type        = string
  description = "Name for the AMI"
  default     = "webapp"
}

# AWS Variables
variable "aws_region" {
  type        = string
  description = "AWS Region for AMI build"
  default     = "us-east-1"
}

variable "aws_instance_type" {
  type        = string
  description = "EC2 instance type for building AMI"
  default     = "t2.micro"
}

variable "aws_source_ami_owner" {
  type        = string
  description = "Owner ID for the source AMI"
  default     = "099720109477"
}

variable "aws_source_ami_filter_name" {
  type        = string
  description = "Name pattern for the source AMI"
  default     = "ubuntu/images/hvm-ssd/ubuntu-noble-24.04-amd64-server-*"
}

# Application Variables
variable "app_binary" {
  type        = string
  description = "Path to the application JAR file"
  default     = "./artifacts/webapp-0.0.1-SNAPSHOT.jar"
}

variable "ssh_username" {
  type        = string
  description = "SSH username for the instances"
  default     = "ubuntu"
}

variable "app_user" {
  type        = string
  description = "Application user"
  default     = "csye6225"
}

variable "app_group" {
  type        = string
  description = "Application group"
  default     = "csye6225"
}

variable "app_dir" {
  type        = string
  description = "Application directory"
  default     = "/opt/webapp"
}

variable "db_name" {
  type        = string
  description = "Database name"
  default     = "csye6225"
}

variable "db_user" {
  type        = string
  description = "Database user"
  default     = "postgres"
}

variable "db_password" {
  type        = string
  description = "Database password"
  default     = "password"
}

variable "webapp_service" {
  type        = string
  description = "Systemd service file name"
  default     = "webapp.service"
}

# variables for AWS RDS and S3 integration
variable "database_host" {
  type        = string
  description = "Database host address"
  default     = "localhost"
}

variable "s3_bucket_name" {
  type        = string
  description = "S3 bucket name for file storage"
  default     = ""
}