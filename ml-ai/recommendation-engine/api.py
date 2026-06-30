# =============================================================
# NearKart - Recommendation Engine FastAPI Service
# Run: uvicorn api:app --reload --port 8002
# =============================================================

from fastapi import FastAPI, HTTPException
import pandas as pd
import joblib
from recommender import hybrid_recommendations, content_recommendations

app = FastAPI(title="NearKart Recommendation API", version="1.0")

# Load artifacts at startup
products_df = pd.read_csv('models/products.csv')

@app.get("/")
def root():
    return {"message": "NearKart Recommendation Engine API is live"}

@app.get("/recommend/hybrid")
def recommend_hybrid(user_id: int, product_id: int, top_n: int = 5):
    try:
        from recommender import generate_data
        ratings_df, _ = generate_data()
        result = hybrid_recommendations(user_id, product_id, ratings_df, products_df, top_n=top_n)
        return result.to_dict(orient='records')
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/recommend/content")
def recommend_content(product_id: int, top_n: int = 5):
    try:
        cosine_sim = joblib.load('models/cosine_sim.pkl')
        result = content_recommendations(product_id, products_df, cosine_sim, top_n)
        return result.to_dict(orient='records')
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/health")
def health():
    return {"status": "ok"}
