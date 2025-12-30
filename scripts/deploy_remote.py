#!/usr/bin/env python3
"""
Remote deployment script for Evaluation System
Uses SSH with password authentication
"""
import paramiko
import os
import sys
from pathlib import Path

# Configuration
SERVER = "172.16.43.189"
USER = "htht"
PASSWORD = "1qaz@1234"
REMOTE_DIR = "/home/htht/evaluation"
LOCAL_DIR = r"D:\Evaluation\evaluation"

def create_ssh_client():
    """Create and return SSH client"""
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    client.connect(SERVER, username=USER, password=PASSWORD)
    return client

def exec_command(ssh, command):
    """Execute command on remote server"""
    stdin, stdout, stderr = ssh.exec_command(command)
    output = stdout.read().decode()
    error = stderr.read().decode()
    if error and "Warning" not in error:
        print(f"Error: {error}")
    return output

def upload_file(sftp, local_path, remote_path):
    """Upload file via SFTP"""
    sftp.put(local_path, remote_path)
    print(f"Uploaded: {local_path} -> {remote_path}")

def upload_directory(sftp, local_dir, remote_dir):
    """Upload directory recursively"""
    local_path = Path(local_dir)
    for item in local_path.rglob("*"):
        if item.is_file():
            rel_path = item.relative_to(local_path)
            remote_file = f"{remote_dir}/{rel_path.as_posix()}"

            # Create remote directory
            remote_dir_path = os.path.dirname(remote_file)
            try:
                sftp.stat(remote_dir_path)
            except FileNotFoundError:
                sftp.mkdir(remote_dir_path)

            upload_file(sftp, str(item), remote_file)

def main():
    print("=" * 50)
    print("Deploying Evaluation System to", SERVER)
    print("=" * 50)

    try:
        # Create SSH client
        print("Connecting to server...")
        ssh = create_ssh_client()
        sftp = ssh.open_sftp()

        # Create directories
        print("Creating directories...")
        exec_command(ssh, f"mkdir -p {REMOTE_DIR}/frontend/dist")
        exec_command(ssh, f"mkdir -p {REMOTE_DIR}/nginx/conf.d")
        exec_command(ssh, f"mkdir -p {REMOTE_DIR}/logs")
        exec_command(ssh, f"mkdir -p {REMOTE_DIR}/uploads")
        exec_command(ssh, f"mkdir -p {REMOTE_DIR}/mysql/conf.d")
        print("Directories created")

        # Upload docker-compose.yml
        print("Uploading docker-compose.yml...")
        upload_file(sftp, f"{LOCAL_DIR}/docker-compose.yml", f"{REMOTE_DIR}/docker-compose.yml")

        # Upload Dockerfile
        print("Uploading Dockerfile...")
        upload_file(sftp, f"{LOCAL_DIR}/Dockerfile", f"{REMOTE_DIR}/Dockerfile")

        # Upload nginx config
        print("Uploading nginx configuration...")
        upload_file(sftp, f"{LOCAL_DIR}/nginx/conf.d/default.conf", f"{REMOTE_DIR}/nginx/conf.d/default.conf")

        # Upload frontend dist
        print("Uploading frontend dist files...")
        frontend_dist = Path(f"{LOCAL_DIR}/frontend/dist")
        if frontend_dist.exists():
            for item in frontend_dist.rglob("*"):
                if item.is_file():
                    rel_path = item.relative_to(frontend_dist)
                    remote_file = f"{REMOTE_DIR}/frontend/dist/{rel_path.as_posix()}"
                    try:
                        sftp.stat(os.path.dirname(remote_file))
                    except FileNotFoundError:
                        sftp.mkdir(os.path.dirname(remote_file))
                    upload_file(sftp, str(item), remote_file)

        print("\n" + "=" * 50)
        print("Files uploaded successfully!")
        print("=" * 50)

        sftp.close()
        ssh.close()

    except Exception as e:
        print(f"Error: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()
