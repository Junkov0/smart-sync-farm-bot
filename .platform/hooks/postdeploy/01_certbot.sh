#!/bin/bash
set -e

DOMAIN="smartfarm-bot.p-e.kr"

# 이미 발급된 인증서가 있으면 재발급 시도하지 않는다 (배포할 때마다 이 훅이 실행되므로
# 매번 발급을 시도하면 Let's Encrypt rate limit에 걸릴 수 있다).
if [ -d "/etc/letsencrypt/live/$DOMAIN" ]; then
  echo "인증서가 이미 존재합니다. 발급을 건너뜁니다."
else
  dnf install -y python3-pip
  # rpm으로 설치된 pip 자체를 업그레이드하려 하면 RECORD 파일이 없어 실패하므로 건드리지 않는다.
  python3 -m pip install certbot certbot-nginx

  # --nginx: http-01 챌린지를 nginx가 임시로 처리하게만 하고, 설정 파일은 영구적으로
  # 건드리지 않는다(certonly). 실제 443 서버 블록은 .platform/nginx/conf.d에서 별도로 관리한다.
  certbot certonly --nginx -d "$DOMAIN" --non-interactive --agree-tos --register-unsafely-without-email
fi

# Let's Encrypt 인증서는 90일마다 만료되므로 자동 갱신 cron을 등록한다.
# 파일 내용이 매번 동일하므로 재실행돼도 중복 등록되지 않는다.
cat > /etc/cron.d/certbot-renew <<'EOF'
0 3 * * * root certbot renew --quiet --deploy-hook "systemctl reload nginx"
EOF
