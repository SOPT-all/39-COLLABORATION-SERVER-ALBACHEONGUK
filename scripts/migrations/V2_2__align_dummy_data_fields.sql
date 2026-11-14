-- (데이터) 더미데이터 양식의 지원 가능 여부와 상세 컴퓨터 활용 항목을 저장한다.
ALTER TABLE alba.job_posting
    ADD COLUMN IF NOT EXISTS in_service_apply BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS preferred_computer_skills VARCHAR(500);

-- (데이터) 기존 자료는 이전 추천 판정(지원방법 온라인 여부)을 새 필드에 옮긴다.
UPDATE alba.job_posting
SET in_service_apply = COALESCE(apply_method ILIKE '%온라인%', FALSE)
WHERE in_service_apply IS TRUE;

-- (데이터) 급여 협의 공고는 금액을 NULL로 표현한다.
ALTER TABLE alba.job_posting ALTER COLUMN pay_amount DROP NOT NULL;
UPDATE alba.job_posting SET pay_amount = NULL WHERE pay_amount = 0;

-- (데이터) 이력서의 복수 경력과 구체적인 컴퓨터 활용 항목.
ALTER TABLE alba.resume ADD COLUMN IF NOT EXISTS computer_skills VARCHAR(500);
CREATE TABLE IF NOT EXISTS alba.resume_career (
    resume_id BIGINT NOT NULL REFERENCES alba.resume(id) ON DELETE CASCADE,
    career_category_code VARCHAR(20) NOT NULL,
    career_months INTEGER NOT NULL CHECK (career_months >= 0),
    PRIMARY KEY (resume_id, career_category_code)
);

-- (데이터) 기존 단일 경력을 새 컬렉션으로 옮기고 기존 컬럼은 호환을 위해 보존한다.
INSERT INTO alba.resume_career (resume_id, career_category_code, career_months)
SELECT id, career_category_code, career_months
FROM alba.resume
WHERE career_category_code IS NOT NULL AND career_months > 0
ON CONFLICT (resume_id, career_category_code) DO NOTHING;

-- (데이터) 지원 불가 상태는 enum 추가 후 신규 적재에서 사용한다.
