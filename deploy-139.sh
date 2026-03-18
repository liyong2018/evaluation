#!/bin/bash
set -e

SERVER="htht@172.16.41.139"
REMOTE_DIR="~/evaluation"
PASSWORD="1qaz@1234"

echo "========================================"
echo "  Deploying to Remote Server"
echo "  Server: 172.16.41.139"
echo "========================================"
echo ""

# Enable SSH connection multiplexing
mkdir -p ~/.ssh/cm
SSH_OPTS="-o ControlMaster=auto -o ControlPath=~/.ssh/cm/%r@%h:%p -o ControlPersist=10m"

# Function to run command with password
run_ssh() {
    sshpass -p "$PASSWORD" ssh $SSH_OPTS -o StrictHostKeyChecking=no "$SERVER" "$1"
}

run_scp() {
    sshpass -p "$PASSWORD" scp $SSH_OPTS -o StrictHostKeyChecking=no "$1" "$SERVER:$2"
}

echo "Step 1: Stopping Docker containers..."
run_ssh "cd $REMOTE_DIR && docker compose down" || true

echo "Step 2: Creating directories..."
run_ssh "mkdir -p $REMOTE_DIR/{target,frontend/dist}"

echo "Step 3: Uploading JAR file (~100MB, may take a while)..."
run_scp "target/disaster-reduction-evaluation-1.0.0.jar" "$REMOTE_DIR/target/"

echo "Step 4: Uploading frontend files..."
run_scp "-r frontend/dist/*" "$REMOTE_DIR/frontend/dist/"
run_scp "frontend/Dockerfile" "$REMOTE_DIR/frontend/"
run_scp "frontend/nginx.conf" "$REMOTE_DIR/frontend/"

echo "Step 5: Uploading docker compose files..."
run_scp "docker-compose.prod.yml" "$REMOTE_DIR/"
run_scp "Dockerfile.prod" "$REMOTE_DIR/"

echo "Step 6: Rebuilding and starting containers..."
run_ssh "cd $REMOTE_DIR && docker compose -f docker-compose.prod.yml up -d --build"

echo "Step 7: Checking deployment status..."
run_ssh "cd $REMOTE_DIR && docker compose ps"

echo ""
echo "========================================"
echo "  Deployment completed!"
echo "========================================"

# Clean up control socket
ssh $SSH_OPTS -O exit "$SERVER" 2>/dev/null || true
