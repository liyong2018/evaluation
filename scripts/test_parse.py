#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""测试都江堰市数据解析"""

test_lines = [
    "四川省成都市都江堰市灌口街道南桥社区居民委员会	510181001001",
    "四川省成都市彭州市天彭街道社区居委会	510182001001",
]

for line in test_lines:
    address = line.split('\t')[0]
    code = line.split('\t')[1]

    print(f"\n=== 解析: {address} ===")
    print(f"Code: {code}")

    # 去掉"四川省"
    if not address.startswith('四川省'):
        print("跳过：不以四川省开头")
        continue
    remaining = address[3:]
    print(f"去掉省后: {remaining}")

    # 找地级市
    if '市' in remaining:
        city_part = remaining.split('市', 1)[0]
        city_name = city_part + '市'
        remaining = remaining.split('市', 1)[1]
        print(f"地级市: {city_name}")
        print(f"剩余: {remaining}")

    # 找区县
    county_name = None
    for suffix in ['区', '县']:
        if suffix in remaining:
            county_part = remaining.split(suffix, 1)[0]
            county_name = county_part + suffix
            remaining = remaining.split(suffix, 1)[1]
            print(f"找到区县({suffix}): {county_name}")
            break

    # 处理县级市
    if county_name is None and '市' in remaining:
        possible_county = remaining.split('市', 1)[0]
        print(f"可能的县级市部分: '{possible_county}' (长度: {len(possible_county)})")
        if possible_county and len(possible_county) >= 2:
            county_name = possible_county + '市'
            remaining = remaining.split('市', 1)[1]
            print(f"县级市: {county_name}")

    if county_name:
        print(f"最终区县名: {county_name}")
        print(f"剩余: {remaining}")
    else:
        print("错误：未能提取区县名！")
