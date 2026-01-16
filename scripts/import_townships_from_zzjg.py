#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
从 zzjg.txt 文件提取街道/乡镇数据并插入到 grassroots_organization 表
"""

import mysql.connector
import re
from collections import defaultdict

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
zzjg_file = 'd:/Evaluation/evaluation/frontend/public/zzjg.txt'

def parse_zzjg_file(filepath):
    """
    解析 zzjg.txt 文件，提取街道/乡镇数据

    数据格式: 四川省成都市锦江区锦官驿街道水井坊社区居委会	510104017001
             四川省成都市都江堰市灌口街道南桥社区居民委员会	510181001001
    代码结构: 12位代码 = 6位区县 + 3位乡镇 + 3位社区
             前9位 = 6位区县 + 3位乡镇 = 乡镇代码
    """
    townships = {}  # {(county_code, township_code): township_info}

    with open(filepath, 'r', encoding='utf-8') as f:
        for line_num, line in enumerate(f, 1):
            line = line.strip()
            if not line or line_num == 1:  # 跳过空行和标题行
                continue

            # 分割地址和代码
            parts = line.split('\t')
            if len(parts) < 2:
                continue

            address = parts[0]
            code = parts[1].strip()

            if len(code) < 9:
                continue

            # 提取区县代码（前6位）
            county_code = code[:6]
            # 提取乡镇代码（前9位）
            township_code = code[:9]

            # 从地址中提取各级名称
            # 格式1: 四川省成都市锦江区锦官驿街道水井坊社区居委会
            # 格式2: 四川省成都市都江堰市灌口街道南桥社区居民委员会
            # 格式3: 四川省阿坝藏族羌族自治州阿坝县阿坝镇...  (自治州)

            # 首先去掉"四川省"
            if not address.startswith('四川省'):
                continue
            remaining = address[3:]  # 去掉"四川省"

            # 处理自治州（如"阿坝藏族羌族自治州"）
            city_name = None
            county_name = None
            township_name = None

            # 查找市级名称（地级市或自治州）
            if remaining.startswith('阿坝藏族羌族自治州'):
                city_name = '阿坝藏族羌族自治州'
                remaining = remaining[7:]
            elif remaining.startswith('甘孜藏族自治州'):
                city_name = '甘孜藏族自治州'
                remaining = remaining[7:]
            elif remaining.startswith('凉山彝族自治州'):
                city_name = '凉山彝族自治州'
                remaining = remaining[7:]
            else:
                # 普通地级市，格式: 成都市...
                if '市' in remaining:
                    city_part = remaining.split('市', 1)[0]
                    if city_part:  # 确保不是空的
                        city_name = city_part + '市'
                        remaining = remaining.split('市', 1)[1]

            if city_name is None:
                continue

            # 查找区县名称（可能是区、县、县级市）
            # 注意：需要排除"社区"中的"区"字
            for suffix in ['区', '县']:
                if suffix in remaining:
                    # 查找所有出现位置，排除"社区"、"村民委员会"等
                    idx = remaining.find(suffix)
                    # 检查这个"区"或"县"后面是不是"社"或"民"（表示是"社区"的一部分）
                    is_valid = False
                    while idx != -1:
                        if idx + 1 < len(remaining):
                            next_char = remaining[idx + 1]
                            # 如果后面不是"社"、"民"、"居"、"委"，则认为这是区县结尾
                            if next_char not in ['社', '民', '居', '委']:
                                # 还要确保这不是"社区"中的"区"
                                if idx == 0 or remaining[idx - 1] != '社':
                                    is_valid = True
                                    county_part = remaining[:idx]
                                    county_name = county_part + suffix
                                    remaining = remaining[idx + len(suffix):]
                                    break
                        # 查找下一个位置
                        idx = remaining.find(suffix, idx + 1)
                    if is_valid:
                        break

            # 处理县级市（如都江堰市、彭州市）
            if county_name is None and '市' in remaining:
                # 查找"市"的位置，排除"社区"、"居委会"中的
                idx = remaining.find('市')
                while idx != -1:
                    if idx + 1 < len(remaining):
                        next_char = remaining[idx + 1]
                        # 如果后面不是"社"、"居"、"民"、"委"，则认为这是县级市
                        if next_char not in ['社', '居', '民', '委']:
                            possible_county = remaining[:idx]
                            # 排除掉明显不是区县的名字
                            if possible_county and len(possible_county) >= 2:
                                county_name = possible_county + '市'
                                remaining = remaining[idx + 1:]
                                break
                    # 查找下一个位置
                    idx = remaining.find('市', idx + 1)

            if county_name is None:
                continue

            # 提取乡镇名（街道或镇）
            for suffix in ['街道', '镇', '乡', '民族乡']:
                if suffix in remaining:
                    township_name = remaining.split(suffix)[0] + suffix
                    break

            if township_name is None:
                continue

            key = (county_code, township_code)
            if key not in townships:
                townships[key] = {
                    'code': township_code,
                    'name': township_name,
                    'level': 4,
                    'county_code': county_code,
                    'province_name': '四川省',
                    'city_name': city_name,
                    'county_name': county_name,
                    'township_name': township_name
                }

    return list(townships.values())

def clear_grassroots_table():
    """清空 grassroots_organization 表"""
    conn = mysql.connector.connect(**db_config)
    cursor = conn.cursor()

    # 禁用外键检查
    cursor.execute("SET FOREIGN_KEY_CHECKS = 0")
    cursor.execute("DELETE FROM grassroots_organization")
    cursor.execute("SET FOREIGN_KEY_CHECKS = 1")

    conn.commit()
    cursor.close()
    conn.close()

def insert_townships(townships):
    """插入乡镇数据到 grassroots_organization 表"""
    conn = mysql.connector.connect(**db_config)
    cursor = conn.cursor()

    # 获取区县ID映射
    cursor.execute("SELECT code, id FROM organization WHERE level = 3")
    county_ids = {row[0]: row[1] for row in cursor.fetchall()}

    inserted = 0
    not_found = []

    for township in townships:
        county_code = township['county_code']
        county_id = county_ids.get(county_code)

        if county_id is None:
            not_found.append(f"{township['name']} ({county_code})")
            continue

        sql = """
        INSERT INTO grassroots_organization
        (county_id, code, name, level, year, data_source,
         province_name, city_name, county_name, township_name,
         is_baseline, is_deleted)
        VALUES (%s, %s, %s, %s, 2020, 'ZZJG', %s, %s, %s, %s, 1, 0)
        """

        try:
            cursor.execute(sql, (
                county_id,
                township['code'],
                township['name'],
                township['level'],
                township['province_name'],
                township['city_name'],
                township['county_name'],
                township['township_name']
            ))
            inserted += 1
        except mysql.connector.Error as e:
            print(f"Error inserting {township['name']} ({township['code']}): {e}")

    conn.commit()
    cursor.close()
    conn.close()

    return inserted, not_found

def main():
    print("解析 zzjg.txt 文件...")
    townships = parse_zzjg_file(zzjg_file)
    print(f"找到 {len(townships)} 个唯一的街道/乡镇")

    print("\n前10个乡镇:")
    for t in townships[:10]:
        print(f"  {t['name']} - {t['code']} - {t['county_name']} ({t['county_code']})")

    print(f"\n清空 grassroots_organization 表...")
    clear_grassroots_table()
    print("已清空")

    print(f"\n插入街道/乡镇数据...")
    inserted, not_found = insert_townships(townships)
    print(f"成功插入 {inserted} 个街道/乡镇")

    if not_found:
        print(f"\n警告: {len(not_found)} 个乡镇无法找到对应的区县ID:")
        for item in not_found[:10]:
            print(f"  {item}")
        if len(not_found) > 10:
            print(f"  ... 还有 {len(not_found) - 10} 个")

if __name__ == '__main__':
    main()
