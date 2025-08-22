#!/bin/bash

set -e

# 현재 쉘 스크립트 파일의 디렉토리로 이동
cd "$(dirname "$0")"

cd ../domain

chmod +x run-test-mysql.sh
./run-test-mysql.sh

# 도커 네트워크 생성
NETWORK_NAME="local-kokomen-net"

if ! docker network inspect "$NETWORK_NAME" >/dev/null 2>&1; then
  echo "Creating Docker network: $NETWORK_NAME"
  docker network create --driver bridge "$NETWORK_NAME"
else
  echo "Docker network '$NETWORK_NAME' already exists. Skipping."
fi

# 도메인 도커 컴포즈 실행
docker compose -f ../domain/local-docker-compose.yml up -d

../gradlew clean :internal:build

# 로컬 도커 컴포즈 실행
# 실행 전에 헬스 체크 필요
cd ../internal
docker rm -f kokomen-payment-local-internal
docker compose -f local-internal-docker-compose.yml up --build -d
