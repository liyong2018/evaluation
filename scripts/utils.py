#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
============================================================
减灾能力评估系统 - 工具脚本集合
Disaster Reduction Evaluation System - Utility Scripts
============================================================

使用方法:
  python scripts/utils.py setup-ssh              # 设置SSH密钥认证
  python scripts/utils.py generate-sql           # 生成消防员配置SQL
  python scripts/utils.py help                   # 显示帮助信息

环境变量:
  SERVER           服务器地址 (默认: 172.16.43.189)
  USERNAME         服务器用户 (默认: htht)
  PASSWORD         服务器密码 (可选)
  PUBLIC_KEY       本地公钥路径 (默认: ~/.ssh/id_rsa.pub)

示例:
  SERVER=192.168.1.100 python scripts/utils.py setup-ssh
  python scripts/utils.py generate-sql

============================================================
"""

import argparse
import os
import sys
from datetime import datetime


# ============================================================
# 配置常量
# ============================================================

# SSH配置
DEFAULT_SERVER = os.getenv("SERVER", "172.16.43.189")
DEFAULT_USERNAME = os.getenv("USERNAME", "htht")
DEFAULT_PASSWORD = os.getenv("PASSWORD", "")
DEFAULT_PUBLIC_KEY = os.getenv("PUBLIC_KEY", os.path.expanduser("~/.ssh/id_rsa.pub"))

# 行政区划代码到市县的映射（用于消防员SQL生成）
REGION_MAPPING = {
    # 绵阳市
    "510781": ("绵阳市", "江油市"), "510705": ("绵阳市", "涪城区"),
    "510703": ("绵阳市", "经济技术开发区"), "510704": ("绵阳市", "游仙区"),
    "510722": ("绵阳市", "三台县"), "510723": ("绵阳市", "盐亭县"),
    "510725": ("绵阳市", "梓潼县"), "510726": ("绵阳市", "北川羌族自治县"),
    "510727": ("绵阳市", "平武县"),
    # 南充市
    "511381": ("南充市", "阆中市"), "511321": ("南充市", "南部县"),
    "511322": ("南充市", "营山县"), "511323": ("南充市", "蓬安县"),
    "511324": ("南充市", "仪陇县"), "511325": ("南充市", "西充县"),
    "511302": ("南充市", "顺庆区"), "511303": ("南充市", "高坪区"),
    "511304": ("南充市", "嘉陵区"),
    # 广元市
    "510822": ("广元市", "青川县"), "510821": ("广元市", "旺苍县"),
    "510823": ("广元市", "剑阁县"), "510824": ("广元市", "苍溪县"),
    "510802": ("广元市", "利州区"), "510811": ("广元市", "昭化区"),
    "510812": ("广元市", "朝天区"),
    # 宜宾市
    "511525": ("宜宾市", "高县"), "511526": ("宜宾市", "珙县"),
    "511523": ("宜宾市", "江安县"), "511524": ("宜宾市", "长宁县"),
    "511504": ("宜宾市", "叙州区"), "511503": ("宜宾市", "南溪区"),
    "511502": ("宜宾市", "翠屏区"), "511527": ("宜宾市", "筠连县"),
    "511529": ("宜宾市", "屏山县"),
    # 泸州市
    "510522": ("泸州市", "泸县"), "510504": ("泸州市", "龙马潭区"),
    "510503": ("泸州市", "纳溪区"), "510502": ("泸州市", "江阳区"),
    "510521": ("泸州市", "泸县"), "510524": ("泸州市", "叙永县"),
    "510525": ("泸州市", "古蔺县"),
    # 成都市
    "510185": ("成都市", "简阳市"), "510183": ("成都市", "邛崃市"),
    "510182": ("成都市", "彭州市"), "510181": ("成都市", "都江堰市"),
    "510184": ("成都市", "崇州市"), "510129": ("成都市", "大邑县"),
    "510131": ("成都市", "蒲江县"), "510121": ("成都市", "金堂县"),
    "510116": ("成都市", "双流区"), "510115": ("成都市", "温江区"),
    "510114": ("成都市", "新都区"), "510113": ("成都市", "青白江区"),
    "510112": ("成都市", "龙泉驿区"), "510107": ("成都市", "武侯区"),
    "510106": ("成都市", "金牛区"), "510105": ("成都市", "青羊区"),
    "510108": ("成都市", "成华区"), "510104": ("成都市", "锦江区"),
    "510117": ("成都市", "郫都区"),
    # 自贡市
    "510322": ("自贡市", "富顺县"), "510321": ("自贡市", "荣县"),
    "510304": ("自贡市", "大安区"), "510302": ("自贡市", "自流井区"),
    # 攀枝花市
    "510402": ("攀枝花市", "东区"), "510403": ("攀枝花市", "西区"),
    "510411": ("攀枝花市", "仁和区"), "510422": ("攀枝花市", "米易县"),
    # 德阳市
    "510681": ("德阳市", "广汉市"), "510682": ("德阳市", "什邡市"),
    "510683": ("德阳市", "绵竹市"), "510604": ("德阳市", "罗江区"),
    "510603": ("德阳市", "德阳经济技术开发区"), "510623": ("德阳市", "中江县"),
    # 遂宁市
    "510921": ("遂宁市", "蓬溪县"), "510923": ("遂宁市", "大英县"),
    "510903": ("遂宁市", "安居区"),
    # 内江市
    "511024": ("内江市", "威远县"), "511083": ("内江市", "隆昌市"),
    "511011": ("内江市", "内江高新区"), "511025": ("内江市", "资中县"),
    "511002": ("内江市", "市中区"),
    # 乐山市
    "511126": ("乐山市", "夹江县"), "511129": ("乐山市", "沐川县"),
    "511113": ("乐山市", "金口河区"), "511111": ("乐山市", "沙湾区"),
    "511123": ("乐山市", "犍为县"), "511124": ("乐山市", "井研县"),
    "511112": ("乐山市", "五通桥区"), "511133": ("乐山市", "马边彝族自治县"),
    "511511": ("乐山市", "峨边彝族自治县"), "511181": ("乐山市", "峨眉山市"),
    # 眉山市
    "511403": ("眉山市", "彭山区"), "511421": ("眉山市", "仁寿县"),
    "511423": ("眉山市", "洪雅县"), "511424": ("眉山市", "丹棱县"),
    "511425": ("眉山市", "青神县"), "511402": ("眉山市", "东坡区"),
    # 广安市
    "511621": ("广安市", "岳池县"), "511622": ("广安市", "武胜县"),
    "511602": ("广安市", "广安区"), "511603": ("广安市", "前锋区"),
    "511623": ("广安市", "邻水县"), "511681": ("广安市", "华蓥市"),
    # 达州市
    "511725": ("达州市", "宣汉县"), "511722": ("达州市", "宣汉县"),
    "511724": ("达州市", "大竹县"), "511723": ("达州市", "开江县"),
    "511703": ("达州市", "达川区"), "511702": ("达州市", "通川区"),
    "511781": ("达州市", "万源市"),
    # 雅安市
    "511822": ("雅安市", "荥经县"), "511823": ("雅安市", "汉源县"),
    "511824": ("雅安市", "石棉县"), "511825": ("雅安市", "天全县"),
    "511826": ("雅安市", "芦山县"), "511827": ("雅安市", "宝兴县"),
    "511803": ("雅安市", "名山区"),
    # 巴中市
    "511921": ("巴中市", "通江县"), "511922": ("巴中市", "南江县"),
    "511902": ("巴中市", "巴州区"), "511903": ("巴中市", "恩阳区"),
    "511923": ("巴中市", "平昌县"),
    # 资阳市
    "512022": ("资阳市", "乐至县"), "512021": ("资阳市", "安岳县"),
    "512002": ("资阳市", "雁江区"),
    # 阿坝藏族羌族自治州
    "513226": ("阿坝藏族羌族自治州", "道孚县"), "513224": ("阿坝藏族羌族自治州", "松潘县"),
    "513227": ("阿坝藏族羌族自治州", "小金县"), "513228": ("阿坝藏族羌族自治州", "黑水县"),
    "513233": ("阿坝藏族羌族自治州", "红原县"), "513201": ("阿坝藏族羌族自治州", "马尔康市"),
    "513221": ("阿坝藏族羌族自治州", "汶川县"), "513223": ("阿坝藏族羌族自治州", "茂县"),
    # 甘孜藏族自治州
    "513326": ("甘孜藏族自治州", "道孚县"), "513335": ("甘孜藏族自治州", "巴塘县"),
    "513330": ("甘孜藏族自治州", "德格县"), "513331": ("甘孜藏族自治州", "白玉县"),
    "513327": ("甘孜藏族自治州", "炉霍县"), "513332": ("甘孜藏族自治州", "石渠县"),
    "513333": ("甘孜藏族自治州", "色达县"), "513334": ("甘孜藏族自治州", "石渠县"),
    "513336": ("甘孜藏族自治州", "乡城县"), "513337": ("甘孜藏族自治州", "稻城县"),
    "513338": ("甘孜藏族自治州", "得荣县"), "513301": ("甘孜藏族自治州", "康定市"),
    "513322": ("甘孜藏族自治州", "泸定县"), "513324": ("甘孜藏族自治州", "九龙县"),
    "513325": ("甘孜藏族自治州", "雅江县"), "513328": ("甘孜藏族自治州", "甘孜县"),
    # 凉山彝族自治州
    "513427": ("凉山彝族自治州", "宁南县"), "513429": ("凉山彝族自治州", "布拖县"),
    "513430": ("凉山彝族自治州", "金阳县"), "513431": ("凉山彝族自治州", "昭觉县"),
    "513433": ("凉山彝族自治州", "冕宁县"), "513434": ("凉山彝族自治州", "越西县"),
    "513435": ("凉山彝族自治州", "甘洛县"), "513436": ("凉山彝族自治州", "美姑县"),
    "513437": ("凉山彝族自治州", "雷波县"), "513422": ("凉山彝族自治州", "木里藏族自治县"),
    "513424": ("凉山彝族自治州", "德昌县"), "513425": ("凉山彝族自治州", "会理县"),
    "513426": ("凉山彝族自治州", "会东县"), "513428": ("凉山彝族自治州", "普格县"),
    "513423": ("凉山彝族自治州", "盐源县"),
}

# 原始消防站数据（部分示例）
FIREFIGHTER_DATA = """0	国能四川天明发电有限公司专职消防队	14	510781111206
1	老观消防站	8	511381111001
2	竹园镇政府专职消防救援站	9	510822105000
3	文江镇政府专职消防队	6	511525100000
4	中江县凯江镇小型消防站	7	510623100000
5	四川天华股份有限公司企业专职消防队	46	510522002001
6	国能四川华蓥山发电有限公司专职消防救援队	5	511725102001
7	道孚县玉科镇政府专职消防队	3	513326104201
8	成都东部新区草池政府专职消防队	21	510185014000
9	成都天府国际机场分公司消防管理部空侧主站	35	510185014000
10	成都天府国际机场分公司消防管理部3号分站	23	510185012000
11	岳池县顾县镇专职消防队	12	511621110000
12	西充县槐树镇政府专职消防队	7	511325109000
13	攀枝花市西区格里坪政府政府专职队	10	510403100000
14	都江堰市消防救援大队	1	510181003000
15	沙渠专职队	17	510129002000
16	龙泉驿区消防救援大队洛带专职队	14	510112102000
17	武侯区武兴路政府专职消防队	32	510107014000
18	武侯区聚龙路政府专职消防队	10	510107014000
19	四川省资阳市乐至县通旅镇政府专职消防队	0	512022115000
20	金川县观音桥消防站	3	513226101000"""


# ============================================================
# 函数: 设置SSH密钥认证
# ============================================================
def setup_ssh_key(server=DEFAULT_SERVER, username=DEFAULT_USERNAME,
                  password=DEFAULT_PASSWORD, public_key=DEFAULT_PUBLIC_KEY):
    """
    Setup SSH key authentication for passwordless login

    Args:
        server: 服务器地址
        username: 服务器用户名
        password: 服务器密码（可选，如果已有密钥认证）
        public_key: 本地公钥路径
    """
    try:
        import paramiko
    except ImportError:
        print("错误: 需要安装 paramiko 库")
        print("请运行: pip install paramiko")
        sys.exit(1)

    # 读取公钥
    try:
        with open(public_key, 'r') as f:
            key_content = f.read().strip()
    except FileNotFoundError:
        print(f"错误: 公钥文件不存在: {public_key}")
        print("请先生成SSH密钥对: ssh-keygen -t rsa")
        sys.exit(1)

    # 创建SSH客户端
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())

    try:
        print(f"正在连接到 {server} 作为 {username}...")
        if password:
            ssh.connect(server, username=username, password=password)
        else:
            # 尝试使用现有密钥
            ssh.connect(server, username=username)
        print("连接成功！")

        # 创建.ssh目录
        print("正在设置SSH密钥...")
        stdin, stdout, stderr = ssh.exec_command("mkdir -p ~/.ssh && chmod 700 ~/.ssh")
        stdout.channel.recv_exit_status()

        # 添加公钥到authorized_keys
        command = f'echo "{key_content}" >> ~/.ssh/authorized_keys'
        stdin, stdout, stderr = ssh.exec_command(command)
        stdout.channel.recv_exit_status()

        # 设置正确的权限
        stdin, stdout, stderr = ssh.exec_command("chmod 600 ~/.ssh/authorized_keys")
        stdout.channel.recv_exit_status()

        print("SSH密钥设置完成！")
        print(f"\n现在可以使用以下命令免密登录:")
        print(f"ssh {username}@{server}")

    except Exception as e:
        print(f"错误: {e}")
        sys.exit(1)
    finally:
        ssh.close()


# ============================================================
# 函数: 根据行政区划代码获取市县信息
# ============================================================
def get_region_info(code):
    """根据行政区划代码获取市县信息"""
    county_code = code[:6]
    if county_code in REGION_MAPPING:
        return REGION_MAPPING[county_code]
    return ("四川省", "未知县")


# ============================================================
# 函数: 转义SQL字符串
# ============================================================
def escape_sql_string(s):
    """转义SQL字符串"""
    if s is None:
        return ""
    return s.replace("'", "''").replace("\\", "\\\\")


# ============================================================
# 函数: 生成消防员配置SQL
# ============================================================
def generate_firefighter_sql(output_file=None):
    """
    生成消防员配置表的SQL插入语句

    Args:
        output_file: 输出文件路径（可选）
    """
    sql_lines = []
    sql_lines.append("-- 消防员配置表数据插入脚本")
    sql_lines.append("-- 数据来源：消防站数据表（按region_code汇总消防员数量）")
    sql_lines.append(f"-- 生成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    sql_lines.append("")
    sql_lines.append("-- 清空现有数据（可选）")
    sql_lines.append("-- TRUNCATE TABLE firefighter_config;")
    sql_lines.append("")
    sql_lines.append("-- 插入消防员配置数据")
    sql_lines.append("-- firefighter_count 为按region_code汇总的消防员总数")
    sql_lines.append("")
    sql_lines.append("INSERT INTO firefighter_config (region_code, province_name, city_name, county_name, township_name, firefighter_count, status, remark, created_time, updated_time) VALUES")

    # 按region_code汇总消防员数量
    region_stats = {}

    for line in FIREFIGHTER_DATA.strip().split('\n'):
        parts = line.split('\t')
        if len(parts) < 4:
            continue

        fid = parts[0]
        name = parts[1]
        count = parts[2]
        code = parts[3]

        try:
            firefighter_count = int(count) if count and count.strip() else 0
        except ValueError:
            firefighter_count = 0

        if code not in region_stats:
            region_stats[code] = [firefighter_count, [name]]
        else:
            region_stats[code][0] += firefighter_count
            region_stats[code][1].append(name)

    records = []
    for code, (total_count, names) in sorted(region_stats.items()):
        city_name, county_name = get_region_info(code)

        if len(names) <= 3:
            remark_str = "、".join(names)
        else:
            remark_str = f"{names[0]}、{names[1]}、{names[2]}等{len(names)}个消防站"

        escaped_remark = remark_str.replace("'", "''")

        record = f"('{code}', '四川省', '{city_name}', '{county_name}', '', {total_count}, 1, '{escaped_remark}', NOW(), NOW())"
        records.append(record)

    sql_lines.append(',\n'.join(records) + ';')
    sql_lines.append("")
    sql_lines.append("-- 更新说明：")
    sql_lines.append("-- 1. region_code: 使用原始数据的12位行政区划代码（唯一）")
    sql_lines.append("-- 2. firefighter_count: 按region_code汇总的消防员总数")
    sql_lines.append("-- 3. remark: 列出该区域的所有消防站名称")

    sql_content = '\n'.join(sql_lines)

    if output_file:
        with open(output_file, 'w', encoding='utf-8') as f:
            f.write(sql_content)
        print(f"SQL文件已生成: {output_file}")
        print(f"共生成 {len(region_stats)} 条记录")
    else:
        print(sql_content)

    return sql_content


# ============================================================
# 函数: 显示帮助信息
# ============================================================
def show_help():
    """显示帮助信息"""
    print(__doc__)


# ============================================================
# 主程序入口
# ============================================================
def main():
    parser = argparse.ArgumentParser(
        description='减灾能力评估系统 - 工具脚本',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
示例:
  python scripts/utils.py setup-ssh
  python scripts/utils.py generate-sql -o src/main/resources/sql/insert_firefighter_config.sql
  SERVER=192.168.1.100 USERNAME=admin python scripts/utils.py setup-ssh
        """
    )

    subparsers = parser.add_subparsers(dest='command', help='可用命令')

    # setup-ssh 命令
    ssh_parser = subparsers.add_parser('setup-ssh', help='设置SSH密钥认证')
    ssh_parser.add_argument('--server', default=DEFAULT_SERVER, help='服务器地址')
    ssh_parser.add_argument('--username', default=DEFAULT_USERNAME, help='服务器用户名')
    ssh_parser.add_argument('--password', default=DEFAULT_PASSWORD, help='服务器密码')
    ssh_parser.add_argument('--public-key', default=DEFAULT_PUBLIC_KEY, help='本地公钥路径')

    # generate-sql 命令
    sql_parser = subparsers.add_parser('generate-sql', help='生成消防员配置SQL')
    sql_parser.add_argument('-o', '--output', help='输出文件路径')
    sql_parser.add_argument('--print', action='store_true', help='打印到控制台')

    # help 命令
    subparsers.add_parser('help', help='显示帮助信息')

    args = parser.parse_args()

    if args.command == 'setup-ssh':
        setup_ssh_key(
            server=args.server,
            username=args.username,
            password=args.password,
            public_key=args.public_key
        )
    elif args.command == 'generate-sql':
        output_file = args.output or 'src/main/resources/sql/insert_firefighter_config.sql'
        generate_firefighter_sql(output_file if not args.print else None)
    else:
        show_help()


if __name__ == "__main__":
    main()
