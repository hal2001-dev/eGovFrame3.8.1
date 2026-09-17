#!/usr/bin/env bash
#
# HMAC request signer / caller for the eGovFrame sample REST API.
# Reference implementation the *external system* can mirror in any language.
#
# Usage:
#   API_KEY=EXTSYS001 SECRET='change-me-...' BASE=http://localhost:8080 \
#     ./sign-request.sh <METHOD> <PATH> [BODY]
#
# Examples:
#   ./sign-request.sh GET /api/samples
#   ./sign-request.sh GET "/api/samples?searchKeyword=HSQL"
#   ./sign-request.sh POST /api/samples '{"name":"n","description":"d","useYn":"Y","regUser":"api"}'
#
set -euo pipefail

API_KEY="${API_KEY:-EXTSYS001}"
SECRET="${SECRET:-change-me-external-system-secret-0001}"
BASE="${BASE:-http://localhost:8080}"

METHOD="${1:?METHOD required}"
RAW_PATH="${2:?PATH required}"
BODY="${3:-}"

# split path / query
PATH_ONLY="${RAW_PATH%%\?*}"
if [[ "$RAW_PATH" == *\?* ]]; then QUERY="${RAW_PATH#*\?}"; else QUERY=""; fi

# epoch milliseconds
TS="$(( $(date +%s) * 1000 ))"

# sha-256 of body (lowercase hex); empty body -> hash of ""
BODY_HASH="$(printf '%s' "$BODY" | openssl dgst -sha256 -hex | sed 's/^.*= *//;s/^.* //')"

# stringToSign = METHOD \n PATH \n QUERY \n TS \n SHA256(body)
STRING_TO_SIGN="$(printf '%s\n%s\n%s\n%s\n%s' "$METHOD" "$PATH_ONLY" "$QUERY" "$TS" "$BODY_HASH")"

# HMAC-SHA256(secret, stringToSign), lowercase hex
SIG="$(printf '%s' "$STRING_TO_SIGN" | openssl dgst -sha256 -hmac "$SECRET" -hex | sed 's/^.*= *//;s/^.* //')"

echo ">> $METHOD $BASE$RAW_PATH" >&2
echo ">> X-API-KEY: $API_KEY" >&2
echo ">> X-API-TIMESTAMP: $TS" >&2
echo ">> X-API-SIGNATURE: $SIG" >&2
echo "----" >&2

curl -s -X "$METHOD" "$BASE$RAW_PATH" \
	-H "X-API-KEY: $API_KEY" \
	-H "X-API-TIMESTAMP: $TS" \
	-H "X-API-SIGNATURE: $SIG" \
	${BODY:+-H "Content-Type: application/json" --data-binary "$BODY"} \
	-w $'\n[HTTP %{http_code}]\n'
