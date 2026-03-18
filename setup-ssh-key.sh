#!/bin/bash
# Setup SSH key for passwordless login

echo "========================================"
echo "  SSH Key Setup for 172.16.41.139"
echo "========================================"
echo ""
echo "Please run the following command manually:"
echo ""
echo "1. Copy your public key:"
cat ~/.ssh/id_rsa.pub
echo ""
echo ""
echo "2. Then SSH to the server and add the key:"
echo "   ssh htht@172.16.41.139"
echo "   mkdir -p ~/.ssh"
echo "   chmod 700 ~/.ssh"
echo "   echo 'PASTE_PUBLIC_KEY_HERE' >> ~/.ssh/authorized_keys"
echo "   chmod 600 ~/.ssh/authorized_keys"
echo ""
echo "========================================"
