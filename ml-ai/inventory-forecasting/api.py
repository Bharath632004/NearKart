# =============================================================
# NearKart - Inventory Forecasting FastAPI Service
# Run: uvicorn api:app --reload --port 8003
# =============================================================

import sys, os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import Optional

app = FastAPI(title="NearKart Inventory Forecasting API", version="1.0")


class InventoryRequest(BaseModel):
    n_products:    int = 5
    n_days:        int = 180
    forecast_days: int = 14


class ReorderRequest(BaseModel):
    avg_demand:       float
    lead_time:        float
    std_demand:       float
    service_level_z:  float = 1.65  # 95% service level default


@app.get("/")
def root():
    return {"message": "NearKart Inventory Forecasting API is live"}


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/forecast")
def run_forecast(req: InventoryRequest):
    """
    Runs full ARIMA inventory forecast for all products.
    Returns per-product forecast, MAE, safety stock & reorder point.
    """
    try:
        from inventory_forecast import generate_inventory_data, forecast_inventory

        df     = generate_inventory_data(n_products=req.n_products, n_days=req.n_days)
        result = forecast_inventory(df, forecast_days=req.forecast_days)
        return result.to_dict(orient='records')

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/reorder-point")
def get_reorder_point(req: ReorderRequest):
    """
    Calculates reorder point and safety stock for given supply chain parameters.
    """
    try:
        from inventory_forecast import calculate_reorder_point

        result = calculate_reorder_point(
            avg_demand      = req.avg_demand,
            lead_time       = req.lead_time,
            std_demand      = req.std_demand,
            service_level_z = req.service_level_z
        )
        return result

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
