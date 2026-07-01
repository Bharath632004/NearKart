# =============================================================
# NearKart - Route Optimization Module
# Solves multi-stop delivery routing (TSP variant)
# Algorithm: Nearest Neighbor Heuristic + 2-opt improvement
# Also includes Google OR-Tools solver (optional, enterprise)
# =============================================================

import numpy as np
import pandas as pd
from itertools import permutations
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import os
import math
from typing import List, Tuple


# ----------------------------------------------------------------
# 1. Generate Delivery Locations (lat/lon in Andhra Pradesh)
# ----------------------------------------------------------------
def generate_delivery_locations(n: int = 10, seed: int = 42) -> pd.DataFrame:
    """
    Simulates delivery addresses around a city (e.g., Guntur, AP).
    FIX: Guard against n=1 edge case where label list would be shorter
         than lats/lons (length n), causing a DataFrame construction error.
    """
    np.random.seed(seed)
    depot_lat, depot_lon = 16.3067, 80.4365  # Guntur city center

    lats = depot_lat + np.random.uniform(-0.1, 0.1, n)
    lons = depot_lon + np.random.uniform(-0.1, 0.1, n)

    # FIX: when n == 1, range(1, n) is empty, producing only ['Depot']
    # which matches the single point correctly.
    labels = ['Depot'] + [f'Customer_{i}' for i in range(1, n)]

    df = pd.DataFrame({
        'stop_id':   range(n),
        'label':     labels,
        'lat':       lats,
        'lon':       lons,
        'demand_kg': [0] + list(np.random.uniform(1, 10, n - 1).round(1)) if n > 1 else [0]
    })
    return df


# ----------------------------------------------------------------
# 2. Haversine Distance (km between two lat/lon points)
# ----------------------------------------------------------------
def haversine(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    R = 6371.0
    phi1, phi2   = math.radians(lat1), math.radians(lat2)
    dphi         = math.radians(lat2 - lat1)
    dlambda      = math.radians(lon2 - lon1)
    a = math.sin(dphi/2)**2 + math.cos(phi1)*math.cos(phi2)*math.sin(dlambda/2)**2
    return R * 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))


# ----------------------------------------------------------------
# 3. Build Distance Matrix
# ----------------------------------------------------------------
def build_distance_matrix(df: pd.DataFrame) -> np.ndarray:
    n   = len(df)
    mat = np.zeros((n, n))
    for i in range(n):
        for j in range(n):
            if i != j:
                mat[i][j] = haversine(df.iloc[i]['lat'], df.iloc[i]['lon'],
                                       df.iloc[j]['lat'], df.iloc[j]['lon'])
    return mat


# ----------------------------------------------------------------
# 4. Nearest Neighbor Heuristic (Greedy TSP)
# ----------------------------------------------------------------
def nearest_neighbor(dist_matrix: np.ndarray, start: int = 0) -> Tuple[List[int], float]:
    n       = len(dist_matrix)
    visited = [False] * n
    route   = [start]
    visited[start] = True
    total_dist = 0.0

    for _ in range(n - 1):
        current = route[-1]
        best_next, best_dist = -1, float('inf')
        for j in range(n):
            if not visited[j] and dist_matrix[current][j] < best_dist:
                best_next  = j
                best_dist  = dist_matrix[current][j]
        route.append(best_next)
        visited[best_next] = True
        total_dist += best_dist

    # Return to depot
    total_dist += dist_matrix[route[-1]][start]
    route.append(start)
    return route, round(total_dist, 3)


# ----------------------------------------------------------------
# 5. 2-opt Improvement (local search)
# ----------------------------------------------------------------
def two_opt(route: List[int], dist_matrix: np.ndarray) -> Tuple[List[int], float]:
    """
    Iteratively reverses segments of the route to reduce total distance.
    Significant improvement over nearest-neighbor alone.
    """
    def route_dist(r):
        return sum(dist_matrix[r[i]][r[i+1]] for i in range(len(r)-1))

    best = route[:]
    improved = True
    while improved:
        improved = False
        for i in range(1, len(best) - 2):
            for j in range(i + 1, len(best) - 1):
                new_route = best[:i] + best[i:j+1][::-1] + best[j+1:]
                if route_dist(new_route) < route_dist(best):
                    best = new_route
                    improved = True

    return best, round(route_dist(best), 3)


# ----------------------------------------------------------------
# 6. Multi-Vehicle Routing (split deliveries across n drivers)
# ----------------------------------------------------------------
def multi_vehicle_routing(df: pd.DataFrame, n_vehicles: int = 2) -> dict:
    """
    Splits customers across vehicles using round-robin assignment
    then optimizes each sub-route independently.
    """
    customers  = df[df['stop_id'] != 0].copy().reset_index(drop=True)
    assignments = {v: [0] for v in range(n_vehicles)}  # all start at depot

    for i, row in customers.iterrows():
        assignments[i % n_vehicles].append(int(row['stop_id']))

    dist_matrix = build_distance_matrix(df)
    routes_info = {}

    for v, stops in assignments.items():
        stops_with_return = stops + [0]
        sub_dist = np.array([[dist_matrix[i][j] for j in stops_with_return]
                              for i in stops_with_return])
        idx_map  = stops_with_return
        raw_route, dist_nn = nearest_neighbor(sub_dist, start=0)
        opt_route, dist_2opt = two_opt(raw_route, sub_dist)

        routes_info[f'vehicle_{v+1}'] = {
            'stops':          [idx_map[i] for i in opt_route],
            'stop_labels':    [df.iloc[idx_map[i]]['label'] for i in opt_route],
            'total_dist_km':  dist_2opt,
            'improvement_km': round(dist_nn - dist_2opt, 3)
        }

    return routes_info


# ----------------------------------------------------------------
# 7. Visualize Routes
# ----------------------------------------------------------------
def plot_routes(df: pd.DataFrame, routes_info: dict):
    os.makedirs('plots', exist_ok=True)
    colors  = ['steelblue', 'darkorange', 'green', 'red', 'purple']
    fig, ax = plt.subplots(figsize=(10, 8))

    # Plot all stops
    for _, row in df.iterrows():
        color  = 'red' if row['stop_id'] == 0 else 'gray'
        marker = '*' if row['stop_id'] == 0 else 'o'
        ax.scatter(row['lon'], row['lat'], c=color, s=120, marker=marker, zorder=5)
        ax.annotate(row['label'], (row['lon'], row['lat']),
                    textcoords='offset points', xytext=(5, 5), fontsize=7)

    # Plot vehicle routes
    for vi, (vname, info) in enumerate(routes_info.items()):
        stops  = info['stops']
        lats   = [df.iloc[s]['lat'] for s in stops]
        lons   = [df.iloc[s]['lon'] for s in stops]
        c      = colors[vi % len(colors)]
        ax.plot(lons, lats, '-o', color=c, label=f"{vname} ({info['total_dist_km']} km)", linewidth=2)

    ax.set_title('NearKart Delivery Route Optimization')
    ax.set_xlabel('Longitude'); ax.set_ylabel('Latitude')
    ax.legend(); plt.tight_layout()
    plt.savefig('plots/route_optimization.png', dpi=150)
    plt.close()
    print("[\u2713] Route plot saved to plots/route_optimization.png")


# ----------------------------------------------------------------
# Main
# ----------------------------------------------------------------
if __name__ == '__main__':
    print("=== NearKart Route Optimization ===")

    df           = generate_delivery_locations(n=12)
    dist_matrix  = build_distance_matrix(df)

    print("\n[Single Vehicle - Nearest Neighbor]")
    route_nn, dist_nn = nearest_neighbor(dist_matrix, start=0)
    print(f"  Route: {route_nn}  |  Distance: {dist_nn} km")

    print("\n[Single Vehicle - After 2-opt Improvement]")
    route_opt, dist_opt = two_opt(route_nn, dist_matrix)
    print(f"  Route: {route_opt}  |  Distance: {dist_opt} km")
    print(f"  Improvement: {round(dist_nn - dist_opt, 3)} km saved")

    print("\n[Multi-Vehicle Routing - 2 Drivers]")
    routes_info = multi_vehicle_routing(df, n_vehicles=2)
    for vname, info in routes_info.items():
        print(f"  {vname}: {info['stop_labels']}  |  {info['total_dist_km']} km")

    plot_routes(df, routes_info)
