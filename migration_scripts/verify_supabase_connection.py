#!/usr/bin/env python3
"""
Supabase 连接验证脚本
验证到 Supabase PostgreSQL 数据库的连接是否正常
"""

import psycopg2
import sys

def test_connection():
    """测试 Supabase 连接"""
    # Supabase 连接信息 (从 application-supabase.yml 获取)
    conn_params = {
        'host': 'aws-1-ap-southeast-1.pooler.supabase.com',
        'port': 6543,
        'database': 'postgres',
        'user': 'postgres.olcdeeonmpjijxtvolum',
        'password': 'Htht@12#$'
    }

    try:
        print("正在连接到 Supabase PostgreSQL...")
        print(f"主机: {conn_params['host']}:{conn_params['port']}")
        print(f"数据库: {conn_params['database']}")
        print(f"用户: {conn_params['user']}")

        conn = psycopg2.connect(**conn_params)
        cur = conn.cursor()

        # 执行简单查询
        cur.execute("SELECT version();")
        version = cur.fetchone()

        print("\n✅ 连接成功!")
        print(f"PostgreSQL 版本: {version[0]}")

        # 检查表是否存在
        cur.execute("""
            SELECT table_name
            FROM information_schema.tables
            WHERE table_schema = 'public'
            ORDER BY table_name;
        """)

        tables = cur.fetchall()
        print(f"\n当前数据库中的表数量: {len(tables)}")

        if tables:
            print("\n现有表列表:")
            for table in tables:
                print(f"  - {table[0]}")
        else:
            print("\n⚠️ 数据库中没有找到表")

        # 关闭连接
        cur.close()
        conn.close()

        return True

    except psycopg2.Error as e:
        print(f"\n❌ 连接失败!")
        print(f"错误类型: {type(e).__name__}")
        print(f"错误信息: {e}")
        return False
    except Exception as e:
        print(f"\n❌ 未知错误!")
        print(f"错误信息: {e}")
        return False

if __name__ == "__main__":
    print("=" * 60)
    print("Supabase PostgreSQL 连接验证工具")
    print("=" * 60)
    print()

    success = test_connection()

    print()
    print("=" * 60)
    if success:
        print("验证完成: Supabase 连接正常")
        sys.exit(0)
    else:
        print("验证失败: 请检查连接参数和网络")
        sys.exit(1)
