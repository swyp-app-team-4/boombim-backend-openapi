#!/bin/bash
set -e

APP_DIR=/home/ec2-user/boombim-scheduling

cd "$APP_DIR" || exit 0

echo "[CodeDeploy] Stopping boombim-scheduling container..."

CONTAINER_NAME=boombim-scheduling-app

if docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}\$"; then
  docker stop "$CONTAINER_NAME" || true
  docker rm "$CONTAINER_NAME" || true
else
  echo "[CodeDeploy] No existing container. Skip."
fi
