#!/bin/bash
set -e

DOMAIN="smartfarm-bot.p-e.kr"

# 이미 발급된 인증서가 있으면 재발급 시도하지 않는다 (배포할 때마다 이 훅이 실행되므로
# 매번 발급을 시도하면 Let's Encrypt rate limit에 걸릴 수 있다. 갱신은 별도 cron이 처리).
if [ -d "/etc/letsencrypt/live/$DOMAIN" ]; then
  echo "인증서가 이미 존재합니다. 발급을 건너뜁니다."
  exit 0
fi

dnf install -y python3-pip
python3 -m pip install --upgrade pip
python3 -m pip install certbot certbot-nginx

# --nginx: http-01 챌린지를 nginx가 임시로 처리하게만 하고, --nginx-server-root의 설정 파일은
# 영구적으로 건드리지 않는다(certonly). 실제 443 서버 블록은 별도로 관리한다.
certbot certonly --nginx -d "$DOMAIN" --non-interactive --agree-tos --register-unsafely-without-email
