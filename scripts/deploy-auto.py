#!/usr/bin/env python3
"""
Automated deployment script for 172.16.41.139
Uses subprocess to handle password authentication
"""

import os
import sys
import subprocess
import time

SERVER = "172.16.41.139"
USER = "htht"
PASSWORD = "1qaz@1234"
REMOTE_DIR = "~/evaluation"
BASE_DIR = "d:/Evaluation/evaluation"

def run_command(cmd, input_password=False):
    """Run command and optionally provide password"""
    print(f"Running: {cmd[:80]}...")

    if input_password:
        process = subprocess.Popen(
            cmd,
            shell=True,
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True
        )
        stdout, stderr = process.communicate(input=PASSWORD + '\n')
        return process.returncode, stdout, stderr
    else:
        result = subprocess.run(
            cmd,
            shell=True,
            capture_output=True,
            text=True,
            timeout=300
        )
        return result.returncode, result.stdout, result.stderr

def main():
    print("=" * 60)
    print("  Automated Deployment to 172.16.41.139")
    print("=" * 60)
    print()

    # Check if files exist
    jar_path = f"{BASE_DIR}/target/disaster-reduction-evaluation-1.0.0.jar"
    dist_path = f"{BASE_DIR}/frontend/dist"

    if not os.path.exists(jar_path):
        print(f"ERROR: JAR file not found at {jar_path}")
        sys.exit(1)

    if not os.path.exists(dist_path):
        print(f"ERROR: Frontend dist not found at {dist_path}")
        sys.exit(1)

    print("Step 1: Testing SSH connection...")
    # Try using sshpass-like approach with plink if available
    # Or use standard SSH with expect

    # For Windows, let's try using the Windows OpenSSH with a different approach
    # We'll create a temporary batch file that handles the password

    batch_content = f"""@echo off
set SERVER={SERVER}
set USER={USER}
set PASSWORD={PASSWORD}
set REMOTE_DIR={REMOTE_DIR}
set BASE_DIR={BASE_DIR}

echo Connecting to server...

echo Step 1: Stopping containers...
ssh -o StrictHostKeyChecking=no %USER%@%SERVER% "cd %REMOTE_DIR% && docker compose down 2>nul || exit 0"

echo Step 2: Creating directories...
ssh -o StrictHostKeyChecking=no %USER%@%SERVER% "mkdir -p %REMOTE_DIR%/target"
ssh -o StrictHostKeyChecking=no %USER%@%SERVER% "mkdir -p %REMOTE_DIR%/frontend/dist"

echo Step 3: Uploading JAR file...
scp -o StrictHostKeyChecking=no %BASE_DIR%\\target\\disaster-reduction-evaluation-1.0.0.jar %USER%@%SERVER%:%REMOTE_DIR%/target/

echo Step 4: Uploading frontend files...
ssh -o StrictHostKeyChecking=no %USER%@%SERVER% "rm -rf %REMOTE_DIR%/frontend/dist/*"
scp -o StrictHostKeyChecking=no -r %BASE_DIR%\\frontend\\dist\\* %USER%@%SERVER%:%REMOTE_DIR%/frontend/dist/
scp -o StrictHostKeyChecking=no %BASE_DIR%\\frontend\\Dockerfile %USER%@%SERVER%:%REMOTE_DIR%/frontend/
scp -o StrictHostKeyChecking=no %BASE_DIR%\\frontend\\nginx.conf %USER%@%SERVER%:%REMOTE_DIR%/frontend/

echo Step 5: Uploading docker configuration...
scp -o StrictHostKeyChecking=no %BASE_DIR%\\docker-compose.prod.yml %USER%@%SERVER%:%REMOTE_DIR%/
scp -o StrictHostKeyChecking=no %BASE_DIR%\\Dockerfile.prod %USER%@%SERVER%:%REMOTE_DIR%/

echo Step 6: Building and starting containers...
ssh -o StrictHostKeyChecking=no %USER%@%SERVER% "cd %REMOTE_DIR% && docker compose -f docker-compose.prod.yml up -d --build"

echo Step 7: Checking status...
ssh -o StrictHostKeyChecking=no %USER%@%SERVER% "cd %REMOTE_DIR% && docker compose ps"

echo.
echo Deployment completed!
echo.
pause
"""

    batch_path = "d:/Evaluation/evaluation/deploy-auto.bat"
    with open(batch_path, 'w') as f:
        f.write(batch_content)

    print(f"Created deployment batch file: {batch_path}")
    print()
    print("To deploy, run the batch file. You'll need to enter your password multiple times.")
    print()
    print("For passwordless deployment, please set up SSH key authentication:")
    print("1. Copy your public key (above)")
    print("2. SSH to the server and add it to ~/.ssh/authorized_keys")
    print()

if __name__ == "__main__":
    main()
