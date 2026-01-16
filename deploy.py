#!/usr/bin/env python3
"""
Deploy to remote server using paramiko
"""
import paramiko
import os
import sys

PASSWORD = "Htht@12#$"
SERVER = "172.16.43.189"
USERNAME = "root"
REMOTE_DIR = "~/evaluation"
LOCAL_DIR = "d:/Evaluation/evaluation"

def exec_command(ssh, command):
    """Execute command on remote server"""
    print(f"Executing: {command}")
    stdin, stdout, stderr = ssh.exec_command(command)
    exit_status = stdout.channel.recv_exit_status()
    output = stdout.read().decode()
    error = stderr.read().decode()
    if exit_status != 0:
        print(f"Error: {error}")
    else:
        if output:
            print(output)
    return exit_status == 0

def sftp_upload(sftp, local_path, remote_path):
    """Upload file via SFTP"""
    print(f"Uploading {local_path} -> {remote_path}")
    sftp.put(local_path, remote_path)

def main():
    # Create SSH client
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())

    try:
        # Connect to server
        print(f"Connecting to {SERVER}...")
        ssh.connect(SERVER, username=USERNAME, password=PASSWORD)
        print("Connected successfully!")

        # Create SFTP client
        sftp = ssh.open_sftp()

        # Stop Docker containers
        print("\nStopping Docker containers...")
        exec_command(ssh, f"cd {REMOTE_DIR} && docker compose down")

        # Upload JAR file
        print("\nUploading JAR file...")
        jar_path = f"{LOCAL_DIR}/target/disaster-reduction-evaluation-1.0.0.jar"
        remote_jar = f"{REMOTE_DIR}/target/disaster-reduction-evaluation-1.0.0.jar"
        sftp_upload(sftp, jar_path, remote_jar)

        # Upload frontend files
        print("\nUploading frontend files...")
        local_dist = f"{LOCAL_DIR}/frontend/dist"
        remote_dist = f"{REMOTE_DIR}/frontend/dist"

        # Create remote dist directory
        exec_command(ssh, f"mkdir -p {remote_dist}")

        # Upload all files from dist
        for root, dirs, files in os.walk(local_dist):
            for file in files:
                local_file = os.path.join(root, file)
                rel_path = os.path.relpath(local_file, local_dist)
                remote_file = f"{remote_dist}/{rel_path}"

                # Create remote directory if needed
                remote_dir = os.path.dirname(remote_file)
                exec_command(ssh, f"mkdir -p {remote_dir}")

                sftp_upload(sftp, local_file, remote_file)

        sftp.close()

        # Rebuild and start Docker containers
        print("\nRebuilding Docker image...")
        exec_command(ssh, f"cd {REMOTE_DIR} && docker compose build evaluation-app")

        print("\nStarting Docker containers...")
        exec_command(ssh, f"cd {REMOTE_DIR} && docker compose up -d")

        print("\nDeployment completed successfully!")

    except Exception as e:
        print(f"Error: {e}")
        sys.exit(1)
    finally:
        ssh.close()

if __name__ == "__main__":
    main()
