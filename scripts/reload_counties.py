#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
重新加载区县级数据到 organization 表
从参考文件读取数据并插入数据库
"""

import mysql.connector
import re

# 数据库连接配置
db_config = {
    'host': '127.0.0.1',
    'port': 30314,
    'user': 'root',
    'password': '123456',
    'database': 'evaluate_db',
    'charset': 'utf8mb4'
}

# 参考文件路径
ref_file = 'd:/Evaluation/evaluation/frontend/public/sichuan_province_city_county.txt'

# 从参考文件解析组织结构
def parse_reference_file(filepath):
    """解析参考文件，返回省市区县结构"""
    result = []
    current_city = None

    with open(filepath, 'r', encoding='utf-8') as f:
        for line in f:
            line = line.rstrip('\n\r')
            if not line.strip():
                continue

            # 计算缩进级别（使用空格数判断）
            indent = len(line) - len(line.lstrip())
            content = line.strip()

            # 分割名称和代码
            parts = content.split('\t')
            if len(parts) < 2:
                continue

            name = parts[0]
            code = parts[1]

            # 根据缩进判断级别
            if indent == 0:  # 省级
                pass  # 省级已插入
            elif indent == 2:  # 市级（2个空格）
                current_city = {'name': name, 'code': code}
            elif indent == 4:  # 区县级（4个空格）
                if current_city:
                    result.append({
                        'name': name,
                        'code': code,
                        'city_code': current_city['code'],
                        'city_name': current_city['name']
                    })

    return result

# 插入区县数据
def insert_counties(counties):
    """插入区县数据到数据库"""
    conn = mysql.connector.connect(**db_config)
    cursor = conn.cursor()

    # 获取各市的ID
    cursor.execute("SELECT code, id FROM organization WHERE level = 2")
    city_ids = {row[0]: row[1] for row in cursor.fetchall()}

    # 获取省ID
    cursor.execute("SELECT id FROM organization WHERE level = 1")
    province_id = cursor.fetchone()[0]

    inserted = 0
    for county in counties:
        city_code = county['city_code']
        # 参考文件中城市代码是4位，需要匹配数据库中的格式
        parent_id = city_ids.get(city_code)

        if parent_id is None:
            print(f"Warning: Cannot find parent for {county['name']} ({county['code']}) with city_code={city_code}")
            continue

        sql = """
        INSERT INTO organization (code, name, level, year, data_source, province_name, city_name, county_name, parent_id, is_baseline)
        VALUES (%s, %s, 3, 2020, 'BASELINE', '四川省', %s, %s, %s, 1)
        """
        try:
            cursor.execute(sql, (
                county['code'],
                county['name'],
                county['city_name'],
                county['name'],
                parent_id
            ))
            inserted += 1
        except mysql.connector.Error as e:
            print(f"Error inserting {county['name']} ({county['code']}): {e}")

    conn.commit()
    cursor.close()
    conn.close()

    return inserted

def main():
    print("解析参考文件...")
    counties = parse_reference_file(ref_file)
    print(f"找到 {len(counties)} 个区县")

    print("\n前10个区县：")
    for county in counties[:10]:
        print(f"  {county['name']} - {county['code']} - {county['city_name']} ({county['city_code']})")

    print(f"\n插入区县数据...")
    inserted = insert_counties(counties)
    print(f"成功插入 {inserted} 个区县")

if __name__ == '__main__':
    main()
