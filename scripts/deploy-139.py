#!/usr/bin/env python3
"""
Deployment script for 172.16.41.139
Automates SSH/SCP with password authentication
"""

import os
import sys
import paramiko
import scp
import glob

# Configuration
SERVER = "172.16.41.139"
USER = "htht"
PASSWORD = "1qaz@1234"
REMOTE_DIR = "~/evaluation"
BASE_DIR = "d:/Evaluation/evaluation"

def create_ssh_client():
    """Create and return SSH client"""
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    client.connect(SERVER, username=USER, password=PASSWORD, timeout=30)
    return client

def execute_command(ssh_client, command):
    """Execute SSH command and print output"""
    stdin, stdout, stderr = ssh_client.exec_command(command)
    output = stdout.read().decode()
    error = stderr.read().decode()
    if output:
        print(output.strip())
    if error:
        print(error.strip(), file=sys.stderr)
    return output

def upload_file(scp_client, local_path, remote_path):
    """Upload file via SCP"""
    print(f"Uploading: {local_path} -> {remote_path}")
    scp_client.put(local_path, remote_path, recursive=True)

def main():
    print("=" * 50)
    print("  Deploying to Remote Server")
    print(f"  Server: {SERVER}")
    print("=" * 50)
    print()

    remote_dir = REMOTE_DIR  # Use local variable

    try:
        # Create SSH client
        print("Connecting to server...")
        ssh_client = create_ssh_client()

        # Expand remote directory
        stdin, stdout, stderr = ssh_client.exec_command(f"echo {remote_dir}")
        remote_dir = stdout.read().decode().strip()

        # Step 1: Stop containers
        print("\nStep 1: Stopping Docker containers...")
        execute_command(ssh_client, f"cd {remote_dir} && docker compose down 2>/dev/null || true")

        # Step 2: Create directories
        print("\nStep 2: Creating directories...")
        execute_command(ssh_client, f"mkdir -p {remote_dir}/target")
        execute_command(ssh_client, f"mkdir -p {remote_dir}/frontend/dist")

        # Step 3: Upload JAR
        print("\nStep 3: Uploading JAR file (~100MB)...")
        with scp.SCPClient(ssh_client.get_transport()) as scp_client:
            scp_client.put(
                f"{BASE_DIR}/target/disaster-reduction-evaluation-1.0.0.jar",
                f"{remote_dir}/target/disaster-reduction-evaluation-1.0.0.jar"
            )

        # Step 4: Upload frontend files
        print("\nStep 4: Uploading frontend files...")
        with scp.SCPClient(ssh_client.get_transport()) as scp_client:
            # Upload dist directory
            dist_dir = f"{BASE_DIR}/frontend/dist"
            if os.path.exists(dist_dir):
                for item in os.listdir(dist_dir):
                    local_path = os.path.join(dist_dir, item)
                    remote_path = f"{remote_dir}/frontend/dist/{item}"
                    if os.path.isdir(local_path):
                        # Create remote directory first
                        execute_command(ssh_client, f"mkdir -p {remote_path}")
                        # Upload directory contents
                        for root, dirs, files in os.walk(local_path):
                            for file in files:
                                file_local = os.path.join(root, file)
                                file_remote = f"{remote_path}/{file}"
                                try:
                                    scp_client.put(file_local, file_remote)
                                except Exception as e:
                                    print(f"Warning: Could not upload {file_local}: {e}")
                    else:
                        scp_client.put(local_path, remote_path)

            # Upload frontend config files
            scp_client.put(f"{BASE_DIR}/frontend/Dockerfile", f"{remote_dir}/frontend/Dockerfile")
            scp_client.put(f"{BASE_DIR}/frontend/nginx.conf", f"{remote_dir}/frontend/nginx.conf")

        # Step 5: Upload docker files
        print("\nStep 5: Uploading docker configuration...")
        with scp.SCPClient(ssh_client.get_transport()) as scp_client:
            scp_client.put(f"{BASE_DIR}/docker-compose.prod.yml", f"{remote_dir}/docker-compose.prod.yml")
            scp_client.put(f"{BASE_DIR}/Dockerfile.prod", f"{remote_dir}/Dockerfile.prod")

        # Step 6: Build and start containers
        print("\nStep 6: Building and starting containers...")
        execute_command(ssh_client, f"cd {remote_dir} && docker compose -f docker-compose.prod.yml up -d --build")

        # Wait a bit for containers to start
        import time
        time.sleep(5)

        # Step 7: Check status
        print("\nStep 7: Checking deployment status...")
        execute_command(ssh_client, f"cd {remote_dir} && docker compose ps")

        print("\n" + "=" * 50)
        print("  Deployment completed!")
        print("=" * 50)

        ssh_client.close()

    except paramiko.AuthenticationException:
        print("Authentication failed. Please check credentials.")
        sys.exit(1)
    except paramiko.SSHException as e:
        print(f"SSH error: {e}")
        sys.exit(1)
    except Exception as e:
        print(f"Error: {e}")
        sys.exit(1)

if __name__ == "__main__":
    # Check if paramiko is installed
    try:
        import paramiko
        import scp
    except ImportError:
        print("Installing required packages...")
        os.system("pip install paramiko scp")
        print("Please run the script again.")
        sys.exit(1)

    main()
