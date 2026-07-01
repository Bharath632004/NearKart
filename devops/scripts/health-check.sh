#!/usr/bin/env bash
# ═══════════════════════════════════════════════════
#  NearKart – Container health check script
# ═══════════════════════════════════════════════════
set -e

declare -A SERVICES=(
  ["api-gateway"]="http://localhost:8080/actuator/health"
  ["auth-service"]="http://localhost:8084/actuator/health"
  ["user-service"]="http://localhost:8081/actuator/health"
  ["product-service"]="http://localhost:8082/actuator/health"
  ["order-service"]="http://localhost:8083/actuator/health"
  ["shop-service"]="http://localhost:8085/actuator/health"
  ["merchant-service"]="http://localhost:8086/actuator/health"
  ["inventory-service"]="http://localhost:8087/actuator/health"
  ["delivery-service"]="http://localhost:8088/actuator/health"
  ["payment-service"]="http://localhost:8089/actuator/health"
  ["notification-service"]="http://localhost:8090/actuator/health"
  ["analytics-service"]="http://localhost:8091/actuator/health"
  ["admin-service"]="http://localhost:8092/actuator/health"
  ["discovery-server"]="http://localhost:8761/actuator/health"
)

PASS=0
FAIL=0

echo "🔍  NearKart Service Health Check"
echo "──────────────────────────────────"
for svc in "${!SERVICES[@]}"; do
  URL="${SERVICES[$svc]}"
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" --max-time 5 "$URL" || echo "000")
  if [ "$STATUS" == "200" ]; then
    echo "  ✅  $svc"
    ((PASS++))
  else
    echo "  ❌  $svc  (HTTP $STATUS – $URL)"
    ((FAIL++))
  fi
done
echo "──────────────────────────────────"
echo "  ✅ UP: $PASS   ❌ DOWN: $FAIL"
[ "$FAIL" -gt 0 ] && exit 1 || exit 0
