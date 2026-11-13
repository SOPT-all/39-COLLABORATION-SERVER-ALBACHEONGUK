#!/usr/bin/env bash
set -Eeuo pipefail

APP_DIR="${1:?Usage: deploy.sh APP_DIR APP_SERVICE ARTIFACT_PATH}"
APP_SERVICE="${2:?Usage: deploy.sh APP_DIR APP_SERVICE ARTIFACT_PATH}"
ARTIFACT_PATH="${3:?Usage: deploy.sh APP_DIR APP_SERVICE ARTIFACT_PATH}"

if [[ ! "$APP_DIR" =~ ^/[A-Za-z0-9/_-]+$ ]]; then
  echo "APP_DIR must be an absolute path using letters, numbers, /, _ or -." >&2
  exit 1
fi
if [[ ! "$APP_SERVICE" =~ ^[A-Za-z0-9_.@-]+$ ]]; then
  echo "APP_SERVICE contains unsupported characters." >&2
  exit 1
fi
if [[ ! -f "$ARTIFACT_PATH" ]]; then
  echo "Application artifact not found: $ARTIFACT_PATH" >&2
  exit 1
fi

APP_PATH="$APP_DIR/app.jar"
STAGED_PATH="$APP_DIR/app.jar.new"
BACKUP_PATH="$APP_DIR/app.jar.rollback"
HAD_PREVIOUS=false

sudo install -d -m 0755 "$APP_DIR"
if sudo test -f "$APP_PATH"; then
  sudo cp "$APP_PATH" "$BACKUP_PATH"
  HAD_PREVIOUS=true
fi
sudo install -m 0644 "$ARTIFACT_PATH" "$STAGED_PATH"
sudo mv "$STAGED_PATH" "$APP_PATH"

rollback() {
  echo "Deployment health check failed; restoring the previous application." >&2
  if [[ "$HAD_PREVIOUS" == true ]]; then
    sudo mv "$BACKUP_PATH" "$APP_PATH"
  else
    sudo rm -f "$APP_PATH"
  fi
  sudo systemctl restart "$APP_SERVICE" || true
  sudo journalctl -u "$APP_SERVICE" -n 80 --no-pager || true
}

if ! sudo systemctl restart "$APP_SERVICE"; then
  rollback
  exit 1
fi

for attempt in {1..30}; do
  if curl --fail --silent --show-error http://127.0.0.1:8080/api/v1/health; then
    OPENAPI_DOCS="$(curl --fail --silent --show-error http://127.0.0.1:8080/v3/api-docs || true)"
    REQUIRED_PATHS=(
      "/api/v1/jobs"
      "/api/v1/jobs/filters"
      "/api/v1/recommendations/region"
      "/api/v1/recommendations/resume"
    )
    ALL_PATHS_PRESENT=true
    for api_path in "${REQUIRED_PATHS[@]}"; do
      if ! grep -Fq "\"$api_path\"" <<< "$OPENAPI_DOCS"; then
        echo "OpenAPI document does not include $api_path yet."
        ALL_PATHS_PRESENT=false
        break
      fi
    done
    if [[ "$ALL_PATHS_PRESENT" == true ]]; then
      echo
      sudo rm -f "$BACKUP_PATH"
      echo "Deployment succeeded: $APP_SERVICE"
      exit 0
    fi
  fi
  sleep 2
done

rollback
exit 1
