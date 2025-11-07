#!/usr/bin/env python3
"""
MySQL to PostgreSQL 数据转换脚本
将 mysqldump 导出的数据转换为 PostgreSQL 可用的格式
"""
import re
import sys

def convert_mysql_to_pg(input_file, output_file):
    with open(input_file, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # 1. 移除反引号
    content = content.replace('`', '')
    
    # 2. 转换 tinyint(1) 为 boolean (用于表结构)
    # content = re.sub(r'tinyint\(1\)', 'boolean', content)
    
    # 3. 转换 CURRENT_TIMESTAMP
    content = content.replace('CURRENT_TIMESTAMP', 'now()')
    
    # 4. 处理 auto_increment (移除用于表结构)
    content = re.sub(r'auto_increment', '', content)
    
    # 5. 处理 UNSIGNED
    content = re.sub(r'\bUNSIGNED\b', '', content)
    
    # 6. 处理 ENGINE=InnoDB
    content = re.sub(r'ENGINE=\w+', '', content)
    
    # 7. 处理 COLLATE
    content = re.sub(r'COLLATE=\w+', '', content)
    
    # 8. 转换 timestamp 字段类型
    content = re.sub(r'timestamp\s+YES', 'timestamptz', content)
    content = re.sub(r'timestamp\s+NO', 'timestamptz', content)
    
    # 9. 处理长文本字段 (LONGTEXT -> text)
    content = re.sub(r'LONGTEXT', 'text', content)
    content = re.sub(r'VARCHAR\(([0-9]+)\)', r'varchar(\1)', content)
    
    # 10. 处理 DEFAULT CHARSET
    content = re.sub(r'DEFAULT CHARSET=\w+', '', content)
    
    # 11. 转换 INSERT 语句
    # 移除表名前的数据库名 (db.table -> table)
    content = re.sub(r'INSERT INTO `?\w+`?\.', 'INSERT INTO', content)
    
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(content)
    
    print(f"转换完成: {output_file}")

if __name__ == '__main__':
    if len(sys.argv) < 3:
        print("用法: python convert_mysql_to_pg.py <输入文件> <输出文件>")
        sys.exit(1)
    
    input_file = sys.argv[1]
    output_file = sys.argv[2]
    convert_mysql_to_pg(input_file, output_file)
