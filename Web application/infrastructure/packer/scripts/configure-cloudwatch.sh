#!/bin/bash
set -e
set -o pipefail

echo "=========================================================="
echo "CONFIGURING CLOUDWATCH AGENT: $(date)"
echo "=========================================================="

# Check if CloudWatch agent is installed
if [ ! -f /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl ]; then
    echo "CloudWatch agent not found, installing..."
    
    # Install CloudWatch agent
    wget https://amazoncloudwatch-agent.s3.amazonaws.com/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb -O /tmp/amazon-cloudwatch-agent.deb
    sudo dpkg -i -E /tmp/amazon-cloudwatch-agent.deb
    rm /tmp/amazon-cloudwatch-agent.deb
    
    echo "CloudWatch agent installed successfully"
else
    echo "CloudWatch agent already installed"
fi

# Create required directories
sudo mkdir -p /opt/aws/amazon-cloudwatch-agent/etc
sudo mkdir -p /var/log/amazon-cloudwatch-agent

# Ensure log files exist with proper permissions
sudo touch /var/log/amazon-cloudwatch-agent/amazon-cloudwatch-agent.log
sudo chmod 755 /var/log/amazon-cloudwatch-agent/amazon-cloudwatch-agent.log

# Ensure application log directory exists
sudo mkdir -p /var/log/webapp
sudo touch /var/log/webapp/application.log
sudo chmod 755 /var/log/webapp/application.log

# Copy the CloudWatch configuration file
echo "Copying CloudWatch configuration..."
sudo cp /opt/webapp/cloudwatch-config.json /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json

# Set proper permissions
sudo chmod 755 /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json

# Configure and start the CloudWatch agent
echo "Starting CloudWatch agent..."
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -s -c file:/opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json

# Enable the service to start on boot
sudo systemctl enable amazon-cloudwatch-agent

# Restart the agent to apply new configuration
sudo systemctl restart amazon-cloudwatch-agent

# Check CloudWatch agent status
echo "CloudWatch agent service status:"
systemctl status amazon-cloudwatch-agent --no-pager || true

# Check for CloudWatch agent logs
echo "CloudWatch agent logs:"
tail -n 50 /var/log/amazon-cloudwatch-agent/amazon-cloudwatch-agent.log || echo "No CloudWatch agent logs found!"

echo "=========================================================="
echo "CLOUDWATCH AGENT CONFIGURATION COMPLETE: $(date)"
echo "=========================================================="