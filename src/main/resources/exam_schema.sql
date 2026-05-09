-- ============================================================
--  EduSmart — Advanced Exam Management Schema
--  Run this script once to add the new tables.
-- ============================================================

-- ------------------------------------------------------------
--  exam_question: one row per question in an exam
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS exam_question (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    exam_id        INT            NOT NULL,
    question_text  TEXT           NOT NULL,
    question_type  VARCHAR(20)    NOT NULL DEFAULT 'OPEN_ENDED',
                                            -- MCQ | OPEN_ENDED | TRUE_FALSE | SHORT_ANSWER
    correct_answer TEXT,
    max_points     DOUBLE         NOT NULL DEFAULT 20.0,
    order_index    INT            NOT NULL DEFAULT 0,
    options        TEXT,                    -- Pipe-separated choices for MCQ
    CONSTRAINT fk_eq_exam FOREIGN KEY (exam_id) REFERENCES exam(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
--  exam_submission: one row per student × question
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS exam_submission (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    exam_id          INT            NOT NULL,
    student_id       INT            NOT NULL,
    question_id      INT            NOT NULL,
    student_answer   TEXT,
    score            DOUBLE         NOT NULL DEFAULT 0.0,
    max_score        DOUBLE         NOT NULL DEFAULT 20.0,
    ai_feedback      TEXT,
    ai_confidence    DOUBLE         NOT NULL DEFAULT 0.0,   -- 0.0–1.0
    plagiarism_score DOUBLE         NOT NULL DEFAULT 0.0,   -- 0.0–100.0
    status           VARCHAR(30)    NOT NULL DEFAULT 'PENDING',
                                            -- PENDING | AI_GRADED | MANUALLY_GRADED | PLAGIARISM_FLAGGED
    submitted_at     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    graded_at        DATETIME,
    CONSTRAINT fk_es_exam     FOREIGN KEY (exam_id)     REFERENCES exam(id)     ON DELETE CASCADE,
    CONSTRAINT fk_es_student  FOREIGN KEY (student_id)  REFERENCES user(id)     ON DELETE CASCADE,
    CONSTRAINT fk_es_question FOREIGN KEY (question_id) REFERENCES exam_question(id) ON DELETE CASCADE,
    UNIQUE KEY uq_student_question (student_id, question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
--  Sample seed data (optional – comment out if not needed)
-- ------------------------------------------------------------
-- INSERT INTO exam_question (exam_id, question_text, question_type, correct_answer, max_points, order_index)
-- VALUES (1, 'Expliquez le concept de polymorphisme en Java.', 'OPEN_ENDED', NULL, 20.0, 0);

-- INSERT INTO exam_question (exam_id, question_text, question_type, correct_answer, max_points, order_index, options)
-- VALUES (1, 'Quel mot-clé permet l''héritage en Java ?', 'MCQ', 'extends', 5.0, 1, 'extends|implements|inherits|super');
