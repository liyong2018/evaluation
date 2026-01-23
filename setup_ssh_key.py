#!/usr/bin/env python3
"""
Setup SSH key authentication for passwordless login
"""
import paramiko
import sys

PASSWORD = "Htht@12#$"
SERVER = "172.16.43.189"
USERNAME = "htht"
LOCAL_PUBLIC_KEY = "C:/Users/admin/.ssh/id_rsa.pub"

def setup_ssh_key():
    # Read the public key
    with open(LOCAL_PUBLIC_KEY, 'r') as f:
        public_key = f.read().strip()

    # Create SSH client
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())

    try:
        print(f"Connecting to {SERVER} as {USERNAME}...")
        ssh.connect(SERVER, username=USERNAME, password=PASSWORD)
        print("Connected successfully!")

        # Create .ssh directory if it doesn't exist
        print("Setting up SSH key...")
        stdin, stdout, stderr = ssh.exec_command("mkdir -p ~/.ssh && chmod 700 ~/.ssh")
        stdout.channel.recv_exit_status()

        # Add public key to authorized_keys
        command = f'echo "{public_key}" >> ~/.ssh/authorized_keys'
        stdin, stdout, stderr = ssh.exec_command(command)
        stdout.channel.recv_exit_status()

        # Set proper permissions
        stdin, stdout, stderr = ssh.exec_command("chmod 600 ~/.ssh/authorized_keys")
        stdout.channel.recv_exit_status()

        print("SSH key setup completed!")
        print("\nYou can now login without password using:")
        print(f"ssh {USERNAME}@{SERVER}")

    except Exception as e:
        print(f"Error: {e}")
        sys.exit(1)
    finally:
        ssh.close()

if __name__ == "__main__":
    setup_ssh_key()
