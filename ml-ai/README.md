# NearKart ML/AI Modules

This directory contains all machine learning and AI components powering NearKart's intelligent features.

## Modules

| Module | Folder | Algorithm | API Port |
|--------|--------|-----------|----------|
| Demand Prediction | `demand-prediction/` | Facebook Prophet (time-series) | — |
| Fraud Detection | `fraud-detection/` | XGBoost + Isolation Forest | 8001 |
| Inventory Forecasting | `inventory-forecasting/` | ARIMA + Safety Stock Formula | — |
| Recommendation Engine | `recommendation-engine/` | SVD (Collaborative) + TF-IDF (Content) | 8002 |
| Route Optimization | `route-optimization/` | Nearest Neighbor + 2-opt TSP | — |

## Getting Started

```bash
# Install dependencies for a module
cd ml-ai/<module-name>
pip install -r requirements.txt

# Run the module
python <module_script>.py

# Start FastAPI service (fraud-detection / recommendation-engine)
uvicorn api:app --reload --port 8001
```

## Tech Stack
- Python 3.11, scikit-learn, XGBoost, Prophet, Surprise
- FastAPI for model serving
- PostgreSQL for production data
- AWS SageMaker for model deployment

## Architecture
```
NearKart Backend (Spring Boot)
        │
        ▼
  ML Service Layer (FastAPI)
        │
   ┌────┴────┐
   │  Models │ (.pkl / .h5 stored in models/)
   └─────────┘
```
