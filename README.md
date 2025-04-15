# Cloud Web Application Infrastructure

This repository contains a web application and the infrastructure as code (Terraform) required to deploy it to AWS.

## Project Overview

This project consists of two main components:

1. **Web Application** - A Spring Boot application with cloud storage integration
2. **Terraform Infrastructure** - Infrastructure as code to set up the required AWS resources

## Web Application

### Assignment 5: Cloud Storage Integration

The web application now integrates with cloud storage:

- Database is now using RDS instance launched with Terraform when running on EC2
- API specification to handle files using cloud services:
  - GET API returns the path to files in the S3 bucket
  - Files are stored in an S3 bucket with metadata stored in the database
  - Users can delete files from the S3 bucket

### Assignment 4: Packer & Custom Images

The project uses HashiCorp Packer to build custom machine images for both AWS and GCP with:

- Base OS: Ubuntu 24.04 LTS
- Java 21 (Temurin distribution)
- PostgreSQL database
- Spring Boot application as a systemd service

CI/CD workflows include:
1. **Packer Validation** on pull requests
2. **Packer Build** when code is merged to main branch
3. **Web App CI** to run tests on pull requests

### Assignment 3: Continuous Integration with GitHub Actions

A GitHub Actions workflow to:
- Run application tests for every pull request
- Prevent merging if any test fails

Branch protection rules are enforced for the main branch.

### Assignment 2 & 1: Prerequisites and Setup

#### Prerequisites
- JDK 17 or later
- Apache Maven
- PostgreSQL or MySQL database

#### Configuration
Database configuration in `.env` file:
```properties
DB_URL=jdbc:postgresql://localhost:5432/<Your Database>
DB_USERNAME=<Database Name>
DB_PASSWORD=<Password>
```

#### Build and Deploy Instructions
```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run

# Or using JAR file
java -jar target/webapp-1.0.0.jar
```

## Terraform Infrastructure

### Assignment 4: Infrastructure as Code with Terraform

The Terraform templates set up AWS infrastructure, including:

#### Application Security Group
- EC2 security group with ingress rules for:
  - Port 22 (SSH)
  - Port 80 (HTTP)
  - Port 443 (HTTPS)
  - Application port (default: 8080)
- All egress traffic allowed

#### EC2 Instance
- Custom AMI
- Configurable instance type (default: t2.micro)
- 25GB GP2 root volume
- Public IP address assigned

### Assignment 3: AWS Networking Setup

Sets up the following AWS networking components:
- VPC with 3 public and 3 private subnets across different availability zones
- Internet Gateway attached to the VPC
- Public and private route tables with appropriate routes

## CI/CD for Web Application - Update Pull Request Merged Workflow

The following workflow triggers when a pull request is merged:

1. Run unit tests
2. Validate Packer Template
3. Build Application Artifact(s)
4. Build AMI in DEV AWS account with:
   - OS package upgrades
   - Installation of dependencies
   - Application setup
   - Auto-start configuration
5. Share the AMI with the DEMO AWS account
6. Reconfigure GitHub Runner's AWS CLI to use DEMO AWS account access keys
7. Create a new Launch Template version with the latest AMI ID
8. Issue an "instance refresh" command to the auto-scaling group
9. Wait for instance refresh to complete before exiting

### AWS Key Management Service
- Create KMS keys for:
  - EC2
  - RDS
  - S3 Buckets
  - Secret Manager (Database Password & Email Service Credentials)
- 90-day rotation period
- Use KMS keys when creating resources in Terraform

### Database Password
- Auto-generated password stored in Secret Manager with custom KMS key
- Retrieved by user-data script for web application configuration

### SSL Certificates
- Dev: AWS Certificate Manager service
- Demo: Certificate from external vendor, imported into AWS Certificate Manager
- No HTTP to HTTPS redirection required
- Traffic from load balancer to EC2 can use HTTP
- Direct connection to EC2 instances not allowed

## Deployment Instructions

### Prerequisites
- Terraform v1.6.0 or later
- AWS CLI configured with appropriate credentials
- Custom AMI ID for the EC2 instance

### Deployment Steps
1. Clone this repository
2. Update the `terraform.tfvars` file with your values
3. Initialize Terraform: `terraform init`
4. Validate the configuration: `terraform validate`
5. Format the Terraform files: `terraform fmt`
6. Create an execution plan: `terraform plan`
7. Apply the configuration: `terraform apply`
8. When finished, destroy the infrastructure: `terraform destroy`
