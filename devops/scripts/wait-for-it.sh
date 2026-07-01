#!/usr/bin/env bash
# wait-for-it.sh – wait for a TCP host:port to be available
# Source: https://github.com/vishnubob/wait-for-it (MIT License)
set -e

HOST=""
PORT=""
TIMEOUT=15
STRICT=0
CHILD=0
QUIET=0
CMD=()

echoerr() { [ "$QUIET" -ne 1 ] && echo "$@" 1>&2; }

usage() {
  cat << USAGE >&2
Usage:
  $0 host:port [-s] [-t timeout] [-- command args]
  -h HOST | --host=HOST   Host to test
  -p PORT | --port=PORT   Port to test
  -s | --strict           Fail if connection not established
  -q | --quiet            Don't output messages
  -t TIMEOUT              Timeout (default: $TIMEOUT)
  -- COMMAND ARGS         Command to execute after check
USAGE
  exit 1
}

wait_for() {
  if [ "$TIMEOUT" -gt 0 ]; then
    echoerr "Waiting $TIMEOUT seconds for $HOST:$PORT"
  else
    echoerr "Waiting for $HOST:$PORT without timeout"
  fi
  start_ts=$(date +%s)
  while true; do
    (echo > /dev/tcp/$HOST/$PORT) >/dev/null 2>&1
    result=$?
    if [ $result -eq 0 ]; then
      end_ts=$(date +%s)
      echoerr "$HOST:$PORT is available after $((end_ts - start_ts)) seconds"
      break
    fi
    sleep 1
    if [ "$TIMEOUT" -gt 0 ] && [ $(($(date +%s) - start_ts)) -ge "$TIMEOUT" ]; then
      echoerr "Timeout: $HOST:$PORT not available"
      [ "$STRICT" -eq 1 ] && exit 1
      break
    fi
  done
  return $result
}

while [ $# -gt 0 ]; do
  case "$1" in
    *:*) HOST=$(echo "$1" | cut -d: -f1); PORT=$(echo "$1" | cut -d: -f2); shift ;;
    --host=*) HOST=$(echo "$1" | cut -d= -f2); shift ;;
    -h) HOST=$2; shift 2 ;;
    --port=*) PORT=$(echo "$1" | cut -d= -f2); shift ;;
    -p) PORT=$2; shift 2 ;;
    -t) TIMEOUT=$2; shift 2 ;;
    --timeout=*) TIMEOUT=$(echo "$1" | cut -d= -f2); shift ;;
    -s|--strict) STRICT=1; shift ;;
    -q|--quiet) QUIET=1; shift ;;
    --) shift; CMD=("$@"); break ;;
    *) echoerr "Unknown arg: $1"; usage ;;
  esac
done

[ -z "$HOST" ] || [ -z "$PORT" ] && usage

wait_for

if [ ${#CMD[@]} -gt 0 ]; then
  exec "${CMD[@]}"
fi
