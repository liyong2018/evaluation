#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""测试都江堰市数据解析 - 详细版本"""

import sys
sys.stdout.reconfigure(encoding='utf-8')

# 模拟解析逻辑
address = "四川省成都市都江堰市灌口街道南桥社区居民委员会"
code = "510181001001"

print(f"原始地址: {address}")
print(f"代码: {code}\n")

# 去掉"四川省"
if not address.startswith('四川省'):
    print("错误：不以四川省开头")
    sys.exit(1)

remaining = address[3:]
print(f"1. 去掉'四川省'后: {remaining}")

# 找地级市
city_name = None
if '市' in remaining:
    city_part = remaining.split('市', 1)[0]
    city_name = city_part + '市'
    remaining = remaining.split('市', 1)[1]
    print(f"2. 地级市: {city_name}")
    print(f"   剩余: {remaining}")

# 找区县
county_name = None
for suffix in ['区', '县']:
    if suffix in remaining:
        county_part = remaining.split(suffix, 1)[0]
        county_name = county_part + suffix
        remaining_after = remaining.split(suffix, 1)[1]
        print(f"3. 找到区县（{suffix}）: {county_name}")
        print(f"   剩余: {remaining_after}")
        break

# 处理县级市
if county_name is None:
    print(f"3. 没有找到'区'或'县'")
    print(f"   检查剩余内容: '{remaining}'")
    print(f"   '市' in remaining: {'市' in remaining}")

    if '市' in remaining:
        possible_county = remaining.split('市', 1)[0]
        print(f"   可能的县级市部分: '{possible_county}' (长度: {len(possible_county)})")
        if possible_county and len(possible_county) >= 2:
            county_name = possible_county + '市'
            remaining = remaining.split('市', 1)[1]
            print(f"   县级市: {county_name}")
            print(f"   剩余: {remaining}")

if county_name:
    print(f"\n最终区县名: {county_name}")
    print(f"剩余: {remaining}")
else:
    print(f"\n错误：未能提取区县名！")
    print(f"最终剩余: {remaining}")
