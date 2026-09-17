#!/usr/bin/env bash
#
# eGovFrame 3.8 sample - OFFLINE build
# Builds the WAR using ONLY the bundled m2-repo (no internet required).
#
set -e
HERE="$(cd "$(dirname "$0")" && pwd)"

# --- JDK 1.8 ---------------------------------------------------------------
# Set JAVA_HOME to a JDK 1.8 before running, e.g.:
#   export JAVA_HOME=/path/to/jdk1.8
# If not set, we try a Zulu 8 under ~/.local/jdks (this machine's install).
if [ -z "$JAVA_HOME" ]; then
	CAND="$(ls -d "$HOME"/.local/jdks/zulu8*/Contents/Home 2>/dev/null | head -1)"
	[ -n "$CAND" ] && export JAVA_HOME="$CAND"
fi
if [ -z "$JAVA_HOME" ] || [ ! -x "$JAVA_HOME/bin/java" ]; then
	echo "ERROR: JAVA_HOME is not set to a valid JDK 1.8. Export JAVA_HOME and retry." >&2
	exit 1
fi
echo "JAVA_HOME = $JAVA_HOME"
"$JAVA_HOME/bin/java" -version

# --- Maven (prefer the bundled distribution; fall back to system mvn) -------
MVN="$HERE/tools/apache-maven-3.9.16/bin/mvn"
[ -x "$MVN" ] || MVN="mvn"

# --- Offline build ---------------------------------------------------------
cd "$HERE/egovframe-sample"
"$MVN" -o -Dmaven.repo.local="$HERE/m2-repo" clean package "$@"

echo
echo "DONE -> $HERE/egovframe-sample/target/egovframe-sample.war"
