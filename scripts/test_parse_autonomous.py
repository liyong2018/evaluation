#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""测试自治州数据解析"""

import sys
sys.stdout.reconfigure(encoding='utf-8')

# 测试自治州数据
test_lines = [
    "四川省甘孜藏族自治州康定市榆林街道公主桥社区居民委员会	513301001001",
    "四川省凉山彝族自治州西昌市西城街道西门坡社区居民委员会	513401001001",
]

for line in test_lines:
    parts = line.strip().split('\t')
    address = parts[0]
    code = parts[1]

    print(f'\n=== 测试: {address[:30]}... ===')
    print(f'Code: {code}')

    # 去掉"四川省"
    if not address.startswith('四川省'):
        print("错误：不以四川省开头")
        continue
    remaining = address[3:]
    print(f'1. 去掉"四川省"后: {remaining}')

    # 处理自治州
    city_name = None
    if remaining.startswith('阿坝藏族羌族自治州'):
        city_name = '阿坝藏族羌族自治州'
        remaining = remaining[7:]
        print(f'2. 找到自治州: {city_name}')
        print(f'   剩余: {remaining}')
    elif remaining.startswith('甘孜藏族自治州'):
        city_name = '甘孜藏族自治州'
        remaining = remaining[7:]
        print(f'2. 找到自治州: {city_name}')
        print(f'   剩余: {remaining}')
    elif remaining.startswith('凉山彝族自治州'):
        city_name = '凉山彝族自治州'
        remaining = remaining[7:]
        print(f'2. 找到自治州: {city_name}')
        print(f'   剩余: {remaining}')
    else:
        print(f'2. 没有匹配到自治州')
        print(f'   剩余: {remaining}')

    # 检查区县
    if city_name:
        print(f'3. 检查区县...')
        for suffix in ['区', '县']:
            if suffix in remaining:
                print(f'   找到"{suffix}": {remaining}')
                break
        if '市' in remaining:
            print(f'   找到"市": {remaining}')
