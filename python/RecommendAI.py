from pydantic import BaseModel
from openai import OpenAI
from dotenv import load_dotenv
import random

load_dotenv()
client = OpenAI()

class RecommendRequest(BaseModel):
    currentCalories: int
    targetCalories: int
    dietGoal: str  # 예: "체중감량", "근육증가", "유지"

class RecommendResponse(BaseModel):
    comment: str

def build_prompt_ai(req: RecommendRequest) -> str:
    percentage = req.currentCalories / req.targetCalories * 100
    remaining = req.targetCalories - req.currentCalories

    return f"""
        당신은 냉철한 영양 분석가입니다. 아래 사용자의 오늘 식단 현황을 보고 사용자에 맞는 영양 분석 코멘트를 작성해주세요.
    
        - 목표: {req.dietGoal}
        - 목표 칼로리: {req.targetCalories:,}kcal
        - 현재 섭취 칼로리: {req.currentCalories:,}kcal
        - 달성률: {percentage:.1f}%
        - 남은 칼로리: {remaining:,}kcal ({'초과' if remaining < 0 else '남음'})
    
        조건:
        - 2~3문장 이내로 간결하게
        - 이모지 1~2개 포함
        - 비판하지 말고 긍정적으로
        - 한국어로 작성
        """.strip()

def build_prompt(req: RecommendRequest) -> str:
    percentage = req.currentCalories / req.targetCalories * 100
    remaining = req.targetCalories - req.currentCalories

    if percentage < 50:
        messages = [
            f"아직 {abs(remaining):,}kcal가 남았어요! 영양가 있는 간식으로 채워보세요 💪",
            f"목표의 {percentage:.0f}%를 달성하셨네요! 단백질 위주로 드시면 좋을 것 같아요 🥗",
        ]
    elif percentage < 90:
        messages = [
            f"거의 다 왔어요! {abs(remaining):,}kcal만 더 채우면 목표 달성이에요 👍",
            f"목표의 {percentage:.0f}% 달성! 저녁에 가볍게 마무리해보세요 🍽️",
        ]
    elif percentage <= 110:
        messages = [
            f"완벽해요! 오늘 목표 칼로리를 딱 맞게 채우셨네요 🎉",
            f"오늘 식단 관리 훌륭하세요! 이 페이스 유지해봐요 ✨",
        ]
    else:
        messages = [
            f"오늘은 {abs(remaining):,}kcal 초과했지만 괜찮아요, 내일 다시 시작하면 돼요 💚",
            f"완벽한 날만 있을 순 없죠! 내일 조금만 더 신경써봐요 😊",
        ]

    return random.choice(messages)