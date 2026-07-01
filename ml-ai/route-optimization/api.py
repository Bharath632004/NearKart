# =============================================================
# NearKart - Route Optimization FastAPI Service
# Run: uvicorn api:app --reload --port 8004
# =============================================================

import sys, os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Optional
import pandas as pd

app = FastAPI(title="NearKart Route Optimization API", version="1.0")


class Location(BaseModel):
    stop_id:    int
    label:      str
    lat:        float
    lon:        float
    demand_kg:  float = 0.0


class RouteRequest(BaseModel):
    locations:  List[Location]      # first item must be the depot (stop_id=0)
    n_vehicles: int = 1


class SingleVehicleRequest(BaseModel):
    n_stops:  int = 10              # use synthetic data
    seed:     int = 42


@app.get("/")
def root():
    return {"message": "NearKart Route Optimization API is live"}


@app.get("/health")
def health():
    return {"status": "ok"}


@app.get("/demo")
def demo_route(n_stops: int = 10, n_vehicles: int = 2):
    """
    Demo endpoint: generates synthetic delivery locations and runs
    nearest-neighbor + 2-opt optimization for n_vehicles.
    """
    try:
        from route_optimizer import (
            generate_delivery_locations, build_distance_matrix,
            nearest_neighbor, two_opt, multi_vehicle_routing
        )

        df          = generate_delivery_locations(n=n_stops)
        dist_matrix = build_distance_matrix(df)
        route_nn, dist_nn   = nearest_neighbor(dist_matrix, start=0)
        route_opt, dist_opt = two_opt(route_nn, dist_matrix)

        routes_info = multi_vehicle_routing(df, n_vehicles=n_vehicles) if n_vehicles > 1 else None

        return {
            "single_vehicle": {
                "nearest_neighbor_route":    route_nn,
                "nearest_neighbor_dist_km":  dist_nn,
                "optimized_route":           route_opt,
                "optimized_dist_km":         dist_opt,
                "saving_km":                 round(dist_nn - dist_opt, 3)
            },
            "multi_vehicle": routes_info
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/optimize")
def optimize_custom_route(req: RouteRequest):
    """
    Accepts custom delivery locations and returns optimized route.
    First location in the list must be the depot (demand_kg = 0).
    """
    try:
        from route_optimizer import (
            build_distance_matrix, nearest_neighbor,
            two_opt, multi_vehicle_routing
        )
        import pandas as pd

        df = pd.DataFrame([loc.dict() for loc in req.locations])

        if df.empty or df.iloc[0]['stop_id'] != 0:
            raise ValueError("First location must be the depot with stop_id=0")

        dist_matrix = build_distance_matrix(df)

        if req.n_vehicles == 1:
            route_nn, dist_nn   = nearest_neighbor(dist_matrix, start=0)
            route_opt, dist_opt = two_opt(route_nn, dist_matrix)
            return {
                "optimized_route": route_opt,
                "total_dist_km":   dist_opt,
                "saving_km":       round(dist_nn - dist_opt, 3)
            }
        else:
            routes_info = multi_vehicle_routing(df, n_vehicles=req.n_vehicles)
            return routes_info

    except ValueError as ve:
        raise HTTPException(status_code=422, detail=str(ve))
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
