#!/bin/sh
set -e

GITEA_BIN="/usr/local/bin/gitea"

echo "⏳ Waiting for Gitea HTTP endpoint..."
until wget -q -O /dev/null "$GITEA_URL/api/v1/version" 2>/dev/null; do
    sleep 2
done
echo "✅ Gitea is up!"

echo "👤 Ensuring admin user '$GITEA_ADMIN_USER' exists..."
if ! $GITEA_BIN admin user list 2>/dev/null | grep -qw "$GITEA_ADMIN_USER"; then
    $GITEA_BIN admin user create \
        --username "$GITEA_ADMIN_USER" \
        --password "$GITEA_ADMIN_PASS" \
        --email    "$GITEA_ADMIN_EMAIL" \
        --admin \
        --must-change-password=false
    echo "✅ Admin user created."
else
    echo "ℹ️ Admin user already exists."
fi

echo "🔑 Generating access token..."
TOKEN=$($GITEA_BIN admin user generate-access-token \
            --username "$GITEA_ADMIN_USER" \
            --token-name "auto-init-$(date +%s)" \
            --scopes "write:repository,write:user" \
            --raw 2>/dev/null | tail -n1)

if [ -z "$TOKEN" ]; then
    TOKEN=$($GITEA_BIN admin user generate-access-token \
                --username "$GITEA_ADMIN_USER" \
                --token-name "auto-init-$(date +%s)" \
                --scopes "write:repository,write:user" 2>&1 \
            | grep -oE '[A-Za-z0-9_-]{40}' | head -n1)
fi

if [ -z "$TOKEN" ]; then
    echo "❌ Failed to obtain access token."
    exit 1
fi
echo "✅ Token obtained: ${TOKEN%????????????????????????????????}..."

echo "📦 Creating repository '$REPO_NAME'..."
HTTP_CODE=$(wget -q -O /tmp/repo_resp.json \
    --server-response \
    --header="Authorization: token $TOKEN" \
    --header="Content-Type: application/json" \
    --post-data="{\"name\":\"$REPO_NAME\",\"description\":\"$REPO_DESCRIPTION\",\"private\":$REPO_PRIVATE,\"auto_init\":false}" \
    "$GITEA_URL/api/v1/user/repos" 2>&1 | awk '/HTTP\// {print $2}' | tail -n1)

if [ "$HTTP_CODE" = "201" ]; then
    echo "✅ Repository created."
elif [ "$HTTP_CODE" = "409" ]; then
    echo "ℹ️ Repository already exists, continuing..."
else
    echo "⚠️ Unexpected response ($HTTP_CODE):"
    cat /tmp/repo_resp.json || true
fi

echo "📤 Uploading files..."
WORKDIR=/tmp/repo-workdir
rm -rf "$WORKDIR"
mkdir -p "$WORKDIR"
cd "$WORKDIR"

git init -q
git config user.email "auto-init@local"
git config user.name  "Auto Init"
git remote add origin "http://$GITEA_ADMIN_USER:$TOKEN@gitea:3000/$GITEA_ADMIN_USER/$REPO_NAME.git"
git checkout -q -b main 2>/dev/null || git checkout -q -B main

# کپی فایل‌ها
if [ -n "$(ls -A /medapp-config-files 2>/dev/null)" ]; then
    cp -r /medapp-config-files/. .
    echo "✅ Files copied from /medapp-config-files."
else
    echo "ℹ️ /medapp-config-files is empty."
fi

if [ -z "$(ls -A | grep -v '^\.git$')" ]; then
    echo "# $REPO_NAME" > README.md
fi

git add .
git commit -q -m "Initial files uploaded automatically" || echo "ℹ️ Nothing to commit."
git push -u origin main --force

echo "🎉 Done! Repository '$REPO_NAME' is ready at $GITEA_URL/$GITEA_ADMIN_USER/$REPO_NAME"