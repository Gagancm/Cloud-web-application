#!/bin/bash
set -e
set -o pipefail

echo "Creating application user and group..."
sudo groupadd -f $APP_GROUP
sudo useradd -m -g $APP_GROUP -s /usr/sbin/nologin $APP_USER || echo "User already exists"

echo "Setting up application directory..."
sudo mkdir -p $APP_DIR
sudo chown $APP_USER:$APP_GROUP $APP_DIR
sudo chmod 750 $APP_DIR

echo "Moving application jar to application directory..."
sudo mv /tmp/webapp-0.0.1-SNAPSHOT.jar $APP_DIR/
sudo chown $APP_USER:$APP_GROUP $APP_DIR/webapp-0.0.1-SNAPSHOT.jar
sudo chmod 750 $APP_DIR/webapp-0.0.1-SNAPSHOT.jar

echo "Application user, group, and directory setup completed!"