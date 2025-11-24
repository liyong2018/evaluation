#!/usr/bin/expect -f

# 减灾能力评估系统Docker部署脚本（密码认证版本）
# 使用expect自动处理SSH密码输入

set timeout 30

# 配置变量
set SERVER "101.126.46.254"
set PASSWORD "Htht@12#$"
set REMOTE_DIR "/opt/evaluation"
set PROJECT_NAME "evaluation-system"

# 颜色定义
spawn sh -c {echo -e "\033[0;32m=== 减灾能力评估系统Docker部署脚本 ===\033[0m"}
expect eof

spawn sh -c {echo -e "\033[1;33m目标服务器: $SERVER\033[0m"}
expect eof

spawn sh -c {echo -e "\033[1;33m部署目录: $REMOTE_DIR\033[0m"}
expect eof

spawn sh -c {echo ""}
expect eof

# 1. 检查本地文件
spawn sh -c {echo -e "\033[0;32m1. 检查本地部署文件...\033[0m"}
expect eof

if {[file exists "docker-compose.yml"] == 0} {
    spawn sh -c {echo -e "\033[0;31m错误: docker-compose.yml 文件不存在\033[0m"}
    exit 1
}

if {[file exists "Dockerfile"] == 0} {
    spawn sh -c {echo -e "\033[0;31m错误: Dockerfile 文件不存在\033[0m"}
    exit 1
}

spawn sh -c {echo -e "\033[0;32m✓ 本地文件检查完成\033[0m"}
expect eof

# 2. 测试SSH连接
spawn sh -c {echo -e "\033[0;32m2. 测试SSH连接...\033[0m"}
expect eof

spawn ssh -o StrictHostKeyChecking=no root@$SERVER "echo 'SSH连接成功'"
expect {
    "password:" {
        send "$PASSWORD\r"
        expect "SSH连接成功"
        spawn sh -c {echo -e "\033[0;32m✓ SSH连接正常\033[0m"}
        expect eof
    }
    "SSH连接成功" {
        spawn sh -c {echo -e "\033[0;32m✓ SSH连接正常\033[0m"}
        expect eof
    }
    timeout {
        spawn sh -c {echo -e "\033[0;31m错误: SSH连接超时\033[0m"}
        exit 1
    }
    eof {
        spawn sh -c {echo -e "\033[0;31m错误: SSH连接失败\033[0m"}
        exit 1
    }
}

# 3. 在服务器上创建部署目录
spawn sh -c {echo -e "\033[0;32m3. 在服务器上创建部署目录...\033[0m"}
expect eof

spawn ssh -o StrictHostKeyChecking=no root@$SERVER "mkdir -p $REMOTE_DIR/{logs,uploads,backups}"
expect {
    "password:" {
        send "$PASSWORD\r"
        expect eof
    }
    eof {}
}

spawn sh -c {echo -e "\033[0;32m✓ 部署目录创建完成\033[0m"}
expect eof

# 4. 检查Docker环境
spawn sh -c {echo -e "\033[0;32m4. 检查服务器Docker环境...\033[0m"}
expect eof

spawn ssh -o StrictHostKeyChecking=no root@$SERVER "docker --version"
expect {
    "password:" {
        send "$PASSWORD\r"
        expect {
            "Docker version" {
                spawn sh -c {echo -e "\033[0;32m✓ Docker已安装\033[0m"}
                expect eof
            }
            "command not found" {
                spawn sh -c {echo -e "\033[1;33m警告: Docker未安装，正在安装...\033[0m"}
                expect eof

                # 安装Docker
                spawn ssh -o StrictHostKeyChecking=no root@$SERVER "curl -fsSL https://get.docker.com | sh"
                expect {
                    "password:" {
                        send "$PASSWORD\r"
                        expect eof
                    }
                    eof {}
                }

                spawn ssh -o StrictHostKeyChecking=no root@$SERVER "systemctl start docker && systemctl enable docker"
                expect {
                    "password:" {
                        send "$PASSWORD\r"
                        expect eof
                    }
                    eof {}
                }
            }
        }
    }
    "Docker version" {
        spawn sh -c {echo -e "\033[0;32m✓ Docker已安装\033[0m"}
        expect eof
    }
    "command not found" {
        spawn sh -c {echo -e "\033[1;33m警告: Docker未安装，请手动安装\033[0m"}
        expect eof
    }
    eof {}
}

# 检查Docker Compose
spawn ssh -o StrictHostKeyChecking=no root@$SERVER "docker-compose --version"
expect {
    "password:" {
        send "$PASSWORD\r"
        expect {
            "docker-compose version" {
                spawn sh -c {echo -e "\033[0;32m✓ Docker Compose已安装\033[0m"}
                expect eof
            }
            "command not found" {
                spawn sh -c {echo -e "\033[1;33m警告: Docker Compose未安装，正在安装...\033[0m"}
                expect eof

                spawn ssh -o StrictHostKeyChecking=no root@$SERVER "curl -L 'https://github.com/docker/compose/releases/download/v2.20.2/docker-compose-\$(uname -s)-\$(uname -m)' -o /usr/local/bin/docker-compose && chmod +x /usr/local/bin/docker-compose"
                expect {
                    "password:" {
                        send "$PASSWORD\r"
                        expect eof
                    }
                    eof {}
                }
            }
        }
    }
    eof {}
}

spawn sh -c {echo -e "\033[0;32m✓ Docker环境检查完成\033[0m"}
expect eof

# 5. 停止现有服务
spawn sh -c {echo -e "\033[0;32m5. 停止现有服务...\033[0m"}
expect eof

spawn ssh -o StrictHostKeyChecking=no root@$SERVER "cd $REMOTE_DIR && if [ -f 'docker-compose.yml' ]; then docker-compose down --remove-orphans || true; fi"
expect {
    "password:" {
        send "$PASSWORD\r"
        expect eof
    }
    eof {}
}

spawn sh -c {echo -e "\033[0;32m✓ 现有服务已停止\033[0m"}
expect eof

# 6. 上传文件
spawn sh -c {echo -e "\033[0;32m6. 上传项目文件...\033[0m"}
expect eof

# 使用rsync上传文件
spawn rsync -avz --delete --exclude='.git' --exclude='node_modules' --exclude='target' --exclude='logs/*' --exclude='uploads/*' --exclude='*.log' ./ root@$SERVER:$REMOTE_DIR/
expect {
    "password:" {
        send "$PASSWORD\r"
        expect eof
    }
    eof {}
}

spawn sh -c {echo -e "\033[0;32m✓ 文件上传完成\033[0m"}
expect eof

# 7. 构建和启动服务
spawn sh -c {echo -e "\033[0;32m7. 构建和启动服务...\033[0m"}
expect eof

spawn ssh -o StrictHostKeyChecking=no root@$SERVER "cd $REMOTE_DIR && docker-compose build --no-cache && docker-compose up -d"
expect {
    "password:" {
        send "$PASSWORD\r"
        expect eof
    }
    eof {}
}

spawn sh -c {echo -e "\033[0;32m✓ 服务启动完成\033[0m"}
expect eof

# 8. 等待服务启动
spawn sh -c {echo -e "\033[0;32m8. 等待服务启动...\033[0m"}
expect eof
spawn sh -c {sleep 30}
expect eof

# 9. 检查服务状态
spawn sh -c {echo -e "\033[0;32m9. 检查服务状态...\033[0m"}
expect eof

spawn sh -c {echo ""}
expect eof

spawn ssh -o StrictHostKeyChecking=no root@$SERVER "cd $REMOTE_DIR && echo '=== Docker容器状态 ===' && docker-compose ps"
expect {
    "password:" {
        send "$PASSWORD\r"
        expect eof
    }
    eof {}
}

# 健康检查
spawn sh -c {echo -e "\033[0;32m=== 服务健康检查 ===\033[0m"}
expect eof

spawn ssh -o StrictHostKeyChecking=no root@$SERVER "for i in {1..10}; do if curl -f http://localhost:8087/actuator/health >/dev/null 2>&1; then echo '应用服务正常 ✓'; break; else echo -n '.'; sleep 5; fi; if [ \$i -eq 10 ]; then echo ''; echo '应用服务可能未正常启动，请检查日志'; fi; done"
expect {
    "password:" {
        send "$PASSWORD\r"
        expect eof
    }
    eof {}
}

# 显示部署信息
spawn sh -c {echo ""}
expect eof
spawn sh -c {echo -e "\033[0;32m=== 部署完成 ===\033[0m"}
expect eof
spawn sh -c {echo -e "\033[1;33m应用访问地址: http://101.126.46.254:8087\033[0m"}
expect eof
spawn sh -c {echo ""}
expect eof
spawn sh -c {echo -e "\033[0;32m常用命令:\033[0m"}
expect eof
spawn sh -c {echo -e "查看日志: ssh root@$SERVER 'cd $REMOTE_DIR && docker-compose logs -f'"}
expect eof
spawn sh -c {echo -e "重启服务: ssh root@$SERVER 'cd $REMOTE_DIR && docker-compose restart'"}
expect eof
spawn sh -c {echo -e "停止服务: ssh root@$SERVER 'cd $REMOTE_DIR && docker-compose down'"}
expect eof
spawn sh -c {echo ""}
expect eof
spawn sh -c {echo -e "\033[0;32m部署成功！\033[0m"}
expect eof