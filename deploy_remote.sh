#!/bin/bash
# Deployment script - run this ON the remote server after SSH connection

SERVER="172.16.43.189"
USER="root"
REMOTE_DIR="~/evaluation"

echo "========================================="
echo "   Remote Deployment Instructions"
echo "========================================="
echo ""
echo "Step 1: Connect to remote server"
echo "  ssh $USER@$SERVER"
echo ""
echo "Step 2: On remote server, run:"
echo "  cd $REMOTE_DIR"
echo "  git pull"
echo "  mvn clean package -DskipTests"
echo "  cd frontend && npm run build"
echo "  docker compose build evaluation-app"
echo "  docker compose up -d"
echo ""
echo "========================================="
