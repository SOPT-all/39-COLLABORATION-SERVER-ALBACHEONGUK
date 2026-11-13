-- Existing postings are initialized as recruiting. Review records that were already completed
-- before running this migration and update their status as part of the deployment data check.
ALTER TABLE alba.job_posting
    ADD COLUMN recruitment_status VARCHAR(20) NOT NULL DEFAULT 'RECRUITING';
