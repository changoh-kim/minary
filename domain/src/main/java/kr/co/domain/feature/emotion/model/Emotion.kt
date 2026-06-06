package kr.co.domain.feature.emotion.model

enum class Emotion {
    // 1. 긍정 & 활력 (Positive & High Energy)
    JOY,                    // 기쁨
    EXCITEMENT,             // 흥분됨, 신남
    TRIUMPH,                // 승리감
    AMUSEMENT,              // 즐거움

    // 2. 사랑 & 애정 (Love & Affection)
    ROMANCE,                // 로맨스
    ADORATION,              // 흠모
    SEXUAL_DESIRE,          // 성적 욕구

    // 3. 차분함 & 심미 (Calm & Aesthetic)
    CALMNESS,               // 차분함
    SATISFACTION,           // 만족
    AESTHETIC_APPRECIATION, // 심미적 감상
    ENTRANCEMENT,           // 황홀경

    // 4. 존경 & 관심 (Respect & Interest)
    ADMIRATION,             // 존경
    AWE,                    // 경외감
    INTEREST,               // 흥미, 호기심

    // 5. 슬픔 & 그리움 (Sadness & Longing)
    SADNESS,                // 슬픔
    NOSTALGIA,              // 향수, 그리움
    SYMPATHY,               // 공감
    EMPATHETIC_PAIN,        // 공감적 고통

    // 6. 부정 & 경계 (Negative & Alert)
    ANGER,                  // 분노
    FEAR,                   // 두려움
    HORROR,                 // 공포
    ANXIETY,                // 걱정

    // 7. 복합 & 모호 (Complex & Ambiguous)
    CONFUSION,              // 혼란스러움
    BOREDOM,                // 지루함
    AWKWARDNESS,            // 어색함
    DISGUST,                // 역겨움
    ENVY,                   // 부러움, 질투

    // 8. 욕구 (Desire)
    CRAVING,                // 간절함

    // 9. 미정 및 기본값 (Undefined / Default)
    UNKNOWN;                // 알 수 없음, 분석 전, 데이터 없음
}