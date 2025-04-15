#!/bin/bash
set -e
set -o pipefail

echo "Generating systemd service file with environment variables..."

# Default values
DB_HOSTNAME=${DB_HOSTNAME:-localhost}
DB_NAME=${DB_NAME:-csye6225}
DB_USERNAME=${DB_USERNAME:-postgres}
DB_PASSWORD=${DB_PASSWORD:-password}
AWS_REGION=${AWS_REGION:-us-east-1}
S3_BUCKET_NAME=${S3_BUCKET_NAME:-csye6225-bucket}
APP_USER=${APP_USER:-csye6225}
APP_GROUP=${APP_GROUP:-csye6225}

echo "Creating webapp.service file with proper environment variables..."

cat << EOF > /tmp/webapp.service.tmp
[Unit]
Description=Spring Boot Web Application
After=network.target

[Service]
User=${APP_USER}
Group=${APP_GROUP}
Type=simple
Environment="SPRING_CONFIG_LOCATION=/opt/webapp/application.properties"

# Database connection environment variables
Environment="DB_URL=jdbc:postgresql://${DB_HOSTNAME}:5432/${DB_NAME}"
Environment="DB_USERNAME=${DB_USERNAME}"
Environment="DB_PASSWORD=${DB_PASSWORD}"
Environment="DB_NAME=${DB_NAME}"
Environment="DB_HOST=${DB_HOSTNAME}"
Environment="DB_PORT=5432"

# AWS environment variables
Environment="AWS_REGION=${AWS_REGION}"
Environment="S3_BUCKET_NAME=${S3_BUCKET_NAME}"

WorkingDirectory=/opt/webapp
ExecStart=/usr/bin/java -jar /opt/webapp/webapp-0.0.1-SNAPSHOT.jar
SuccessExitStatus=143
TimeoutStopSec=10
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
EOF

# Move the temporary file to the expected location
mv /tmp/webapp.service.tmp /etc/systemd/system/webapp.service
chmod 644 /etc/systemd/system/webapp.service

echo "webapp.service file generated successfully"