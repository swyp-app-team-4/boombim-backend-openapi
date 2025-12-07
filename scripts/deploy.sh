#!/bin/bash
set -e

APP_DIR=/home/ec2-user/boombim-scheduling
mkdir -p "$APP_DIR"
cd "$APP_DIR"

AWS_REGION=ap-northeast-2
ECR_REGISTRY=098072157131.dkr.ecr.ap-northeast-2.amazonaws.com
IMAGE_NAME=boombim-scheduling
CONTAINER_NAME=boombim-scheduling-app

echo "[deploy] Generate .env from SSM..."

PARAM_KEYS=(
  DB_URL
  DB_USER
  DB_PASSWORD
  OPEN_API_KEY
  S3_BUCKET
  S3_REGION
  S3_BASE_URL
  S3_PLACEHOLDER_KEY
  KAKAO_COORDINATE_TO_REGION_CODE_API_KEY
)

: > .env

for key in "${PARAM_KEYS[@]}"; do
  value=$(aws ssm get-parameter \
    --name "/boombim/${key}" \   # ← 여기만 /boombim/
    --with-decryption \
    --region "$AWS_REGION" \
    --query "Parameter.Value" \
    --output text)

  printf '%s=%s\n' "$key" "$value" >> .env
done

echo "[deploy] Login to ECR..."
aws ecr get-login-password --region "$AWS_REGION" \
  | docker login --username AWS --password-stdin "$ECR_REGISTRY"

echo "[deploy] Stop old container if exists..."
if docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}\$"; then
  docker stop "$CONTAINER_NAME" || true
  docker rm "$CONTAINER_NAME" || true
fi

echo "[deploy] Pull latest image..."
docker pull "$ECR_REGISTRY/$IMAGE_NAME:latest"

echo "[deploy] Run container..."
docker run -d \
  --name "$CONTAINER_NAME" \
  --env-file .env \
  -e SPRING_PROFILES_ACTIVE=prod \
  "$ECR_REGISTRY/$IMAGE_NAME:latest"

echo "[deploy] Done."
