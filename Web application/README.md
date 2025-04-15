# Webapp - Assignment 5

## Cloud Storage Integration

In this assignment, we are no longer install or use the local database on the EC2 instance. However, you will use the local database for your integration tests in your GitHub Actions.

The web application’s database will be the RDS instance launched with Terraform when running on the EC2 instance.

we are also creating a API specification to handle file using the APIs.

- GET API does not actually return the file but only returns the path to the file in the S3 bucket.
- Each file must be stored in an S3 bucket, and object metadata must be stored in the database.
- Users can (hard) delete files. The file will be deleted from the S3 bucket.

# Webapp - Assignment 4

## Packer & Custom Images

This project uses HashiCorp Packer to build custom machine images for both AWS and GCP.

### Architecture Overview

The Packer configuration builds identical custom images for both AWS (AMI) and GCP with the following components:

- Base OS: Ubuntu 24.04 LTS
- Java 21 (Temurin distribution)
- PostgreSQL database
- Spring Boot application as a systemd service


### CI/CD Workflows

The project includes GitHub Actions workflows for:

1. **Packer Validation** (`packer-validate.yml`): Runs on pull requests to validate Packer templates
   - Formats and validates Packer templates
   - Prevents merging if validation fails

2. **Packer Build** (`packer-build.yml`): Runs when code is merged to the main branch
   - Builds the application JAR
   - Creates custom images in both AWS and GCP
   - Shares the images with appropriate target accounts/projects

3. **Web App CI** (`web-app-ci.yml`): Runs tests on pull requests to ensure application quality


# Webapp - Assignment 3

# Continuous Integration (CI) with GitHub Actions

## Continuous Integration (CI) for Web App

### Workflow Overview

A GitHub Actions workflow is added to:

1. Run **application tests** for every pull request.
2. Prevent merging if any test fails.

### Enforcing Branch Protection

1. Go to **GitHub Repository Settings → Branches**.
2. Under **Branch Protection Rules**, click **Add Rule**.
3. Select the **main** branch.
4. Enable **Require status checks to pass before merging**.
5. Select the workflow check (`Run Application Tests`).

# Webapp - Assignment 2

# **Prerequisites and Setup Guide**

## **Prerequisites**

Before building and deploying the application, ensure you have the following prerequisites installed:

```
sudo apt update

sudo apt upgrade -y

sudo apt install unzip
```

## **Configuration**

### **Database Configuration**

In .env file, update your Database Details

```properties
DB_URL=jdbc:postgresql://localhost:5432/<Your Database>
DB_USERNAME=<Database Name>
DB_PASSWORD=<Password>
```

## **Setup Instructions**

### **Download the Assignment Files**

Download the ZIP file from GitHub and extract it. Move the setup.sh file outside the extracted directory to avoid zipping it again.

### **Re-zip the Directory (without setup.sh)**

Zip the directory again but exclude setup.sh from the archive.

### **Push to Virtual Machine**

Upload the .zip file, setup.sh, and .env to your virtual machine using your preferred method.

### **Set Permissions for setup.sh**

```
chmod +x setup.sh
```

### **Execute the Setup Script**

Run the setup script to configure the environment.

```
./setup.sh

```

The script will:

Update the system using sudo apt update and sudo apt upgrade
Install unzip, PostgreSQL, Maven
Create the necessary database, user, and group in PostgreSQL
Set up the required Maven configurations


# Webapp - Assignment 1

# **Prerequisites and Setup Guide**

## **Prerequisites**
Before building and deploying the application locally, ensure you have the following prerequisites installed:

### **1. Java Development Kit (JDK)**
* **Required:** JDK 17 or later
* **Verify installation:** `java -version`
* **Download from:** [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://openjdk.java.net/)

### **2. Apache Maven**
* **Required for:** Building and managing dependencies
* **Verify installation:** `mvn -version`
* **Download from:** [Apache Maven](https://maven.apache.org/download.cgi)

### **3. Database**
* **Required:** PostgreSQL (recommended) or MySQL
* **Setup:** Create a database for the application
* **Configuration:** Update application.properties with your database credentials

## **Configuration**

### **Database Configuration**

In .env file, update your Database Details

```properties
DB_URL=jdbc:postgresql://localhost:5432/<Your Database>
DB_USERNAME=<Database Name>
DB_PASSWORD=<Password>
```

## **Build and Deploy Instructions**

### **1. Clone and Setup**
```bash
git clone <repository-url>
cd webapp
```

### **2. Build Process**
```bash
# Build the project
mvn clean install

# Compile the code
mvn clean compile

# Run the tests
mvn test
```

### **3. Run Application**
```bash
# Using Maven
mvn spring-boot:run

# Or using JAR file
java -jar target/webapp-1.0.0.jar
```

### **4. Access Application**
Access the application at `http://localhost:8080`



