#!/bin/bash

set -e

# 현재 쉘 스크립트 파일의 디렉토리로 이동
cd "$(dirname "$0")"

# 테스트 컨테이너 실행 (MySQL + Redis)
docker compose -f test-docker-compose.yml up -d

# 도커 네트워크 생성
NETWORK_NAME="local-kokomen-net"

if ! docker network inspect "$NETWORK_NAME" >/dev/null 2>&1; then
  echo "Creating Docker network: $NETWORK_NAME"
  docker network create --driver bridge "$NETWORK_NAME"
else
  echo "Docker network '$NETWORK_NAME' already exists. Skipping."
fi

./gradlew clean build

docker rm -f kokomen-payment-local-internal || true
docker compose -f local-internal-docker-compose.yml up --build -d
