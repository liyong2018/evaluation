#!/usr/bin/env python3
"""
Setup SSH key authentication for passwordless login
Run this once to enable passwordless SSH deployment
"""

import paramiko
import sys

SERVER = "172.16.41.139"
USER = "htht"
PASSWORD = "1qaz@1234"
PUBLIC_KEY = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQDAW/+RzD6/LxUKtnmbF3JR00HHdKkjFjKtMQiqPjV5oFnqzctKp8lfa6BeFNuN6IeOE1KbJTSlZ3jwQuue4Y6mLQjCUX1uzM/NIo0sRUFboVTXrSV9uTO/9Zt4j46dg12rPXJWWyshKnxjMsd+QIluW1zCpUYox4vTofv3BnAjcO4+oRNqamHA51k6VzJ4XqxEi5WYEDmd4Cva8RiENlGkCC3bN5MfaZX8Sir94v8O+sz7vK4+4DspvoJE01PCPIG50XMaK+k4tfFoB4feC2oaE7a5gyWs7KfUwK3zEoquJ1snMir3W3aH6gNrWO4lcestDvh37MJWBoX7fEoFJJinfdf/UoaGXFmU0tRfh0esum5Crr8hmawU0UbdxoiWwhzMkDMWH2GuA90gL6Q+1NjoutH5g8dHedO9eJiV/JVtsoNk059V1vCzncmEEtueJH5Zpyu9UXe0oqDQe/mH2cyjELy5AnG8bEOMTeXtX73QYAttvs6BNmATSUONCHbaKdM= admin@DESKTOP-KC2OIL3"

def main():
    print("=" * 60)
    print("  Setting up SSH Key Authentication")
    print("=" * 60)
    print()

    # Connect to server
    print(f"Connecting to {SERVER}...")
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())

    try:
        client.connect(SERVER, username=USER, password=PASSWORD, timeout=30)
        print("Connected successfully!")
        print()

        # Setup .ssh directory
        print("Setting up .ssh directory...")
        commands = [
            "mkdir -p ~/.ssh",
            "chmod 700 ~/.ssh",
            "touch ~/.ssh/authorized_keys",
            "chmod 600 ~/.ssh/authorized_keys",
        ]

        for cmd in commands:
            stdin, stdout, stderr = client.exec_command(cmd)
            stdout.read()

        # Add public key
        print("Adding public key to authorized_keys...")
        add_key_cmd = f'echo "{PUBLIC_KEY}" >> ~/.ssh/authorized_keys'
        stdin, stdout, stderr = client.exec_command(add_key_cmd)
        stdout.read()

        # Verify
        print("Verifying...")
        stdin, stdout, stderr = client.exec_command("cat ~/.ssh/authorized_keys")
        keys = stdout.read().decode()

        if PUBLIC_KEY in keys:
            print()
            print("=" * 60)
            print("  SSH Key Setup Successful!")
            print("=" * 60)
            print()
            print("You can now run passwordless deployment:")
            print("  python d:/Evaluation/evaluation/scripts/deploy-139.py")
            print()
        else:
            print("Warning: Key may not have been added properly.")
            sys.exit(1)

        client.close()

    except paramiko.AuthenticationException:
        print("Authentication failed! Password may be incorrect.")
        sys.exit(1)
    except Exception as e:
        print(f"Error: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)

if __name__ == "__main__":
    main()
