DROP SCHEMA IF EXISTS userinfo  CASCADE ;
CREATE SCHEMA IF NOT EXISTS userinfo;

CREATE TABLE IF NOT EXISTS userinfo.users (
                          id VARCHAR(255) PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          email VARCHAR(320) NOT NULL UNIQUE,
                          password VARCHAR(255) NOT NULL,
                          profile_type VARCHAR(32) NOT NULL,
                          created_at TIMESTAMPTZ NOT NULL DEFAULT now()
                                          );


CREATE TYPE userinfo.nexus_basis AS ENUM (
    'NONE',
    'PHYSICAL_PRESENCE',
    'ECONOMIC_NEXUS',
    'AFFILIATE_NEXUS',
    'MARKET_BASED_SOURCING_ONLY'
    );


CREATE TABLE IF NOT EXISTS userinfo.legal_entity
(
    id                      UUID PRIMARY KEY                  DEFAULT gen_random_uuid(),
    legal_name              TEXT                     NOT NULL,
    federal_employer_id     VARCHAR(10)              NOT NULL REFERENCES userinfo.users (id) ON DELETE RESTRICT ,
    fiscal_year_end_month   SMALLINT                 NOT NULL CHECK (fiscal_year_end_month BETWEEN 1 AND 12),
    created_at              TIMESTAMPTZ              NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS userinfo.state_tax_jurisdiction
(
    id                               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    legal_entity_id                  UUID REFERENCES userinfo.legal_entity (id) ON DELETE CASCADE,
    state_code                       CHAR(2)          NOT NULL,
    jurisdiction_notes               TEXT,
    apportionment_factor_snapshot    NUMERIC(9, 6),
    nexus_basis                      userinfo.nexus_basis NOT NULL DEFAULT 'NONE',
    created_at                       TIMESTAMPTZ      NOT NULL DEFAULT now()
);

CREATE TYPE userinfo.filing_status AS ENUM (
    'DRAFT',
    'CALCULATED',
    'FILED',
    'AMENDED'
    );

CREATE TABLE IF NOT EXISTS userinfo.tax_filing
(
    id                      UUID PRIMARY KEY                  DEFAULT gen_random_uuid(),
    legal_entity_id         UUID                     NOT NULL REFERENCES userinfo.legal_entity (id) ON DELETE CASCADE,
    jurisdiction_id         UUID                     NOT NULL REFERENCES userinfo.state_tax_jurisdiction (id) ON DELETE CASCADE,
    tax_year                SMALLINT                 NOT NULL CHECK (tax_year BETWEEN 2000 AND 2100),
    taxable_income          NUMERIC(15, 2),
    apportioned_income      NUMERIC(15, 2),
    tax_rate                NUMERIC(7, 5),
    computed_tax            NUMERIC(15, 2),
    filing_status           userinfo.filing_status   NOT NULL DEFAULT 'DRAFT',
    filed_at                TIMESTAMPTZ,
    created_at              TIMESTAMPTZ              NOT NULL DEFAULT now(),
    UNIQUE (legal_entity_id, jurisdiction_id, tax_year)
);

