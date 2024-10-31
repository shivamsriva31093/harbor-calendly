-- Users and authentication
create schema if not exists stg;
drop table if exists stg.users cascade ;
CREATE TABLE stg.users
(
  id                       UUID PRIMARY KEY,
  email                    TEXT UNIQUE NOT NULL,
  first_name               TEXT        NOT NULL,
  last_name                TEXT        NOT NULL,
  timezone                 TEXT        NOT NULL,
  created_at               TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
  updated_at               TIMESTAMPTZ,
  notification_preferences JSONB       DEFAULT '{
    "email": true,
    "sms": false
  }'::jsonb
);

-- Availability settings
drop table if exists stg.availability_settings cascade ;
CREATE TABLE stg.availability_settings
(
  id          UUID PRIMARY KEY default gen_random_uuid(),
  user_id     UUID REFERENCES stg.users (id) ON DELETE CASCADE,
  day_of_week INTEGER NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
  start_time  TIME    NOT NULL,
  end_time    TIME    NOT NULL,
  created_at  TIMESTAMPTZ      DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT valid_time_range CHECK (start_time < end_time),
  UNIQUE (user_id, day_of_week, start_time, end_time)
);

CREATE INDEX idx_availability_user_day ON stg.availability_settings (user_id, day_of_week);

-- Availability exceptions (holidays, time off, etc.)
drop table if exists stg.availability_exceptions cascade ;
CREATE TABLE stg.availability_exceptions
(
  id                UUID PRIMARY KEY,
  user_id           UUID REFERENCES stg.users (id) ON DELETE CASCADE,
  date              DATE    NOT NULL,
  is_available      BOOLEAN NOT NULL,
  reason            TEXT,
  available_windows JSONB, -- Array of time slots if partially available
  created_at        TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (user_id, date)
);

-- Events/Meetings
drop table if exists stg.events cascade ;
CREATE TABLE stg.events
(
  id           UUID PRIMARY KEY,
  title        TEXT        NOT NULL,
  description  TEXT,
  start_time   TIMESTAMPTZ NOT NULL,
  end_time     TIMESTAMPTZ NOT NULL,
  organizer_id UUID REFERENCES stg.users (id) ON DELETE CASCADE,
  status       TEXT        NOT NULL CHECK (status IN ('SCHEDULED', 'CANCELLED', 'COMPLETED', 'RESCHEDULED')),
  created_at   TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
  updated_at   TIMESTAMPTZ,
  metadata     JSONB       DEFAULT '{}',
  CONSTRAINT valid_event_time CHECK (start_time < end_time)
);

CREATE INDEX idx_events_time_range ON stg.events (start_time, end_time);
CREATE INDEX idx_events_organizer ON stg.events (organizer_id);

-- Event participants and their responses
drop table if exists stg.event_participants cascade ;
CREATE TABLE stg.event_participants
(
  event_id                UUID REFERENCES stg.events (id) ON DELETE CASCADE,
  user_id                 UUID REFERENCES stg.users (id) ON DELETE CASCADE,
  response                TEXT NOT NULL CHECK (response IN ('PENDING', 'ACCEPTED', 'DECLINED', 'TENTATIVE')),
  notification_preference TEXT        DEFAULT 'EMAIL',
  created_at              TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
  updated_at              TIMESTAMPTZ,
  response_history        JSONB[], -- Array of previous responses with timestamps
  PRIMARY KEY (event_id, user_id)
);

CREATE INDEX idx_participant_user ON stg.event_participants (user_id);

-- Buffer settings for meetings
drop table if exists stg.buffer_settings cascade ;
CREATE TABLE stg.buffer_settings
(
  user_id                UUID REFERENCES stg.users (id) ON DELETE CASCADE PRIMARY KEY,
  before_meeting_minutes INTEGER NOT NULL DEFAULT 0,
  after_meeting_minutes  INTEGER NOT NULL DEFAULT 0,
  apply_to               TEXT[]           DEFAULT ARRAY ['external']::TEXT[], -- 'external', 'internal', 'all'
  created_at             TIMESTAMPTZ      DEFAULT CURRENT_TIMESTAMP,
  updated_at             TIMESTAMPTZ,
  CONSTRAINT valid_buffer_time CHECK (before_meeting_minutes >= 0 AND after_meeting_minutes >= 0)
);

-- Common indexes and functions
-- First enable btree_gist extension if not already enabled
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- Create the range index
CREATE INDEX idx_events_timerange ON stg.events USING GIST (
                                                            tstzrange(start_time, end_time)
  );

-- First remove the existing constraint if it exists
ALTER TABLE stg.events DROP CONSTRAINT IF EXISTS no_overlapping_events;

-- Add constraint to ensure no overlapping events for same user
ALTER TABLE stg.events
  ADD CONSTRAINT no_overlapping_events
    EXCLUDE USING GIST (
    tstzrange(start_time, end_time) WITH &&,
    organizer_id WITH =
    ) WHERE (status != 'CANCELLED');

-- Example querying overlapping events:
SELECT *
FROM stg.events
WHERE tstzrange(start_time, end_time) &&
      tstzrange('2024-01-01 00:00:00+00', '2024-01-31 23:59:59+00');

-- Function to check for meeting conflicts
CREATE OR REPLACE FUNCTION stg.check_meeting_conflicts(
  p_user_id UUID,
  p_start_time TIMESTAMPTZ,
  p_end_time TIMESTAMPTZ,
  p_exclude_event_id UUID DEFAULT NULL
)
  RETURNS TABLE
          (
            event_id   UUID,
            title      TEXT,
            start_time TIMESTAMPTZ,
            end_time   TIMESTAMPTZ
          )
AS
$$
BEGIN
  RETURN QUERY
    SELECT e.id, e.title, e.start_time, e.end_time
    FROM stg.events e
           LEFT JOIN stg.event_participants ep ON e.id = ep.event_id
    WHERE (e.organizer_id = p_user_id OR ep.user_id = p_user_id)
      AND e.status != 'CANCELLED'
      AND (
      (e.start_time >= p_start_time AND e.start_time < p_end_time)
        OR (e.end_time > p_start_time AND e.end_time <= p_end_time)
        OR (e.start_time <= p_start_time AND e.end_time >= p_end_time)
      )
      AND (p_exclude_event_id IS NULL OR e.id != p_exclude_event_id);
END;
$$ LANGUAGE plpgsql;
