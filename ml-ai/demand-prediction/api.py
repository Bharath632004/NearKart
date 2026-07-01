# =============================================================
# NearKart - Demand Prediction FastAPI Service
# Run: uvicorn api:app --reload --port 8000
# =============================================================

import sys, os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import Optional
import pandas as pd
import joblib

app = FastAPI(title="NearKart Demand Prediction API", version="1.0")


class ForecastRequest(BaseModel):
    periods: int = 30  # number of days to forecast


@app.get("/")
def root():
    return {"message": "NearKart Demand Prediction API is live"}


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/forecast")
def get_forecast(req: ForecastRequest):
    """
    Returns demand forecast for the next `periods` days.
    Loads a pre-trained Prophet model from models/demand_prophet.pkl.
    Run demand_prediction.py first to generate the model.
    """
    try:
        from demand_prediction import load_model, forecast, add_regressors, generate_sample_data

        model = load_model('models/demand_prophet.pkl')
        df    = generate_sample_data(365)
        df    = add_regressors(df)
        fc_df = forecast(model, df, periods=req.periods)

        tail = fc_df[['ds', 'yhat', 'yhat_lower', 'yhat_upper']].tail(req.periods)
        tail['ds'] = tail['ds'].dt.strftime('%Y-%m-%d')
        return tail.to_dict(orient='records')

    except FileNotFoundError:
        raise HTTPException(
            status_code=503,
            detail="Model not found. Run `python demand_prediction.py` first to train and save the model."
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/train")
def train_model_endpoint():
    """
    Trains the Prophet model on synthetic data and saves it.
    POST /train to trigger re-training.
    """
    try:
        from demand_prediction import (
            generate_sample_data, add_regressors,
            train_model, save_model, evaluate
        )
        df      = generate_sample_data(365)
        df      = add_regressors(df)
        model   = train_model(df)
        metrics = evaluate(model, df)
        save_model(model)
        return {"status": "trained", "metrics": metrics}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
