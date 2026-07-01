# =============================================================
# NearKart ML/AI — Master Runner
# Trains and validates all 5 ML modules in sequence.
# Run from repo root: python ml-ai/run_all.py
# =============================================================

import sys
import os
import time

RED   = '\033[91m'
GREEN = '\033[92m'
YEL   = '\033[93m'
BLUE  = '\033[94m'
RESET = '\033[0m'


def section(title: str):
    print(f"\n{BLUE}{'='*60}{RESET}")
    print(f"{BLUE}  {title}{RESET}")
    print(f"{BLUE}{'='*60}{RESET}")


def ok(msg): print(f"{GREEN}[✓] {msg}{RESET}")
def warn(msg): print(f"{YEL}[!] {msg}{RESET}")
def err(msg): print(f"{RED}[✗] {msg}{RESET}")


# ── 1. Demand Prediction ────────────────────────────────────
def run_demand_prediction():
    section("1 / 5  Demand Prediction  (Prophet)")
    sys.path.insert(0, os.path.join('ml-ai', 'demand-prediction'))
    try:
        from demand_prediction import (
            generate_sample_data, add_regressors,
            train_model, forecast, evaluate, save_model, plot_forecast
        )
        df      = generate_sample_data(365)
        df      = add_regressors(df)
        model   = train_model(df)
        fc_df   = forecast(model, df, periods=30)
        metrics = evaluate(model, df)
        save_model(model, 'ml-ai/demand-prediction/models/demand_prophet.pkl')
        plot_forecast(model, fc_df)
        ok(f"Demand Prediction done — MAE: {metrics['mae']:.2f}, RMSE: {metrics['rmse']:.2f}")
    except Exception as e:
        err(f"Demand Prediction failed: {e}")


# ── 2. Fraud Detection ──────────────────────────────────────
def run_fraud_detection():
    section("2 / 5  Fraud Detection  (XGBoost + Isolation Forest)")
    sys.path.insert(0, os.path.join('ml-ai', 'fraud-detection'))
    try:
        from fraud_detection import (
            generate_transactions, preprocess,
            train_isolation_forest, train_xgboost, predict_fraud
        )
        df      = generate_transactions(5000)
        X, y, _ = preprocess(df)
        train_isolation_forest(X)
        train_xgboost(X, y)

        sample = {
            'amount': 9500, 'hour_of_day': 2, 'day_of_week': 6,
            'distance_km': 250, 'is_new_account': 1, 'failed_attempts': 7,
            'payment_method': 'Card', 'geo_mismatch': 1
        }
        result = predict_fraud(sample)
        ok(f"Fraud Detection done — Sample prediction: {result}")
    except Exception as e:
        err(f"Fraud Detection failed: {e}")


# ── 3. Inventory Forecasting ────────────────────────────────
def run_inventory_forecasting():
    section("3 / 5  Inventory Forecasting  (ARIMA + Safety Stock)")
    sys.path.insert(0, os.path.join('ml-ai', 'inventory-forecasting'))
    try:
        from inventory_forecast import generate_inventory_data, forecast_inventory
        df     = generate_inventory_data(n_products=5, n_days=180)
        result = forecast_inventory(df, forecast_days=14)
        ok(f"Inventory Forecasting done — {len(result)} products processed.")
    except Exception as e:
        err(f"Inventory Forecasting failed: {e}")


# ── 4. Recommendation Engine ────────────────────────────────
def run_recommendation_engine():
    section("4 / 5  Recommendation Engine  (SVD + TF-IDF Hybrid)")
    sys.path.insert(0, os.path.join('ml-ai', 'recommendation-engine'))
    try:
        from recommender import (
            generate_data, train_collaborative,
            build_content_model, hybrid_recommendations
        )
        ratings_df, products_df = generate_data()
        model, _  = train_collaborative(ratings_df)
        cosine_sim = build_content_model(products_df)
        recs = hybrid_recommendations(10, 5, ratings_df, products_df)
        ok(f"Recommendation Engine done — {len(recs)} recommendations for User 10 x Product 5.")
    except Exception as e:
        err(f"Recommendation Engine failed: {e}")


# ── 5. Route Optimization ───────────────────────────────────
def run_route_optimization():
    section("5 / 5  Route Optimization  (Nearest Neighbor + 2-opt)")
    sys.path.insert(0, os.path.join('ml-ai', 'route-optimization'))
    try:
        from route_optimizer import (
            generate_delivery_locations, build_distance_matrix,
            nearest_neighbor, two_opt, multi_vehicle_routing, plot_routes
        )
        df          = generate_delivery_locations(n=12)
        dist_matrix = build_distance_matrix(df)
        route_nn, dist_nn   = nearest_neighbor(dist_matrix, start=0)
        route_opt, dist_opt = two_opt(route_nn, dist_matrix)
        routes_info = multi_vehicle_routing(df, n_vehicles=2)
        plot_routes(df, routes_info)
        ok(f"Route Optimization done — NN: {dist_nn} km → 2-opt: {dist_opt} km "
           f"(saved {round(dist_nn - dist_opt, 3)} km)")
    except Exception as e:
        err(f"Route Optimization failed: {e}")


# ── Entry Point ─────────────────────────────────────────────
if __name__ == '__main__':
    t0 = time.time()
    print(f"{BLUE}{'='*60}")
    print( "  NearKart ML/AI — Running All 5 Modules")
    print(f"{'='*60}{RESET}")

    run_demand_prediction()
    run_fraud_detection()
    run_inventory_forecasting()
    run_recommendation_engine()
    run_route_optimization()

    elapsed = round(time.time() - t0, 1)
    print(f"\n{GREEN}{'='*60}")
    print(f"  All modules completed in {elapsed}s")
    print(f"{'='*60}{RESET}")
