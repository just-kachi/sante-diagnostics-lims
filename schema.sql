-- =====================================================================
--  Sante Diagnostics LIMS — PostgreSQL Schema
-- =====================================================================
--  Target database : sante_lims  (or lims_db, per DatabaseConnection.java)
--  Deployment      :
--     1. CREATE DATABASE lims_db;
--     2. \c lims_db
--     3. \i schema.sql
--     4. Run the Java application
-- =====================================================================


-- ---------------------------------------------------------------------
--  USERS
--  Backs: UserService, AuthService, DatabaseSeeder
--  Roles used in code: SUPER_ADMIN, LAB_ATTENDANT, CUSTOMER
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id                      SERIAL          PRIMARY KEY,
    full_name               VARCHAR(150)    NOT NULL,
    email                   VARCHAR(150)    NOT NULL UNIQUE,
    password_hash           VARCHAR(255)    NOT NULL,
    role                    VARCHAR(20)     NOT NULL
        CHECK (role IN ('SUPER_ADMIN', 'LAB_ATTENDANT', 'CUSTOMER')),
    email_verified          BOOLEAN         NOT NULL DEFAULT FALSE,
    force_password_change   BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- ---------------------------------------------------------------------
--  EMAIL VERIFICATION TOKENS
--  Backs: EmailVerificationService
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS email_verification_tokens (
    id          SERIAL      PRIMARY KEY,
    user_id     INTEGER     NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(64) NOT NULL,
    expires_at  TIMESTAMP   NOT NULL,
    used        BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_email_tokens_user_id
    ON email_verification_tokens(user_id);


-- ---------------------------------------------------------------------
--  TEST TYPES (Custom Test Builder)
--  Backs: TestTypeService
--  Result formats expected by brief: NUMERIC, TEXT, PDF, IMAGE
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS test_types (
    id              SERIAL          PRIMARY KEY,
    name            VARCHAR(150)    NOT NULL,
    category        VARCHAR(80)     NOT NULL,
    price           NUMERIC(12, 2)  NOT NULL,
    tat_hours       INTEGER         NOT NULL CHECK (tat_hours > 0),
    result_format   VARCHAR(20)     NOT NULL,
    description     TEXT,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- ---------------------------------------------------------------------
--  TEST REQUESTS
--  Backs: TestRequestService, PaymentService
--  Statuses used in code:
--     request_status : ACTIVE, COMPLETED, CANCELLED
--     payment_status : UNPAID, PAID
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS test_requests (
    id                  SERIAL      PRIMARY KEY,
    customer_id         INTEGER     NOT NULL REFERENCES users(id),
    test_type_id        INTEGER     NOT NULL REFERENCES test_types(id),
    request_status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
        CHECK (request_status IN ('ACTIVE', 'COMPLETED', 'CANCELLED')),
    payment_status      VARCHAR(20) NOT NULL DEFAULT 'UNPAID'
        CHECK (payment_status IN ('UNPAID', 'PAID')),
    requested_at        TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estimated_ready_at  TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_test_requests_customer
    ON test_requests(customer_id);

CREATE INDEX IF NOT EXISTS idx_test_requests_test_type
    ON test_requests(test_type_id);


-- ---------------------------------------------------------------------
--  SAMPLES
--  Backs: SampleService
--  Statuses used in code (and brief): REQUESTED, COLLECTED, PROCESSING, READY
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS samples (
    id              SERIAL      PRIMARY KEY,
    request_id      INTEGER     NOT NULL REFERENCES test_requests(id) ON DELETE CASCADE,
    sample_status   VARCHAR(20) NOT NULL DEFAULT 'REQUESTED',
    updated_by      INTEGER              REFERENCES users(id) ON DELETE SET NULL,
    updated_at      TIMESTAMP            DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_samples_request_id
    ON samples(request_id);


-- ---------------------------------------------------------------------
--  RESULTS
--  Backs: ResultService, ResultValidationController
--  Statuses used in code: PENDING, UPLOADED, VALIDATED, REJECTED
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS results (
    id              SERIAL      PRIMARY KEY,
    request_id      INTEGER     NOT NULL REFERENCES test_requests(id) ON DELETE CASCADE,
    result_value    TEXT,
    result_status   VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (result_status IN ('PENDING', 'UPLOADED', 'VALIDATED', 'REJECTED')),
    uploaded_by     INTEGER              REFERENCES users(id) ON DELETE SET NULL,
    uploaded_at     TIMESTAMP,
    validated_by    INTEGER              REFERENCES users(id) ON DELETE SET NULL,
    validated_at    TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_results_request_id
    ON results(request_id);


-- ---------------------------------------------------------------------
--  RESULT FILES (PDF reports & medical images)
--  Backs: ResultService.uploadResult / getFilesForResult, FileStorageService
--  file_type values produced by FileStorageService: PDF, IMAGE, UNKNOWN
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS result_files (
    id          SERIAL          PRIMARY KEY,
    result_id   INTEGER         NOT NULL REFERENCES results(id) ON DELETE CASCADE,
    file_name   VARCHAR(255)    NOT NULL,
    file_path   TEXT            NOT NULL,
    file_type   VARCHAR(20)     NOT NULL,
    uploaded_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_result_files_result_id
    ON result_files(result_id);


-- ---------------------------------------------------------------------
--  PAYMENTS
--  Backs: TestRequestService.createRequest (inserts row),
--         PaymentService.markRequestAsPaid (updates row)
--  Statuses used in code: UNPAID, PAID
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS payments (
    id                  SERIAL          PRIMARY KEY,
    request_id          INTEGER         NOT NULL REFERENCES test_requests(id) ON DELETE CASCADE,
    amount              NUMERIC(12, 2)  NOT NULL,
    payment_status      VARCHAR(20)     NOT NULL DEFAULT 'UNPAID'
        CHECK (payment_status IN ('UNPAID', 'PAID')),
    marked_paid_by      INTEGER                  REFERENCES users(id) ON DELETE SET NULL,
    marked_paid_at      TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_payments_request_id
    ON payments(request_id);


-- ---------------------------------------------------------------------
--  AUDIT LOGS  (immutable per project brief)
--  Backs: AuditLogService
--  user_id is nullable to allow SYSTEM-originated entries (see service code:
--  statement.setNull(1, INTEGER) when userId <= 0).
--  No UPDATE/DELETE triggers are issued by application code, so the table
--  is append-only in practice. Revoke UPDATE/DELETE at the role level in
--  your deployment environment to enforce immutability at the DB layer.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_logs (
    id              SERIAL          PRIMARY KEY,
    user_id         INTEGER                  REFERENCES users(id) ON DELETE SET NULL,
    action          VARCHAR(80)     NOT NULL,
    entity_type     VARCHAR(40)     NOT NULL,
    entity_id       INTEGER         NOT NULL,
    description     TEXT,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_user_id
    ON audit_logs(user_id);

CREATE INDEX IF NOT EXISTS idx_audit_logs_entity
    ON audit_logs(entity_type, entity_id);


-- =====================================================================
--  End of schema
-- =====================================================================
