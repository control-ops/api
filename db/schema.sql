CREATE TYPE time_unit AS ENUM (
    'milliseconds',
    'seconds',
    'minutes');

CREATE TYPE signal_unit AS ENUM (
    'celsius',
    'm3_per_hour',
    'percentage');

CREATE TABLE sensors (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    signal_unit signal_unit NOT NULL,
    sampling_period BIGINT NOT NULL,
    sampling_period_time_unit time_unit NOT NULL);
