from fastapi import FastAPI
from recommender import FoodRecommender

app = FastAPI()
recommender = FoodRecommender()

@app.get("/ai/recommend/{user_no}")
async def get_recommendation(user_no: int):
    # 서비스 로직 호출
    result = recommender.recommend_meal(user_no)
    return {"status": "success", "data": result}