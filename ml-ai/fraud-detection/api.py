# =============================================================
# NearKart - Fraud Detection FastAPI Service
# Run: uvicorn api:app --reload --port 8001
# =============================================================

import sys
import os
# FIX: Ensure the module directory is on sys.path so fraud_detection
#      can be imported regardless of the working directory.
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from fraud_detection import predict_fraud

app = FastAPI(title="NearKart Fraud Detection API", version="1.0")

class Transaction(BaseModel):
    amount:           float
    hour_of_day:      int
    day_of_week:      int
    distance_km:      float
    is_new_account:   int
    failed_attempts:  int
    payment_method:   str   # 'UPI' | 'Card' | 'COD' | 'Wallet'
    geo_mismatch:     int

@app.get("/")
def root():
    return {"message": "NearKart Fraud Detection API is live"}

@app.post("/predict")
def predict(txn: Transaction):
    try:
        result = predict_fraud(txn.dict())
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/health")
def health():
    return {"status": "ok"}
