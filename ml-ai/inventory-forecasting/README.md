# Inventory Forecasting

Predicts stock requirements for merchants to avoid stockouts or overstock.

## Approach
- ARIMA / SARIMA (seasonal patterns)
- Exponential Smoothing (Holt-Winters)
- Feature inputs: sales history, promotions, seasonal events

## Stack
- Python, Statsmodels
- FastAPI (serving)
- PostgreSQL (historical data)
