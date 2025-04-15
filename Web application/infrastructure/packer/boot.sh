#!/bin/bash
set -e
set -o pipefail

echo "Preparing to generate systemd service file..."
sudo chmod +x /tmp/generate-service-file.sh

echo "Generating systemd service file with proper environment variables..."
sudo -E /tmp/generate-service-file.sh

echo "Service file generation complete. Setting up systemd service..."
sudo systemctl daemon-reload
sudo systemctl enable webapp.service

echo "Starting webapp service..."
sudo systemctl start webapp.service
sudo systemctl status webapp.service

echo "Systemd service setup completed!"