#!/usr/bin/env python3
"""将家庭减灾能力模型 SQL 应用到 MySQL。"""

from pathlib import Path
import re
import sys

import pymysql


DB_CONFIG = {
    "host": "127.0.0.1",
    "port": 30314,
    "user": "root",
    "password": "123456",
    "database": "evaluate_db",
    "charset": "utf8mb4",
    "autocommit": False,
}

SQL_PATH = Path(__file__).resolve().parents[1] / "src" / "main" / "resources" / "sql" / "add_family_capacity_evaluation_model.sql"


def split_sql(content: str) -> list[str]:
    statements = []
    buffer = []
    in_single = False
    in_double = False
    i = 0
    while i < len(content):
        ch = content[i]
        prev = content[i - 1] if i > 0 else ""
        if ch == "'" and not in_double and prev != "\\":
            in_single = not in_single
        elif ch == '"' and not in_single and prev != "\\":
            in_double = not in_double
        if ch == ";" and not in_single and not in_double:
            stmt = "".join(buffer).strip()
            if stmt:
                statements.append(stmt)
            buffer = []
        else:
            buffer.append(ch)
        i += 1
    tail = "".join(buffer).strip()
    if tail:
        statements.append(tail)
    return statements


def normalize_sql(content: str) -> str:
    lines = []
    for line in content.splitlines():
        stripped = line.strip()
        if stripped.startswith("--"):
            continue
        lines.append(line)
    return "\n".join(lines)


def replace_session_vars(sql: str, session_vars: dict[str, int]) -> str:
    for key, value in session_vars.items():
        sql = sql.replace(f"@{key}", str(value))
    return sql


def resolve_session_var(cur, statement: str, session_vars: dict[str, int]) -> None:
    match = re.match(
        r"SET\s+@(?P<name>\w+)\s*=\s*\(\s*SELECT\s+id\s+FROM\s+model_step\s+WHERE\s+model_id\s*=\s*(?P<model_id>\d+)\s+AND\s+step_code\s*=\s*'(?P<step_code>[^']+)'\s+LIMIT\s+1\s*\)\s*$",
        statement,
        flags=re.IGNORECASE | re.DOTALL,
    )
    if not match:
        raise ValueError(f"Unsupported SET statement: {statement}")
    cur.execute(
        "SELECT id FROM model_step WHERE model_id=%s AND step_code=%s LIMIT 1",
        (int(match.group("model_id")), match.group("step_code")),
    )
    row = cur.fetchone()
    if not row:
        raise RuntimeError(f"Cannot resolve step_id for step_code={match.group('step_code')}")
    session_vars[match.group("name")] = int(row[0])


def main() -> int:
    content = normalize_sql(SQL_PATH.read_text(encoding="utf-8"))
    statements = split_sql(content)
    session_vars: dict[str, int] = {}

    conn = pymysql.connect(**DB_CONFIG)
    try:
        with conn.cursor() as cur:
            for statement in statements:
                stripped = statement.strip()
                if not stripped:
                    continue
                if stripped.upper().startswith("SET NAMES"):
                    cur.execute("SET NAMES utf8mb4")
                    continue
                if stripped.upper().startswith("SET @"):
                    resolve_session_var(cur, stripped, session_vars)
                    continue
                cur.execute(replace_session_vars(stripped, session_vars))
        conn.commit()
        print(f"APPLIED: {SQL_PATH}")
        return 0
    except Exception as exc:
        conn.rollback()
        print(f"FAILED: {exc}", file=sys.stderr)
        return 1
    finally:
        conn.close()


if __name__ == "__main__":
    raise SystemExit(main())
