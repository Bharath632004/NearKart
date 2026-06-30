# Fraud Detection

Detects fraudulent orders, fake accounts, and suspicious payment patterns.

## Approach
- Isolation Forest (anomaly detection)
- XGBoost (supervised classification)
- Rule-based filters (velocity checks, geo-mismatch)

## Stack
- Python, Scikit-learn, XGBoost
- FastAPI (real-time scoring API)
- PostgreSQL (transaction features)
