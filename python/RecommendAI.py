from pydantic import BaseModel


class RecommendResponse(BaseModel):
    comment: str

def build_prompt(currentCalories: int, targetCalories: int, dietGoal: str) -> str:
    percentage = currentCalories / targetCalories * 100
    remaining = targetCalories - currentCalories

    return f"""당신은 친근한 영양사입니다. 아래 사용자의 오늘 식단 현황을 보고 따뜻하고 동기부여가 되는 코멘트를 작성해주세요.

            - 목표: {dietGoal}
            - 목표 칼로리: {targetCalories:,}kcal
            - 현재 섭취 칼로리: {currentCalories:,}kcal
            - 달성률: {percentage:.1f}%
            - 남은 칼로리: {abs(remaining):,}kcal ({'초과' if remaining < 0 else '남음'})
            
            조건:
            - 2~3문장 이내로 간결하게
            - 이모지 1~2개 포함
            - 비판하지 말고 긍정적으로
            - 한국어로 작성"""