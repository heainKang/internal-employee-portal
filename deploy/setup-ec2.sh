#!/bin/bash
# ============================================================
# EC2 초기 설정 스크립트 — Ubuntu 22.04 LTS 이상
# sudo bash setup-ec2.sh 으로 실행
# ============================================================
set -euo pipefail

echo "▶ 1. 시스템 패키지 업데이트"
apt-get update -y && apt-get upgrade -y

echo "▶ 2. Java 17 설치"
apt-get install -y openjdk-17-jdk
java -version

echo "▶ 3. Nginx 설치"
apt-get install -y nginx
systemctl enable nginx

echo "▶ 4. 애플리케이션 디렉토리 생성"
mkdir -p /opt/portal
chown ubuntu:ubuntu /opt/portal

echo "▶ 5. .env 파일 생성 — 아래 값을 실제 값으로 반드시 교체하세요"
cat > /opt/portal/.env <<'ENVEOF'
# ── Database ──────────────────────────────────────────────
DB_URL=jdbc:sqlserver://localhost:1433;databaseName=portal_db;encrypt=false;trustServerCertificate=true
DB_USERNAME=sa
DB_PASSWORD=CHANGE_ME

# ── JWT (최소 32자 이상 랜덤 문자열) ─────────────────────
JWT_SECRET_KEY=CHANGE_ME_TO_SECURE_256BIT_RANDOM_KEY
JWT_EXPIRATION=1800

# ── JPA ──────────────────────────────────────────────────
DDL_AUTO=update

# ── Initial Admin ────────────────────────────────────────
ADMIN_INITIAL_EMAIL=admin@bit.com
ADMIN_INITIAL_PASSWORD=CHANGE_ME

# ── CORS (Vue.js 배포 도메인/IP) ─────────────────────────
CORS_ALLOWED_ORIGINS=http://CHANGE_ME_EC2_IP,http://localhost:5173
ENVEOF
chmod 600 /opt/portal/.env
chown ubuntu:ubuntu /opt/portal/.env
echo "  → /opt/portal/.env 생성 완료. 값을 직접 수정하세요: nano /opt/portal/.env"

echo "▶ 6. systemd 서비스 등록"
cat > /etc/systemd/system/portal.service <<'SVCEOF'
[Unit]
Description=Internal Employee Portal API Server
After=network.target docker.service
Wants=network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/opt/portal
ExecStart=/usr/bin/java -jar /opt/portal/app.jar
EnvironmentFile=/opt/portal/.env
Restart=on-failure
RestartSec=10
SuccessExitStatus=143
TimeoutStopSec=30
StandardOutput=journal
StandardError=journal
SyslogIdentifier=portal

[Install]
WantedBy=multi-user.target
SVCEOF
systemctl daemon-reload
systemctl enable portal

echo "▶ 7. Nginx 설정"
cat > /etc/nginx/sites-available/portal <<'NGINXEOF'
server {
    listen 80;
    server_name _;

    location / {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Host              $host;
        proxy_set_header X-Real-IP         $remote_addr;
        proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 60s;
        proxy_read_timeout    60s;
    }
}
NGINXEOF
ln -sf /etc/nginx/sites-available/portal /etc/nginx/sites-enabled/portal
rm -f /etc/nginx/sites-enabled/default
nginx -t && systemctl restart nginx

echo "▶ 8. Docker 설치 및 PostgreSQL 컨테이너 실행"
# MSSQL은 RAM 2GB 요구 → t3.micro(1GB) 불가. PostgreSQL(~200MB)로 대체
curl -fsSL https://get.docker.com | sh
usermod -aG docker ubuntu
systemctl enable docker
systemctl start docker

DB_PASSWORD="${PG_PASSWORD:-Local_Test1234!}"

docker run -d \
  --name postgres \
  --restart unless-stopped \
  -e POSTGRES_DB=portal_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD="${DB_PASSWORD}" \
  -p 5432:5432 \
  postgres:16

echo "  PostgreSQL 컨테이너 시작 대기 중 (15초)..."
sleep 15
docker exec postgres pg_isready -U postgres -d portal_db && echo "  → portal_db 준비 완료"

PUBLIC_IP=$(curl -s ifconfig.me)

echo ""
echo "=========================================="
echo "  EC2 초기 설정 완료"
echo "=========================================="
echo ""
echo "  ★ 필수 후속 작업:"
echo "  1. .env 파일 값 수정: nano /opt/portal/.env"
echo "     DB_PASSWORD=${DB_PASSWORD}"
echo ""
echo "  2. GitHub Actions SSH 키 생성:"
echo "     ssh-keygen -t ed25519 -C github-actions -f ~/.ssh/github-actions -N ''"
echo "     cat ~/.ssh/github-actions.pub >> ~/.ssh/authorized_keys"
echo ""
echo "  3. GitHub Secrets 등록 (아래 3개):"
echo "     EC2_HOST     = ${PUBLIC_IP}"
echo "     EC2_USER     = ubuntu"
echo "     EC2_SSH_KEY  = (cat ~/.ssh/github-actions 내용 전체)"
echo ""
echo "  4. GitHub main 브랜치에 push → 자동 배포 시작"
echo "  5. 배포 후 Swagger UI: http://${PUBLIC_IP}/swagger-ui/index.html"
