#!/usr/bin/env bash
#
# HMAC 클라이언트 샘플 컴파일 + 실행 (mac/linux)
# 사용법: ./run-client.sh [baseUrl] [apiKey] [secret]
#   예)  ./run-client.sh http://localhost:8080 EXTSYS001 change-me-external-system-secret-0001
#
set -e
HERE="$(cd "$(dirname "$0")" && pwd)"

if [ -z "$JAVA_HOME" ]; then
	CAND="$(ls -d "$HOME"/.local/jdks/zulu8*/Contents/Home 2>/dev/null | head -1)"
	[ -n "$CAND" ] && export JAVA_HOME="$CAND"
fi
if [ -z "$JAVA_HOME" ] || [ ! -x "$JAVA_HOME/bin/java" ]; then
	echo "ERROR: JAVA_HOME 를 JDK 1.8 로 지정하세요." >&2
	exit 1
fi

mkdir -p "$HERE/build"
"$JAVA_HOME/bin/javac" -encoding UTF-8 -d "$HERE/build" \
	"$HERE/src/egovframework/example/client/EgovApiClient.java"
"$JAVA_HOME/bin/java" -cp "$HERE/build" egovframework.example.client.EgovApiClient "$@"
