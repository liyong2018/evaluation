#!/bin/bash

# SSH密钥认证配置脚本
# 使用方法: ./setup_ssh.sh 101.126.46.254

SERVER_IP=$1
if [ -z "$SERVER_IP" ]; then
    echo "使用方法: $0 <服务器IP>"
    echo "示例: $0 101.126.46.254"
    exit 1
fi

echo "正在为服务器 $SERVER_IP 配置SSH密钥认证..."

# 1. 确保本地有SSH密钥
if [ ! -f ~/.ssh/id_rsa ]; then
    echo "生成SSH密钥..."
    ssh-keygen -t rsa -b 4096 -f ~/.ssh/id_rsa -N ""
fi

# 2. 创建SSH配置
cat > ~/.ssh/config << EOF
Host $SERVER_IP
    HostName $SERVER_IP
    User root
    Port 22
    IdentityFile ~/.ssh/id_rsa_evaluation
    PreferredAuthentications publickey
    StrictHostKeyChecking no
    ServerAliveInterval 60
    ServerAliveCountMax 3
EOF

echo "SSH配置已创建"

# 3. 自动复制公钥到服务器
echo "正在复制公钥到服务器..."
ssh-copy-id -i ~/.ssh/id_rsa.pub root@$SERVER_IP

if [ $? -eq 0 ]; then
    echo "✅ SSH密钥配置成功！"
    echo "现在可以使用 'ssh $SERVER_IP' 无密码登录"
else
    echo "❌ SSH密钥配置失败，请检查密码是否正确"
    echo "您可以手动执行以下命令："
    echo "ssh-copy-id -i ~/.ssh/id_rsa.pub root@$SERVER_IP"
fi