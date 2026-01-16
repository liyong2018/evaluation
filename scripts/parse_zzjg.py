#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
解析组织机构数据文件 zzjg.txt，生成完整的5级组织机构树
"""

import re
import json
from collections import OrderedDict
from typing import Dict, List, Tuple

def parse_address_and_code(line: str) -> Tuple[str, str]:
    """
    解析每一行数据
    格式: 社区（行政村）地址    行政区划代码
    """
    parts = line.strip().split('\t')
    if len(parts) >= 2:
        return parts[0].strip(), parts[1].strip()
    return None, None

def parse_hierarchy(address: str, code: str) -> Dict:
    """
    从地址和代码解析出5级组织机构结构
    """
    # 代码结构: 51 5101 510104 510104017 510104017001
    # 省市区街道社区

    # 省级 (2位)
    province_code = code[:2]
    # 市级 (4位)
    city_code = code[:4]
    # 区县级 (6位)
    county_code = code[:6]
    # 街道级 (9位)
    township_code = code[:9]
    # 社区级 (12位)
    community_code = code

    # 从地址中提取各级名称
    # 格式: 四川省成都市锦江区锦官驿街道水井坊社区居委会

    # 提取省
    province_match = re.search(r'(四川省|云南省|贵州省|西藏自治区|重庆市)', address)
    province_name = province_match.group(1) if province_match else '四川省'

    # 提取市 (四川省后面跟城市名)
    city_pattern = re.escape(province_name) + r'(.*?市)'
    city_match = re.search(city_pattern, address)
    city_name = city_match.group(1) if city_match else ''

    # 提取区县 (市后面跟区/县名)
    county_pattern = re.escape(city_name) + r'(.*?区|.*?县)'
    county_match = re.search(county_pattern, address)
    county_name = county_match.group(1) if county_match else ''

    # 提取街道 (区县后面跟街道/镇/乡)
    township_pattern = re.escape(county_name) + r'(.*?街道|.*?镇|.*?乡)'
    township_match = re.search(township_pattern, address)
    township_name = township_match.group(1) if township_match else ''

    # 提取社区 (街道后面跟社区/村)
    community_pattern = re.escape(township_name) + r'(.*?社区|.*?村委会|.*?村居委会)'
    community_match = re.search(community_pattern, address)
    community_name = community_match.group(1) if community_match else ''

    return {
        'level_1': {'code': province_code, 'name': province_name, 'level': 1},
        'level_2': {'code': city_code, 'name': city_name, 'level': 2, 'parent_code': province_code},
        'level_3': {'code': county_code, 'name': county_name, 'level': 3, 'parent_code': city_code},
        'level_4': {'code': township_code, 'name': township_name, 'level': 4, 'parent_code': county_code},
        'level_5': {'code': community_code, 'name': community_name, 'level': 5, 'parent_code': township_code},
    }

def build_organization_tree(input_file: str, output_file: str):
    """
    构建完整的组织机构树
    """
    # 使用字典去重，按code存储
    org_dict: Dict[str, Dict] = OrderedDict()

    with open(input_file, 'r', encoding='utf-8') as f:
        # 跳过标题行
        next(f)

        for line in f:
            address, code = parse_address_and_code(line)
            if not address or not code:
                continue

            hierarchy = parse_hierarchy(address, code)

            # 将每一级加入到字典中（自动去重）
            for level_key, org_data in hierarchy.items():
                code = org_data['code']
                if code not in org_dict:
                    org_dict[code] = {
                        'code': org_data['code'],
                        'name': org_data['name'],
                        'level': org_data['level'],
                        'parent_code': org_data.get('parent_code', ''),
                    }

    # 转换为列表
    organizations = list(org_dict.values())

    # 按代码排序
    organizations.sort(key=lambda x: x['code'])

    # 输出JSON
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(organizations, f, ensure_ascii=False, indent=2)

    print(f"总共生成 {len(organizations)} 条组织机构记录")
    print(f"各级别统计:")

    level_count = {}
    for org in organizations:
        level = org['level']
        level_count[level] = level_count.get(level, 0) + 1

    for level in sorted(level_count.keys()):
        print(f"  Level {level}: {level_count[level]} 条")

    return organizations

def generate_sql_insert(organizations: List[Dict], output_file: str, year: int = 2020, is_baseline: int = 1):
    """
    生成SQL插入语句 - 使用parent_id
    """
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write("-- 组织机构数据导入SQL\n")
        f.write(f"-- 年份: {year}, 基准数据: {is_baseline}\n")
        f.write("-- 第一步：插入所有组织机构（parent_id先设为NULL）\n\n")

        # 第一步：插入所有组织机构
        for i, org in enumerate(organizations):
            code = org['code']
            name = org['name']
            level = org['level']

            # 生成不同级别的字段
            province_name = ''
            city_name = ''
            county_name = ''
            township_name = ''
            community_name = ''

            if level == 1:
                province_name = name
            elif level == 2:
                city_name = name
            elif level == 3:
                county_name = name
            elif level == 4:
                township_name = name
            elif level == 5:
                community_name = name

            # 转义单引号
            name = name.replace("'", "''")
            province_name = province_name.replace("'", "''")
            city_name = city_name.replace("'", "''")
            county_name = county_name.replace("'", "''")
            township_name = township_name.replace("'", "''")
            community_name = community_name.replace("'", "''")

            sql = f"INSERT INTO organization (code, name, level, parent_id, year, is_baseline, province_name, city_name, county_name, township_name, community_name, data_source, create_time, update_time) VALUES ('{code}', '{name}', {level}, NULL, {year}, {is_baseline}, '{province_name}', '{city_name}', '{county_name}', '{township_name}', '{community_name}', 'IMPORT', NOW(), NOW());"

            f.write(sql + '\n')

        f.write("\n-- 第二步：更新parent_id（根据parent_code查找父节点的id）\n\n")

        # 第二步：更新parent_id
        for org in organizations:
            parent_code = org.get('parent_code', '')
            code = org['code']

            if parent_code:  # 如果有父级
                update_sql = f"UPDATE organization SET parent_id = (SELECT id FROM (SELECT id FROM organization WHERE code = '{parent_code}' AND year = {year} AND is_baseline = {is_baseline}) AS tmp) WHERE code = '{code}' AND year = {year} AND is_baseline = {is_baseline};"
                f.write(update_sql + '\n')

    print(f"\nSQL文件已生成: {output_file}")

def generate_excel_import(organizations: List[Dict], output_file: str):
    """
    生成Excel导入文件
    """
    try:
        import pandas as pd
    except ImportError:
        print("需要安装pandas: pip install pandas openpyxl")
        return

    # 准备Excel数据
    excel_data = []
    for org in organizations:
        excel_data.append({
            '代码': org['code'],
            '名称': org['name'],
            '级别': org['level'],
            '上级代码': org.get('parent_code', ''),
            '省份': org['name'] if org['level'] == 1 else '',
            '城市': org['name'] if org['level'] == 2 else '',
            '区县': org['name'] if org['level'] == 3 else '',
            '街道/乡镇': org['name'] if org['level'] == 4 else '',
            '社区/村': org['name'] if org['level'] == 5 else '',
        })

    df = pd.DataFrame(excel_data)
    df.to_excel(output_file, index=False, engine='openpyxl')
    print(f"\nExcel文件已生成: {output_file}")

if __name__ == '__main__':
    input_file = 'frontend/public/zzjg.txt'
    json_output = 'frontend/public/organizations.json'
    sql_output = 'src/main/resources/sql/import_organizations.sql'
    excel_output = 'frontend/public/organizations_import.xlsx'

    print("开始解析组织机构数据...")

    # 构建组织机构树
    organizations = build_organization_tree(input_file, json_output)

    # 生成SQL
    generate_sql_insert(organizations, sql_output)

    # 生成Excel
    generate_excel_import(organizations, excel_output)

    print("\n完成！")
