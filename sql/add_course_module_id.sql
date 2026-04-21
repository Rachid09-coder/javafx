-- Run this script once on your EduSmart MySQL database (same DB as in DbConnection).
-- Fixes: Unknown column 'module_id' in 'field list'

ALTER TABLE course ADD COLUMN module_id INT NULL;

-- Optional: index for lookups by module
-- CREATE INDEX idx_course_module_id ON course (module_id);

-- Optional: foreign key (only if module.id exists and types match; adjust ON DELETE if needed)
-- ALTER TABLE course ADD CONSTRAINT fk_course_module FOREIGN KEY (module_id) REFERENCES module (id);
