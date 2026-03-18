#!/usr/bin/env python3
"""
Interactive deployment script for 172.16.41.139
Only requires password input once
"""

import os
import sys
import getpass
import paramiko
import scp
import time
import stat

# Configuration
SERVER = "172.16.41.139"
USER = "htht"
REMOTE_DIR = "~/evaluation"
BASE_DIR = "d:/Evaluation/evaluation"

def create_ssh_client(password):
    """Create and return SSH client with password"""
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    try:
        client.connect(SERVER, username=USER, password=password, timeout=30)
        return client
    except paramiko.AuthenticationException:
        print("Authentication failed! Password may be incorrect.")
        return None

def execute_command(ssh_client, command, show_output=True):
    """Execute SSH command and print output"""
    stdin, stdout, stderr = ssh_client.exec_command(command)
    output = stdout.read().decode()
    error = stderr.read().decode()
    if output and show_output:
        for line in output.strip().split('\n'):
            print(f"  {line}")
    if error:
        for line in error.strip().split('\n'):
            print(f"  [ERROR] {line}", file=sys.stderr)
    return output

def mkdir_p(sftp_client, remote_dir):
    """Create directory recursively"""
    dirs = []
    dir_path = remote_dir
    while True:
        try:
            sftp_client.stat(dir_path)
            break
        except IOError:
            dirs.append(dir_path)
            dir_path = os.path.dirname(dir_path)
            if dir_path == '/':
                break
    for dir_path in reversed(dirs):
        try:
            sftp_client.mkdir(dir_path)
        except:
            pass

def upload_file(sftp_client, local_path, remote_path):
    """Upload file via SFTP"""
    print(f"  Uploading: {os.path.basename(local_path)}")
    if os.path.isdir(local_path):
        mkdir_p(sftp_client, remote_path)
        for item in os.listdir(local_path):
            local_item = os.path.join(local_path, item)
            remote_item = f"{remote_path}/{item}"
            if os.path.isdir(local_item):
                upload_file(sftp_client, local_item, remote_item)
            else:
                sftp_client.put(local_item, remote_item)
    else:
        # Ensure parent directory exists
        parent_dir = os.path.dirname(remote_path)
        mkdir_p(sftp_client, parent_dir)
        sftp_client.put(local_path, remote_path)

def main():
    print("=" * 60)
    print("  Deployment Script for 172.16.41.139")
    print("=" * 60)
    print()

    # Get password from user
    password = getpass.getpass(f"Enter SSH password for {USER}@{SERVER}: ")

    # Create SSH client
    print("Connecting to server...")
    ssh_client = create_ssh_client(password)
    if not ssh_client:
        sys.exit(1)

    print("Connected successfully!")
    print()

    # Expand remote directory
    stdin, stdout, stderr = ssh_client.exec_command(f"echo {REMOTE_DIR}")
    remote_dir = stdout.read().decode().strip()

    try:
        # Step 1: Stop containers
        print("Step 1: Stopping Docker containers...")
        execute_command(ssh_client, f"cd {remote_dir} && docker compose down 2>/dev/null || true", show_output=False)
        print("  Done.")
        print()

        # Step 2: Create directories
        print("Step 2: Creating directories...")
        execute_command(ssh_client, f"mkdir -p {remote_dir}/target")
        execute_command(ssh_client, f"mkdir -p {remote_dir}/frontend/dist")
        print("  Done.")
        print()

        # Step 3: Upload JAR
        print("Step 3: Uploading JAR file (~100MB)...")
        jar_size = os.path.getsize(f"{BASE_DIR}/target/disaster-reduction-evaluation-1.0.0.jar") / (1024 * 1024)
        print(f"  JAR size: {jar_size:.1f} MB")
        sftp_client = ssh_client.open_sftp()
        sftp_client.put(
            f"{BASE_DIR}/target/disaster-reduction-evaluation-1.0.0.jar",
            f"{remote_dir}/target/disaster-reduction-evaluation-1.0.0.jar",
            callback=lambda x: None  # Suppress progress
        )
        print("  Done.")
        print()

        # Step 4: Upload frontend files
        print("Step 4: Uploading frontend files...")
        # First, clear remote dist directory
        execute_command(ssh_client, f"rm -rf {remote_dir}/frontend/dist/*", show_output=False)

        # Upload dist contents
        dist_dir = f"{BASE_DIR}/frontend/dist"
        for item in os.listdir(dist_dir):
            local_path = os.path.join(dist_dir, item)
            remote_path = f"{remote_dir}/frontend/dist/{item}"
            upload_file(sftp_client, local_path, remote_path)

        # Upload frontend config files
        print("  Uploading frontend config...")
        sftp_client.put(f"{BASE_DIR}/frontend/Dockerfile", f"{remote_dir}/frontend/Dockerfile")
        sftp_client.put(f"{BASE_DIR}/frontend/nginx.conf", f"{remote_dir}/frontend/nginx.conf")
        print("  Done.")
        print()

        # Step 5: Upload docker files
        print("Step 5: Uploading docker configuration...")
        sftp_client.put(f"{BASE_DIR}/docker-compose.prod.yml", f"{remote_dir}/docker-compose.prod.yml")
        sftp_client.put(f"{BASE_DIR}/Dockerfile.prod", f"{remote_dir}/Dockerfile.prod")
        print("  Done.")
        print()

        # Close SFTP
        sftp_client.close()

        # Step 6: Build and start containers
        print("Step 6: Building and starting containers...")
        print("  This may take several minutes...")
        execute_command(ssh_client, f"cd {remote_dir} && docker compose -f docker-compose.prod.yml up -d --build", show_output=False)
        print("  Done.")
        print()

        # Wait for containers to start
        print("Step 7: Waiting for containers to start...")
        time.sleep(10)

        # Step 8: Check status
        print("Step 8: Checking deployment status...")
        print()
        execute_command(ssh_client, f"cd {remote_dir} && docker compose ps")

        print()
        print("=" * 60)
        print("  Deployment completed!")
        print("=" * 60)
        print()
        print(f"Frontend URL: http://{SERVER}:8080")
        print(f"Backend URL:  http://{SERVER}:8081")
        print()

        ssh_client.close()

    except Exception as e:
        print(f"Error: {e}")
        import traceback
        traceback.print_exc()
        ssh_client.close()
        sys.exit(1)

if __name__ == "__main__":
    main()
