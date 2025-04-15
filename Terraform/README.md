# tf-aws-infra

# Assignment 4: Infrastructure as Code with Terraform

This repository contains Terraform templates for setting up cloud infrastructure on AWS, including VPC, subnets, security groups, and EC2 instances.

## Requirements Implemented

### Application Security Group
- Created an EC2 security group (`aws_security_group.app_security_group`) for web application instances
- Added ingress rules to allow TCP traffic on the following ports:
   - Port 22 (SSH)
   - Port 80 (HTTP)
   - Port 443 (HTTPS)
   - Application port (configurable via the `app_port` variable, default: 8080)
- All ingress rules allow traffic from anywhere in the world (`0.0.0.0/0`)
- Configured egress rules to allow all outbound traffic

### EC2 Instance
- Created an EC2 instance (`aws_instance.web_app_instance`) with the following specifications:
   - Uses a custom AMI (specified via the `custom_ami_id` variable)
   - Instance type is configurable (default: t2.micro)
   - Launched in the first public subnet of the custom VPC
   - Attached the application security group to the instance
   - Root volume configured with:
      - Size: 25GB
      - Type: General Purpose SSD (GP2)
      - Set to be deleted on instance termination
   - Protection against accidental termination is disabled
   - Assigned a public IP address

## Deployment Instructions

### Prerequisites

- Terraform v1.6.0 or later
- AWS CLI configured with appropriate credentials
- Custom AMI ID for the EC2 instance

### Deployment Steps

1. Clone this repository
2. Navigate to the repository directory
3. Review and update the `terraform.tfvars` file with your specific values
4. Initialize Terraform:
   ```
   terraform init
   ```
5. Validate the Terraform configuration:
   ```
   terraform validate
   ```
6. Format the Terraform files:
   ```
   terraform fmt
   ```
7. Create an execution plan:
   ```
   terraform plan
   ```
8. Apply the Terraform configuration:
   ```
   terraform apply
   ```
9. When finished, you can destroy the infrastructure:
   ```
   terraform destroy
   ```
   

# Assignment 03

# AWS Networking Setup with Terraform

## Prerequisites

Before using the Terraform configuration files, make sure you have the following installed:

1. **AWS CLI**  
   Install and configure the AWS Command Line Interface (CLI) to interact with AWS resources. For installation and configuration instructions, refer to [AWS CLI Documentation](https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2.html).

2. **Terraform**  
   Install Terraform, a tool to manage AWS infrastructure as code. Follow the instructions on the [Terraform website](https://www.terraform.io/downloads.html).

## AWS Networking Setup

### VPC and Subnets
- Create a **VPC** with 3 **public subnets** and 3 **private subnets** across 3 different availability zones in the same region.
- Each availability zone has 1 public and 1 private subnet.

### Internet Gateway
- Create an **Internet Gateway** (IGW) and attach it to the VPC.

### Route Tables
- Create a **public route table** and attach all public subnets to it.
- Create a **private route table** and attach all private subnets to it.
- Add a public route in the **public route table** with the destination CIDR block `0.0.0.0/0` and the IGW as the target.

## Steps to Deploy

### Step 1: Initialize Terraform

Run the following command to initialize the Terraform environment:

```bash
terraform init
```

This will install the necessary providers and modules.

### Step 2: Apply Terraform Configuration

To apply the configuration and create the infrastructure, run:

```bash
terraform apply
```

Terraform will prompt you to confirm the creation of resources. Type `yes` to proceed.

### Step 3: Verify Infrastructure

Once the deployment is complete, you can log in to the AWS Management Console to verify the following:
- The VPC and subnets are created.
- The Internet Gateway is attached to the VPC.
- The route tables are set up and associated with the respective subnets.

## Clean Up

To destroy the infrastructure and remove all the resources created by Terraform, run the following command:

```bash
terraform destroy
```