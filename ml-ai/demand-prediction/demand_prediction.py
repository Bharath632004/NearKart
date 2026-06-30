# =============================================================
# NearKart - Demand Prediction Module
# Uses Facebook Prophet for time-series forecasting
# Features: historical orders, weather, events, holidays
# =============================================================

import pandas as pd
import numpy as np
from prophet import Prophet
from sklearn.metrics import mean_absolute_error, mean_squared_error
import matplotlib.pyplot as plt
import warnings
import joblib
import os

warnings.filterwarnings('ignore')

# ----------------------------------------------------------------
# 1. Synthetic Data Generator (replace with real DB data)
# ----------------------------------------------------------------
def generate_sample_data(n_days: int = 365) -> pd.DataFrame:
    """
    Generates synthetic daily order data with trend, seasonality,
    and random noise — simulating NearKart's order volume.
    """
    np.random.seed(42)
    dates = pd.date_range(start='2023-01-01', periods=n_days, freq='D')
    trend = np.linspace(100, 300, n_days)
    seasonality = 50 * np.sin(2 * np.pi * np.arange(n_days) / 7)   # weekly
    monthly = 30 * np.sin(2 * np.pi * np.arange(n_days) / 30)       # monthly
    noise = np.random.normal(0, 15, n_days)

    orders = trend + seasonality + monthly + noise
    orders = np.clip(orders, 10, None)  # no negative orders

    df = pd.DataFrame({'ds': dates, 'y': orders})

    # Add holiday / event spikes (e.g., festivals, weekends)
    df.loc[df['ds'].dt.month == 10, 'y'] += 80   # Diwali season
    df.loc[df['ds'].dt.month == 1,  'y'] += 50   # New Year
    return df


# ----------------------------------------------------------------
# 2. Add Regressors (Weather, Events)
# ----------------------------------------------------------------
def add_regressors(df: pd.DataFrame) -> pd.DataFrame:
    """
    Adds external features that influence demand.
    In production, pull from weather API / event calendar.
    """
    np.random.seed(7)
    df = df.copy()
    df['temperature']   = np.random.uniform(20, 42, len(df))          # Celsius
    df['is_weekend']    = df['ds'].dt.dayofweek.isin([5, 6]).astype(int)
    df['is_festival']   = ((df['ds'].dt.month == 10) |
                           (df['ds'].dt.month == 1)).astype(int)
    df['discount_active'] = np.random.choice([0, 1], size=len(df), p=[0.7, 0.3])
    return df


# ----------------------------------------------------------------
# 3. Train Prophet Model
# ----------------------------------------------------------------
def train_model(df: pd.DataFrame) -> Prophet:
    """
    Trains a Prophet model with Indian holidays and custom regressors.
    """
    model = Prophet(
        yearly_seasonality=True,
        weekly_seasonality=True,
        daily_seasonality=False,
        changepoint_prior_scale=0.1,
        seasonality_prior_scale=10
    )

    # Add Indian holidays
    model.add_country_holidays(country_name='IN')

    # Add external regressors
    for col in ['temperature', 'is_weekend', 'is_festival', 'discount_active']:
        model.add_regressor(col)

    model.fit(df)
    print("[✓] Prophet model trained successfully.")
    return model


# ----------------------------------------------------------------
# 4. Forecast Future Demand
# ----------------------------------------------------------------
def forecast(model: Prophet, df: pd.DataFrame, periods: int = 30) -> pd.DataFrame:
    """
    Generates demand forecast for the next `periods` days.
    """
    future = model.make_future_dataframe(periods=periods)
    future = add_regressors(future)  # fill regressors for future dates

    forecast_df = model.predict(future)
    forecast_df['yhat'] = forecast_df['yhat'].clip(lower=0)

    print(f"\n[Forecast] Next {periods} days demand preview:")
    print(forecast_df[['ds', 'yhat', 'yhat_lower', 'yhat_upper']].tail(periods).to_string(index=False))
    return forecast_df


# ----------------------------------------------------------------
# 5. Evaluate Model
# ----------------------------------------------------------------
def evaluate(model: Prophet, df: pd.DataFrame) -> dict:
    """
    Evaluates model on historical data using MAE and RMSE.
    """
    forecast_df = model.predict(df)
    mae  = mean_absolute_error(df['y'], forecast_df['yhat'])
    rmse = np.sqrt(mean_squared_error(df['y'], forecast_df['yhat']))
    print(f"\n[Metrics] MAE: {mae:.2f} | RMSE: {rmse:.2f}")
    return {'mae': mae, 'rmse': rmse}


# ----------------------------------------------------------------
# 6. Save / Load Model
# ----------------------------------------------------------------
def save_model(model: Prophet, path: str = 'models/demand_prophet.pkl'):
    os.makedirs('models', exist_ok=True)
    joblib.dump(model, path)
    print(f"[✓] Model saved to {path}")


def load_model(path: str = 'models/demand_prophet.pkl') -> Prophet:
    model = joblib.load(path)
    print(f"[✓] Model loaded from {path}")
    return model


# ----------------------------------------------------------------
# 7. Plot Forecast
# ----------------------------------------------------------------
def plot_forecast(model: Prophet, forecast_df: pd.DataFrame):
    fig = model.plot(forecast_df)
    plt.title('NearKart - Demand Forecast (Next 30 Days)')
    plt.xlabel('Date')
    plt.ylabel('Orders')
    plt.tight_layout()
    os.makedirs('plots', exist_ok=True)
    plt.savefig('plots/demand_forecast.png', dpi=150)
    plt.close()
    print("[✓] Plot saved to plots/demand_forecast.png")


# ----------------------------------------------------------------
# Main
# ----------------------------------------------------------------
if __name__ == '__main__':
    print("=== NearKart Demand Prediction ===")
    df = generate_sample_data(365)
    df = add_regressors(df)

    model       = train_model(df)
    forecast_df = forecast(model, df, periods=30)
    metrics     = evaluate(model, df)
    save_model(model)
    plot_forecast(model, forecast_df)
