#!/bin/bash

set -e

DEPLOY_DIR=".deploy"

echo "Creating deploy directory..."
mkdir -p $DEPLOY_DIR

echo "Copying files..."
cp docker-compose.yml $DEPLOY_DIR/
cp -r backend $DEPLOY_DIR/
cp -r frontend $DEPLOY_DIR/

echo "Building and starting containers..."
cd $DEPLOY_DIR
docker-compose down
docker-compose build --no-cache
docker-compose up -d

echo "Deploy completed!"
echo "Frontend: http://localhost:3000"
echo "Backend: http://localhost:8080"
