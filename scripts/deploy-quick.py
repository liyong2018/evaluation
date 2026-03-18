#!/usr/bin/env python3
"""
Quick deployment - replace files and restart containers
No docker build required
"""

import os
import sys
import paramiko
import subprocess
import time

SERVER = "172.16.41.139"
USER = "htht"
REMOTE_DIR = "~/evaluation"
BASE_DIR = r"D:\Evaluation\evaluation"

def run_ssh(command):
    """Run SSH command using system ssh (key-based auth)"""
    cmd = f'ssh -o StrictHostKeyChecking=no {USER}@{SERVER} "{command}"'
    result = subprocess.run(cmd, shell=True, capture_output=True, text=True, timeout=120)
    return result.returncode, result.stdout, result.stderr

def run_scp(source, dest):
    """Run SCP command using system scp (key-based auth)"""
    cmd = f'scp -o StrictHostKeyChecking=no -r "{source}" {USER}@{SERVER}:{dest}'
    result = subprocess.run(cmd, shell=True, capture_output=True, text=True, timeout=300)
    return result.returncode, result.stdout, result.stderr

def main():
    print("=" * 50)
    print("  Quick Deploy to 172.16.41.139")
    print("=" * 50)
    print()

    # Step 1: Upload JAR
    print("Step 1: Uploading JAR (~100MB)...")
    rc, out, err = run_scp(f"{BASE_DIR}\\target\\disaster-reduction-evaluation-1.0.0.jar", f"{REMOTE_DIR}/target/")
    if rc == 0:
        print("  Done")
    else:
        print(f"  Error: {err}")
        return

    # Step 2: Upload frontend
    print("Step 2: Uploading frontend...")
    run_ssh(f"rm -rf {REMOTE_DIR}/frontend/dist/*")
    run_ssh(f"mkdir -p {REMOTE_DIR}/frontend/dist")

    # Upload dist contents
    dist_dir = f"{BASE_DIR}\\frontend\\dist"
    for item in os.listdir(dist_dir):
        local_path = os.path.join(dist_dir, item)
        if os.path.isfile(local_path):
            run_scp(local_path, f"{REMOTE_DIR}/frontend/dist/")

    print("  Done")

    # Step 3: Restart containers
    print("Step 3: Restarting containers...")
    run_ssh(f"cd {REMOTE_DIR} && docker compose restart")
    time.sleep(5)

    # Step 4: Check status
    print("Step 4: Checking status...")
    rc, out, err = run_ssh(f"cd {REMOTE_DIR} && docker compose ps")
    print(out)

    print()
    print("=" * 50)
    print("  Deploy complete!")
    print("=" * 50)
    print(f"  http://{SERVER}:8080")

if __name__ == "__main__":
    main()
