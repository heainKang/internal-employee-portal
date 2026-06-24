#!/bin/bash
set -e

SQLCMD=""
if [ -f /opt/mssql-tools18/bin/sqlcmd ]; then
  SQLCMD="/opt/mssql-tools18/bin/sqlcmd"
elif [ -f /opt/mssql-tools/bin/sqlcmd ]; then
  SQLCMD="/opt/mssql-tools/bin/sqlcmd"
else
  echo "❌ sqlcmd를 찾을 수 없습니다"
  exit 1
fi

echo "sqlcmd 위치: $SQLCMD"

$SQLCMD -S mssql,1433 -U SA -P 'Local_Test1234!' -C \
  -Q "IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'portal_db') CREATE DATABASE portal_db"

echo "✓ portal_db ready"
