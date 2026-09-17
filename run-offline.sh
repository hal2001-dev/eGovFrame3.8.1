#!/usr/bin/env bash
#
# eGovFrame 3.8 sample - OFFLINE run (embedded Jetty)
# Starts the app at http://localhost:8080/ using ONLY the bundled m2-repo.
#
set -e
HERE="$(cd "$(dirname "$0")" && pwd)"

if [ -z "$JAVA_HOME" ]; then
	CAND="$(ls -d "$HOME"/.local/jdks/zulu8*/Contents/Home 2>/dev/null | head -1)"
	[ -n "$CAND" ] && export JAVA_HOME="$CAND"
fi
if [ -z "$JAVA_HOME" ] || [ ! -x "$JAVA_HOME/bin/java" ]; then
	echo "ERROR: JAVA_HOME is not set to a valid JDK 1.8. Export JAVA_HOME and retry." >&2
	exit 1
fi
echo "JAVA_HOME = $JAVA_HOME"

MVN="$HERE/tools/apache-maven-3.9.16/bin/mvn"
[ -x "$MVN" ] || MVN="mvn"

cd "$HERE/egovframe-sample"
echo "Starting Jetty on http://localhost:8080/  (Ctrl+C to stop)"
"$MVN" -o -Dmaven.repo.local="$HERE/m2-repo" jetty:run "$@"
