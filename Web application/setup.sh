#!/bin/bash
set -e
set -o pipefail

echo "Installing dependencies..."
sudo apt-get update -y
sudo apt-get upgrade -y

# Make sure all required repositories are enabled
echo "Ensuring required repositories are enabled..."
sudo apt-get install -y software-properties-common
sudo apt-add-repository universe
sudo apt-add-repository multiverse
sudo apt-get update -y

echo "Installing Maven and Java"
sudo DEBIAN_FRONTEND=noninteractive apt-get install -y maven openjdk-21-jdk

echo "Installing PostgreSQL..."
sudo DEBIAN_FRONTEND=noninteractive apt-get install -y postgresql postgresql-contrib

echo "Configuring PostgreSQL..."
sudo systemctl enable postgresql
sudo systemctl start postgresql

# Wait for PostgreSQL to start completely
echo "Waiting for PostgreSQL to start..."
for i in {1..30}; do
  if pg_isready -q; then
    echo "PostgreSQL is ready!"
    break
  fi
  echo "Waiting for PostgreSQL to start... ($i/30)"
  sleep 1
done

# Configure PostgreSQL
echo "Setting up PostgreSQL database and user..."
sudo -u postgres psql <<EOF
DO
\$do\$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '$DB_USER') THEN
        CREATE USER $DB_USER WITH PASSWORD '$DB_PASSWORD';
    ELSE
        ALTER USER $DB_USER WITH PASSWORD '$DB_PASSWORD';
    END IF;
END
\$do\$;

DROP DATABASE IF EXISTS $DB_NAME;
CREATE DATABASE $DB_NAME;
GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_USER;
\c $DB_NAME
GRANT ALL ON SCHEMA public TO $DB_USER;
EOF

echo "PostgreSQL setup completed!"