#!/bin/bash
set -e
set -o pipefail

echo "Creating application.properties file..."
sudo touch $APP_DIR/application.properties
sudo chmod 666 $APP_DIR/application.properties

echo "Configuring application properties..."
cat << EOF | sudo tee $APP_DIR/application.properties
spring.application.name=web-app

# Server port configuration
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:postgresql://\${database_host:localhost}:5432/${database}
spring.datasource.username=\${username}
spring.datasource.password=\${password}
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.datasource.hikari.maximum-pool-size=2
spring.datasource.hikari.minimum-idle=1
spring.datasource.hikari.connection-timeout=10000
spring.datasource.hikari.idle-timeout=2000
spring.jpa.open-in-view=false

# AWS S3 Configuration
aws.s3.bucket=${S3_BUCKET_NAME}
aws.region=${AWS_REGION:us-east-1}

# File upload settings
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
app.max-file-size=10485760

# Security settings
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=true
EOF

echo "Setting correct ownership and permissions..."
sudo chown ${APP_USER:-csye6225}:${APP_GROUP:-csye6225} $APP_DIR/application.properties
sudo chmod 640 $APP_DIR/application.properties

echo "Application properties configuration completed!"