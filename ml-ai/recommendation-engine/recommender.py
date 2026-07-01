# =============================================================
# NearKart - Recommendation Engine
# Approach: Hybrid (Collaborative Filtering + Content-Based)
# Stack: Surprise SVD + TF-IDF cosine similarity
# =============================================================

import pandas as pd
import numpy as np
from surprise import Dataset, Reader, SVD, accuracy
from surprise.model_selection import cross_validate, train_test_split as surp_split
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.preprocessing import MinMaxScaler
import joblib
import os
import warnings

warnings.filterwarnings('ignore')


# ----------------------------------------------------------------
# 1. Generate Synthetic Data
# ----------------------------------------------------------------
def generate_data(n_users=200, n_products=100, n_ratings=3000):
    np.random.seed(42)

    user_ids    = np.random.randint(1, n_users + 1, n_ratings)
    product_ids = np.random.randint(1, n_products + 1, n_ratings)
    ratings     = np.random.choice([1, 2, 3, 4, 5], n_ratings, p=[0.05, 0.1, 0.2, 0.35, 0.3])
    ratings_df  = pd.DataFrame({'user_id': user_ids, 'product_id': product_ids, 'rating': ratings})
    ratings_df  = ratings_df.drop_duplicates(subset=['user_id', 'product_id'])

    categories  = ['Groceries', 'Dairy', 'Snacks', 'Beverages', 'Personal Care',
                   'Household', 'Bakery', 'Frozen', 'Organic', 'Baby']
    brands      = ['Amul', 'Nestle', 'Parle', 'Britannia', 'ITC', 'HUL', 'P&G', 'Dabur']

    products_df = pd.DataFrame({
        'product_id': range(1, n_products + 1),
        'name':       [f'Product_{i}' for i in range(1, n_products + 1)],
        'category':   np.random.choice(categories, n_products),
        'brand':      np.random.choice(brands, n_products),
        'price':      np.round(np.random.uniform(10, 500, n_products), 2),
        'avg_rating': np.round(np.random.uniform(3.0, 5.0, n_products), 1)
    })
    products_df['description'] = (products_df['category'] + ' ' +
                                   products_df['brand']   + ' ' +
                                   products_df['name'])
    return ratings_df, products_df


# ----------------------------------------------------------------
# 2. Collaborative Filtering with SVD
# ----------------------------------------------------------------
def train_collaborative(ratings_df: pd.DataFrame):
    reader  = Reader(rating_scale=(1, 5))
    data    = Dataset.load_from_df(ratings_df[['user_id', 'product_id', 'rating']], reader)

    trainset, testset = surp_split(data, test_size=0.2, random_state=42)

    model = SVD(n_factors=50, n_epochs=30, lr_all=0.005, reg_all=0.02, random_state=42)
    model.fit(trainset)

    predictions = model.test(testset)
    rmse = accuracy.rmse(predictions, verbose=False)
    print(f"[Collaborative SVD] RMSE: {rmse:.4f}")

    os.makedirs('models', exist_ok=True)
    joblib.dump(model, 'models/svd_model.pkl')
    joblib.dump(trainset, 'models/trainset.pkl')
    print("[\u2713] SVD model saved.")
    return model, trainset


# ----------------------------------------------------------------
# 3. Content-Based Filtering with TF-IDF
# ----------------------------------------------------------------
def build_content_model(products_df: pd.DataFrame):
    tfidf = TfidfVectorizer(stop_words='english', ngram_range=(1, 2))
    tfidf_matrix = tfidf.fit_transform(products_df['description'])
    cosine_sim   = cosine_similarity(tfidf_matrix, tfidf_matrix)

    joblib.dump(cosine_sim, 'models/cosine_sim.pkl')
    products_df.to_csv('models/products.csv', index=False)
    print("[\u2713] Content-based model built and saved.")
    return cosine_sim


# ----------------------------------------------------------------
# 4. Get Content-Based Recommendations
# ----------------------------------------------------------------
def content_recommendations(product_id: int, products_df: pd.DataFrame,
                             cosine_sim: np.ndarray, top_n: int = 5) -> pd.DataFrame:
    idx_arr = products_df.index[products_df['product_id'] == product_id]
    if len(idx_arr) == 0:
        raise ValueError(f"product_id {product_id} not found in catalog.")
    idx      = idx_arr[0]
    scores   = list(enumerate(cosine_sim[idx]))
    scores   = sorted(scores, key=lambda x: x[1], reverse=True)
    top_idx  = [i for i, _ in scores[1:top_n + 1]]
    return products_df.iloc[top_idx][['product_id', 'name', 'category', 'brand', 'price']]


# ----------------------------------------------------------------
# 5. Get Collaborative Recommendations
# ----------------------------------------------------------------
def collaborative_recommendations(user_id: int, products_df: pd.DataFrame,
                                   model, ratings_df: pd.DataFrame,
                                   top_n: int = 5) -> pd.DataFrame:
    rated_products   = set(ratings_df[ratings_df['user_id'] == user_id]['product_id'])
    all_products     = set(products_df['product_id'])
    unrated_products = all_products - rated_products

    preds = [(pid, model.predict(user_id, pid).est) for pid in unrated_products]
    preds = sorted(preds, key=lambda x: x[1], reverse=True)[:top_n]

    top_ids = [p[0] for p in preds]
    return products_df[products_df['product_id'].isin(top_ids)][['product_id', 'name', 'category', 'price']]


# ----------------------------------------------------------------
# 6. Hybrid Recommendation (Weighted Blend)
# ----------------------------------------------------------------
def hybrid_recommendations(user_id: int, product_id: int,
                            ratings_df: pd.DataFrame, products_df: pd.DataFrame,
                            collab_weight: float = 0.6,
                            content_weight: float = 0.4,
                            top_n: int = 5) -> pd.DataFrame:
    """
    Blends collaborative and content-based scores.
    Handles cold-start: if new user, fallback to content-based only.
    FIX: Added product_id existence check before indexing to prevent IndexError.
    """
    # FIX: Validate product_id exists before any indexing operation
    if product_id not in products_df['product_id'].values:
        raise ValueError(f"product_id {product_id} not found in catalog.")

    model      = joblib.load('models/svd_model.pkl')
    cosine_sim = joblib.load('models/cosine_sim.pkl')

    rated = ratings_df[ratings_df['user_id'] == user_id]
    is_cold_start = len(rated) < 3

    if is_cold_start:
        print(f"[Cold Start] User {user_id} has few ratings. Using content-based only.")
        return content_recommendations(product_id, products_df, cosine_sim, top_n)

    all_pids  = products_df['product_id'].tolist()
    cf_scores = {pid: model.predict(user_id, pid).est for pid in all_pids}

    ref_idx   = products_df.index[products_df['product_id'] == product_id][0]
    cb_scores = {products_df.iloc[i]['product_id']: cosine_sim[ref_idx][i]
                 for i in range(len(products_df))}

    cf_vals   = np.array([cf_scores[p] for p in all_pids])
    cb_vals   = np.array([cb_scores[p] for p in all_pids])
    cf_norm   = (cf_vals - cf_vals.min()) / (cf_vals.max() - cf_vals.min() + 1e-9)
    cb_norm   = (cb_vals - cb_vals.min()) / (cb_vals.max() - cb_vals.min() + 1e-9)
    hybrid    = collab_weight * cf_norm + content_weight * cb_norm

    top_idx   = np.argsort(hybrid)[::-1][:top_n]
    top_pids  = [all_pids[i] for i in top_idx]

    return products_df[products_df['product_id'].isin(top_pids)][
        ['product_id', 'name', 'category', 'brand', 'price']
    ]


# ----------------------------------------------------------------
# Main
# ----------------------------------------------------------------
if __name__ == '__main__':
    print("=== NearKart Recommendation Engine ===")
    ratings_df, products_df = generate_data()

    model, trainset = train_collaborative(ratings_df)
    cosine_sim      = build_content_model(products_df)

    print("\n[Content-Based] Similar to Product 5:")
    print(content_recommendations(5, products_df, cosine_sim))

    print("\n[Collaborative] Top picks for User 10:")
    print(collaborative_recommendations(10, products_df, model, ratings_df))

    print("\n[Hybrid] Recommendations for User 10 viewing Product 5:")
    print(hybrid_recommendations(10, 5, ratings_df, products_df))
