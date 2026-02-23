from fastapi import FastAPI

from RecommendAI import RecommendResponse, RecommendRequest, build_prompt, client
from recommender import Recommender

app = FastAPI()
recommender = Recommender()

# 하루 전체 추천 (페이지 로드용)
@app.get("/ai/recommend/daily")
async def get_daily_recommend(user_no: int = None, targetCal: int = None):
    result = recommender.recommend_daily(user_no=user_no, guest_cal=targetCal)
    return {"status": "success", "data": result}

# 한 끼 단일 추천 (새로고침용)
@app.get("/ai/recommend/single")
async def get_single_refresh(meal_type: str, user_no: int = None, targetCal: int = None):
    result = recommender.recommend_single(meal_type, {}, user_no, targetCal)
    return {"status": "success", "data": result}

# ai 코멘트
@app.post("/ai/recommend/comment") # , response_model=RecommendResponse)
async def recommend(req: RecommendRequest):
    prompt = build_prompt(req)

    return prompt
'''
    response = client.chat.completions.create(
        model="gpt-4o-mini",
        messages=[{"role": "user", "content": prompt}],
        max_tokens=200,
        temperature=0.8,
    )

    comment = response.choices[0].message.content.strip()
    return RecommendResponse(comment=comment)
'''

# uvicorn main:app --reload --port 8000