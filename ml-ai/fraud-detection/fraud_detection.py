# =============================================================
# NearKart - Fraud Detection Module
# Approach: Isolation Forest (anomaly) + XGBoost (supervised)
# Serves predictions via FastAPI
# =============================================================

import pandas as pd
import numpy as np
from sklearn.ensemble import IsolationForest, RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler, LabelEncoder
from sklearn.metrics import (classification_report, confusion_matrix,
                              roc_auc_score)
from imblearn.over_sampling import SMOTE
import xgboost as xgb
import joblib
import os

# ----------------------------------------------------------------
# 1. Synthetic Transaction Data Generator
# ----------------------------------------------------------------
def generate_transactions(n: int = 5000) -> pd.DataFrame:
    """
    Simulates NearKart payment transaction records.
    ~5% are labelled as fraudulent.
    """
    np.random.seed(42)
    n_fraud  = int(n * 0.05)
    n_legit  = n - n_fraud

    legit = pd.DataFrame({
        'amount':           np.random.uniform(50, 2000, n_legit),
        'hour_of_day':      np.random.randint(6, 23, n_legit),
        'day_of_week':      np.random.randint(0, 7, n_legit),
        'distance_km':      np.random.uniform(0, 20, n_legit),
        'is_new_account':   np.random.choice([0, 1], n_legit, p=[0.85, 0.15]),
        'failed_attempts':  np.random.randint(0, 3, n_legit),
        'payment_method':   np.random.choice(['UPI', 'Card', 'COD', 'Wallet'], n_legit),
        'geo_mismatch':     np.random.choice([0, 1], n_legit, p=[0.95, 0.05]),
        'is_fraud':         0
    })

    fraud = pd.DataFrame({
        'amount':           np.random.uniform(1500, 10000, n_fraud),
        'hour_of_day':      np.random.randint(0, 5, n_fraud),
        'day_of_week':      np.random.randint(0, 7, n_fraud),
        'distance_km':      np.random.uniform(50, 500, n_fraud),
        'is_new_account':   np.random.choice([0, 1], n_fraud, p=[0.3, 0.7]),
        'failed_attempts':  np.random.randint(3, 10, n_fraud),
        'payment_method':   np.random.choice(['UPI', 'Card', 'COD', 'Wallet'], n_fraud),
        'geo_mismatch':     np.random.choice([0, 1], n_fraud, p=[0.2, 0.8]),
        'is_fraud':         1
    })

    return pd.concat([legit, fraud], ignore_index=True).sample(frac=1, random_state=42)


# ----------------------------------------------------------------
# 2. Preprocessing
# ----------------------------------------------------------------
def preprocess(df: pd.DataFrame):
    df = df.copy()
    le = LabelEncoder()
    df['payment_method'] = le.fit_transform(df['payment_method'])

    feature_cols = ['amount', 'hour_of_day', 'day_of_week', 'distance_km',
                    'is_new_account', 'failed_attempts', 'payment_method',
                    'geo_mismatch']
    X = df[feature_cols].values
    y = df['is_fraud'].values

    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)

    os.makedirs('models', exist_ok=True)
    joblib.dump(scaler, 'models/fraud_scaler.pkl')
    joblib.dump(le,     'models/fraud_label_encoder.pkl')

    return X_scaled, y, feature_cols


# ----------------------------------------------------------------
# 3. Rule-Based Pre-filter
# ----------------------------------------------------------------
def rule_based_flags(df: pd.DataFrame) -> pd.Series:
    """
    Fast hard-coded rules that flag obvious fraud before ML scoring.
    Returns a boolean Series: True = suspicious.
    FIX: Added parentheses around each compound & clause to fix
         operator precedence bug (| binds looser than & in Python).
    """
    return (
        (df['amount']          > 8000) |
        (df['failed_attempts'] >= 5)   |
        ((df['geo_mismatch'] == 1) & (df['distance_km'] > 100)) |
        ((df['hour_of_day'] <= 3)  & (df['is_new_account'] == 1))
    )


# ----------------------------------------------------------------
# 4. Isolation Forest (Unsupervised Anomaly Detection)
# ----------------------------------------------------------------
def train_isolation_forest(X: np.ndarray) -> IsolationForest:
    iso = IsolationForest(n_estimators=200, contamination=0.05, random_state=42)
    iso.fit(X)
    print("[\u2713] Isolation Forest trained.")
    return iso


# ----------------------------------------------------------------
# 5. XGBoost (Supervised Classification)
# ----------------------------------------------------------------
def train_xgboost(X: np.ndarray, y: np.ndarray):
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42, stratify=y
    )

    sm = SMOTE(random_state=42)
    X_res, y_res = sm.fit_resample(X_train, y_train)

    # FIX: Removed deprecated `use_label_encoder=False` (removed in XGBoost >= 1.6)
    model = xgb.XGBClassifier(
        n_estimators=300,
        max_depth=6,
        learning_rate=0.05,
        scale_pos_weight=20,
        eval_metric='logloss',
        random_state=42
    )
    model.fit(X_res, y_res)

    y_pred = model.predict(X_test)
    y_prob = model.predict_proba(X_test)[:, 1]

    print("\n[XGBoost Classification Report]")
    print(classification_report(y_test, y_pred, target_names=['Legit', 'Fraud']))
    print(f"ROC-AUC Score: {roc_auc_score(y_test, y_prob):.4f}")

    joblib.dump(model, 'models/fraud_xgboost.pkl')
    print("[\u2713] XGBoost model saved to models/fraud_xgboost.pkl")
    return model, X_test, y_test


# ----------------------------------------------------------------
# 6. Predict on New Transaction
# ----------------------------------------------------------------
def predict_fraud(transaction: dict) -> dict:
    """
    Predicts fraud probability for a single transaction dict.
    Returns: { 'fraud_probability': float, 'is_fraud': bool, 'risk_level': str }
    """
    model  = joblib.load('models/fraud_xgboost.pkl')
    scaler = joblib.load('models/fraud_scaler.pkl')
    le     = joblib.load('models/fraud_label_encoder.pkl')

    df = pd.DataFrame([transaction])
    df['payment_method'] = le.transform(df['payment_method'])

    feature_cols = ['amount', 'hour_of_day', 'day_of_week', 'distance_km',
                    'is_new_account', 'failed_attempts', 'payment_method', 'geo_mismatch']
    X = scaler.transform(df[feature_cols].values)
    prob = model.predict_proba(X)[0][1]

    risk = 'HIGH' if prob > 0.7 else ('MEDIUM' if prob > 0.4 else 'LOW')
    return {'fraud_probability': round(prob, 4), 'is_fraud': prob > 0.5, 'risk_level': risk}


# ----------------------------------------------------------------
# Main
# ----------------------------------------------------------------
if __name__ == '__main__':
    print("=== NearKart Fraud Detection ===")
    df       = generate_transactions(5000)
    X, y, _  = preprocess(df)

    iso_model           = train_isolation_forest(X)
    xgb_model, Xt, yt  = train_xgboost(X, y)

    sample_txn = {
        'amount': 9500, 'hour_of_day': 2, 'day_of_week': 6,
        'distance_km': 250, 'is_new_account': 1, 'failed_attempts': 7,
        'payment_method': 'Card', 'geo_mismatch': 1
    }
    result = predict_fraud(sample_txn)
    print(f"\n[Sample Prediction] {result}")
