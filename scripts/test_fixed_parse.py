#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""测试修复后的解析逻辑"""

import sys
sys.stdout.reconfigure(encoding='utf-8')

# 测试数据
test_cases = [
    ("四川省成都市锦江区锦官驿街道水井坊社区居委会", "510104017001", "锦江区"),
    ("四川省成都市都江堰市灌口街道南桥社区居民委员会", "510181001001", "都江堰市"),
    ("四川省成都市彭州市天彭街道社区居委会", "510182001001", "彭州市"),
]

for address, code, expected_county in test_cases:
    print(f"\n=== 测试: {expected_county} ===")
    print(f"地址: {address}")
    print(f"代码: {code}")

    # 去掉"四川省"
    remaining = address[3:]
    print(f"1. 去掉'四川省'后: {remaining}")

    # 找地级市
    city_name = None
    if '市' in remaining:
        city_part = remaining.split('市', 1)[0]
        city_name = city_part + '市'
        remaining = remaining.split('市', 1)[1]
        print(f"2. 地级市: {city_name}, 剩余: {remaining}")

    # 查找区县名称
    county_name = None
    for suffix in ['区', '县']:
        if suffix in remaining:
            idx = remaining.find(suffix)
            is_valid = False
            while idx != -1:
                if idx + 1 < len(remaining):
                    next_char = remaining[idx + 1]
                    if next_char not in ['社', '民', '居', '委']:
                        if idx == 0 or remaining[idx - 1] != '社':
                            is_valid = True
                            county_part = remaining[:idx]
                            county_name = county_part + suffix
                            remaining = remaining[idx + len(suffix):]
                            print(f"3. 找到区县({suffix}): {county_name}, 剩余: {remaining}")
                            break
                idx = remaining.find(suffix, idx + 1)
            if is_valid:
                break

    # 处理县级市
    if county_name is None and '市' in remaining:
        idx = remaining.find('市')
        while idx != -1:
            if idx + 1 < len(remaining):
                next_char = remaining[idx + 1]
                if next_char not in ['社', '居', '民', '委']:
                    possible_county = remaining[:idx]
                    if possible_county and len(possible_county) >= 2:
                        county_name = possible_county + '市'
                        remaining = remaining[idx + 1:]
                        print(f"3. 找到县级市: {county_name}, 剩余: {remaining}")
                        break
            idx = remaining.find('市', idx + 1)

    # 验证结果
    if county_name == expected_county:
        print(f"✅ 正确！提取到区县: {county_name}")
    else:
        print(f"❌ 错误！预期: {expected_county}, 实际: {county_name}")
