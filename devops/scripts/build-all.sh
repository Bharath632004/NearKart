#!/usr/bin/env bash
# ═══════════════════════════════════════════════════
#  NearKart – Build all Spring Boot services
#  Run from repo root: bash devops/scripts/build-all.sh
# ═══════════════════════════════════════════════════
set -e

SERVICES=(
  discovery-server
  auth-service
  user-service
  product-service
  order-service
  shop-service
  merchant-service
  inventory-service
  delivery-service
  payment-service
  notification-service
  analytics-service
  admin-service
  api-gateway
)

echo "🔨  Building all NearKart backend services..."
for svc in "${SERVICES[@]}"; do
  echo "──────────────────────────────"
  echo "▶  Building: $svc"
  (cd "backend/$svc" && mvn clean package -DskipTests -q)
  echo "✅  Done: $svc"
done
echo ""
echo "🎉  All services built successfully!"
