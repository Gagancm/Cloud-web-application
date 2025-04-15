variable "aws_region" {
  description = "The AWS region to create resources in"
  type        = string
  default     = "us-east-1"
}

variable "profile" {
  type    = string
  default = "dev"
}

variable "vpc_cidr_block" {
  description = "The CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "vpc_name" {
  description = "The name of the VPC"
  type        = string
  default     = "vpc_1"
}

variable "public_subnet_name" {
  description = "The name prefix for the public subnets"
  type        = string
  default     = "public-subnet"
}

variable "private_subnet_name" {
  description = "The name prefix for the private subnets"
  type        = string
  default     = "private-subnet"
}

variable "aws_internet_gateway_name" {
  description = "The name of the Internet Gateway"
  type        = string
  default     = "main-gateway"
}

variable "public_route_table_name" {
  description = "The name of the public route table"
  type        = string
  default     = "public-route-table"
}

variable "private_route_table_name" {
  description = "The name of the private route table"
  type        = string
  default     = "private-route-table"
}

variable "availability_zones" {
  description = "List of availability zones in the region"
  type        = list(string)
  default     = ["us-east-1a", "us-east-1b", "us-east-1c"]
}

variable "tags" {
  description = "Tags to apply to resources"
  type        = map(string)
  default = {
    "Environment" = "Dev"
    "Project"     = "VPC Setup"
  }
}

# Variable for public subnet CIDRs
variable "public_subnet_cidrs" {
  description = "List of CIDR blocks for the public subnets"
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24", "10.0.3.0/24"]
}

# Variable for private subnet CIDRs
variable "private_subnet_cidrs" {
  description = "List of CIDR blocks for the private subnets"
  type        = list(string)
  default     = ["10.0.4.0/24", "10.0.5.0/24", "10.0.6.0/24"]
}

# Security Group Variables
variable "app_security_group_name" {
  description = "Name of the application security group"
  type        = string
  default     = "application-security-group"
}

variable "app_port" {
  description = "Port on which the application runs"
  type        = number
  default     = 8080
}

# EC2 Instance Variables
variable "custom_ami_id" {
  description = "ID of your custom AMI"
  type        = string
  sensitive   = true
}

variable "instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t2.micro"
}

variable "ec2_instance_name" {
  description = "Name of the EC2 instance"
  type        = string
  default     = "web-application-server"
}

variable "root_volume_size" {
  description = "Size of the root volume in GB"
  type        = number
  default     = 25
}

variable "root_volume_type" {
  description = "Type of the root volume"
  type        = string
  default     = "gp2"
}

# Database Security Group
variable "db_security_group_name" {
  description = "Name of the database security group"
  type        = string
  default     = "database-security-group"
}

# RDS Variables
variable "db_allocated_storage" {
  description = "Allocated storage for the RDS instance in GB"
  type        = number
  default     = 20
}

variable "db_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t3.micro" # Cheapest option as required
}

variable "db_instance_identifier" {
  description = "Identifier for the RDS instance"
  type        = string
  default     = "csye6225" # As required in the assignment
}

variable "db_name" {
  description = "Name of the database to create"
  type        = string
  default     = "csye6225" # As required in the assignment
}

variable "db_username" {
  description = "Username for the database"
  type        = string
  default     = "csye6225" # As required in the assignment
  sensitive   = true
}

variable "db_password" {
  description = "Password for the database"
  type        = string
  sensitive   = true
}

variable "db_engine" {
  description = "Database engine to use (mysql, postgres, mariadb)"
  type        = string
  default     = "postgres"
}

variable "db_engine_version" {
  description = "Version of the database engine"
  type        = string
  default     = "14.17"
}

variable "db_parameter_group_family" {
  description = "DB parameter group family"
  type        = string
  default     = "postgres14"
}

# S3 Variables
variable "app_bucket_name_prefix" {
  description = "Prefix for the application S3 bucket name (UUID will be appended)"
  type        = string
  default     = "csye6225-app-images"
}

# User data template file path
variable "user_data_template_path" {
  description = "Path to the user data template file"
  type        = string
  default     = "user_data.tftpl"
}

# Load Balancer Variables
variable "lb_security_group_name" {
  description = "Name of the load balancer security group"
  type        = string
  default     = "load-balancer-security-group"
}

# Key Pair Variable
variable "key_name" {
  description = "Name of the AWS key pair to use for EC2 instances (optional)"
  type        = string
  default     = ""
}

variable "ec2_kms_key_id" {
  description = "ID of the KMS key used for EC2 EBS encryption"
  type        = string
  default     = "" # You'll need to provide this when applying
}

variable "certificate_arn" {
  description = "ARN of the imported SSL certificate in ACM"
  type        = string
  default     = "" # Empty default allows applying without HTTPS initially
}