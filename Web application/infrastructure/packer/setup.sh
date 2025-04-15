#!/bin/bash
set -e
set -o pipefail

echo "Installing dependencies..."
# Add a more resilient apt update that can handle potential errors with command-not-found database
sudo apt-get update -y || {
    echo "Initial apt-get update failed, trying with limited scope..."
    # Disable command-not-found temporarily 
    sudo mv /etc/apt/apt.conf.d/20apt-show-versions /etc/apt/apt.conf.d/20apt-show-versions.bak 2>/dev/null || true
    sudo apt-get update -y
}

sudo apt-get upgrade -y || echo "Upgrade failed, continuing with installation..."

echo "Adding required repositories..."
# Make sure universe repository is enabled (for maven)
sudo apt-get install -y software-properties-common
sudo add-apt-repository -y universe
sudo apt-get update -y || echo "Repository update failed, continuing with installation..."

echo "Installing Maven and Java"
sudo DEBIAN_FRONTEND=noninteractive apt-get install -y maven openjdk-21-jdk

echo "Installing AWS CLI..."
# Check if AWS CLI is installed
if ! command -v aws &> /dev/null; then
    # Install dependencies
    sudo apt-get install -y unzip curl
    
    # Download and install AWS CLI v2
    curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "/tmp/awscliv2.zip"
    unzip -q /tmp/awscliv2.zip -d /tmp
    sudo /tmp/aws/install
    
    # Clean up
    rm -rf /tmp/aws /tmp/awscliv2.zip
    
    echo "AWS CLI installed successfully"
    aws --version
else
    echo "AWS CLI is already installed"
    aws --version
fi

echo "Installing CloudWatch Agent..."
# Download and install the CloudWatch Agent
wget https://amazoncloudwatch-agent.s3.amazonaws.com/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb
sudo dpkg -i -E ./amazon-cloudwatch-agent.deb
rm ./amazon-cloudwatch-agent.deb

# Enable the CloudWatch Agent service to start on boot
sudo systemctl enable amazon-cloudwatch-agent

echo "Setup completed!"