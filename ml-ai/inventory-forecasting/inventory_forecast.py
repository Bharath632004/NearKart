# =============================================================
# NearKart - Inventory Forecasting Module
# Predicts stock depletion & calculates optimal reorder point
# Uses: ARIMA + Safety Stock formula (supply chain standard)
# =============================================================

import pandas as pd
import numpy as np
from statsmodels.tsa.arima.model import ARIMA
from statsmodels.tsa.stattools import adfuller
from sklearn.metrics import mean_absolute_error
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import warnings
import os
import joblib

warnings.filterwarnings('ignore')


# ----------------------------------------------------------------
# 1. Sample Inventory Dataset
# ----------------------------------------------------------------
def generate_inventory_data(n_products: int = 5, n_days: int = 180) -> pd.DataFrame:
    """
    Creates daily sales records for multiple products.
    """
    np.random.seed(42)
    records = []
    products = [f'PROD_{i:03d}' for i in range(1, n_products + 1)]
    dates    = pd.date_range('2024-01-01', periods=n_days)

    for pid in products:
        base_demand = np.random.uniform(20, 100)
        for i, d in enumerate(dates):
            demand  = base_demand + 10 * np.sin(2 * np.pi * i / 7) + np.random.normal(0, 5)
            demand  = max(0, demand)
            records.append({'date': d, 'product_id': pid,
                            'units_sold': round(demand),
                            'lead_time_days': np.random.randint(2, 7)})

    return pd.DataFrame(records)


# ----------------------------------------------------------------
# 2. Stationarity Check
# ----------------------------------------------------------------
def is_stationary(series: pd.Series) -> bool:
    result = adfuller(series.dropna())
    return result[1] < 0.05   # p-value < 0.05 → stationary


# ----------------------------------------------------------------
# 3. Train ARIMA per Product
# ----------------------------------------------------------------
def train_arima(series: pd.Series, order=(2, 1, 2)) -> ARIMA:
    model = ARIMA(series, order=order)
    fitted = model.fit()
    return fitted


# ----------------------------------------------------------------
# 4. Reorder Point Calculation
# ----------------------------------------------------------------
def calculate_reorder_point(avg_demand: float, lead_time: float,
                            std_demand: float, service_level_z: float = 1.65) -> dict:
    """
    Reorder Point = (Avg Demand × Lead Time) + Safety Stock
    Safety Stock  = Z × StdDev(Demand) × sqrt(Lead Time)
    Z = 1.65 for 95% service level.
    """
    safety_stock  = service_level_z * std_demand * np.sqrt(lead_time)
    reorder_point = (avg_demand * lead_time) + safety_stock
    eoq = np.sqrt((2 * avg_demand * 365 * 50) / 2)   # EOQ formula (order cost=50, holding=2)

    return {
        'avg_daily_demand':  round(avg_demand, 2),
        'safety_stock':      round(safety_stock, 2),
        'reorder_point':     round(reorder_point, 2),
        'eoq_units':         round(eoq, 2)
    }


# ----------------------------------------------------------------
# 5. Full Pipeline per Product
# ----------------------------------------------------------------
def forecast_inventory(df: pd.DataFrame, forecast_days: int = 14) -> pd.DataFrame:
    results = []
    os.makedirs('plots', exist_ok=True)
    os.makedirs('models', exist_ok=True)

    for pid, group in df.groupby('product_id'):
        series      = group.set_index('date')['units_sold'].asfreq('D').fillna(method='ffill')
        lead_time   = group['lead_time_days'].mean()
        avg_demand  = series.mean()
        std_demand  = series.std()

        # Train
        arima_model = train_arima(series)
        forecast    = arima_model.forecast(steps=forecast_days)
        forecast    = np.clip(forecast, 0, None)

        # Evaluate on last 14 days
        train_len   = len(series) - 14
        eval_model  = train_arima(series.iloc[:train_len])
        eval_fc     = eval_model.forecast(steps=14)
        mae         = mean_absolute_error(series.iloc[train_len:], eval_fc)

        # Reorder metrics
        metrics     = calculate_reorder_point(avg_demand, lead_time, std_demand)

        results.append({
            'product_id':    pid,
            'forecast_14d':  round(forecast.sum(), 0),
            'mae':           round(mae, 2),
            **metrics
        })

        # Save model
        joblib.dump(arima_model, f'models/arima_{pid}.pkl')

        # Plot
        plt.figure(figsize=(10, 4))
        plt.plot(series.index, series.values, label='Historical', color='steelblue')
        future_dates = pd.date_range(series.index[-1], periods=forecast_days + 1, freq='D')[1:]
        plt.plot(future_dates, forecast, label='Forecast', color='orange', linestyle='--')
        plt.axhline(metrics['reorder_point'], color='red', linestyle=':', label='Reorder Point')
        plt.title(f'NearKart Inventory Forecast - {pid}')
        plt.xlabel('Date'); plt.ylabel('Units Sold')
        plt.legend(); plt.tight_layout()
        plt.savefig(f'plots/inventory_{pid}.png', dpi=120)
        plt.close()

    result_df = pd.DataFrame(results)
    print("\n[Inventory Forecast Results]")
    print(result_df.to_string(index=False))
    result_df.to_csv('inventory_forecast_results.csv', index=False)
    return result_df


# ----------------------------------------------------------------
# Main
# ----------------------------------------------------------------
if __name__ == '__main__':
    print("=== NearKart Inventory Forecasting ===")
    df = generate_inventory_data(n_products=5, n_days=180)
    forecast_inventory(df, forecast_days=14)
    print("[✓] Done. Check plots/ folder and inventory_forecast_results.csv")
