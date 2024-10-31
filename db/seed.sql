-- Users data
INSERT INTO stg.users (id, email, first_name, last_name, timezone)
VALUES (gen_random_uuid(), 'john.doe@example.com', 'John', 'Doe', 'America/New_York'),
       (gen_random_uuid(), 'jane.smith@example.com', 'Jane', 'Smith', 'Europe/London'),
       (gen_random_uuid(), 'bob.wilson@example.com', 'Bob', 'Wilson', 'Asia/Tokyo');

-- Store user IDs for reference
DO
$$
  DECLARE
    user1_id UUID;
    user2_id UUID;
    user3_id UUID;
  BEGIN
    SELECT id INTO user1_id FROM stg.users WHERE email = 'john.doe@example.com';
    SELECT id INTO user2_id FROM stg.users WHERE email = 'jane.smith@example.com';
    SELECT id INTO user3_id FROM stg.users WHERE email = 'bob.wilson@example.com';

--         Availability settings
    INSERT INTO stg.availability_settings (id, user_id, day_of_week, start_time, end_time)
    VALUES (gen_random_uuid(), user1_id, 1, '09:00', '17:00'),
           (gen_random_uuid(), user1_id, 2, '09:00', '17:00'),
           (gen_random_uuid(), user1_id, 3, '09:00', '17:00'),
           (gen_random_uuid(), user2_id, 1, '10:00', '18:00'),
           (gen_random_uuid(), user2_id, 2, '10:00', '18:00'),
           (gen_random_uuid(), user3_id, 1, '08:00', '16:00'),
           (gen_random_uuid(), user3_id, 2, '08:00', '16:00');

    -- Events
    INSERT INTO stg.events (id, title, description, start_time, end_time, organizer_id, status)
    VALUES (gen_random_uuid(), 'Team Meeting', 'Weekly sync', NOW() + interval '1 day',
            NOW() + interval '1 day' + interval '1 hour', user1_id, 'SCHEDULED'),
           (gen_random_uuid(), 'Project Review', 'Q4 Review', NOW() + interval '2 days',
            NOW() + interval '2 days' + interval '2 hours', user2_id, 'SCHEDULED'),
           (gen_random_uuid(), 'Client Call', 'Product Demo', NOW() + interval '3 days',
            NOW() + interval '3 days' + interval '30 minutes', user3_id, 'SCHEDULED');

    -- Event participants
    WITH events_data AS (SELECT id
                         FROM stg.events
                         WHERE title = 'Team Meeting')
    INSERT
    INTO stg.event_participants (event_id, user_id, response)
    SELECT id, user2_id, 'ACCEPTED'
    FROM events_data
    UNION ALL
    SELECT id, user3_id, 'PENDING'
    FROM events_data;

    -- Buffer settings
    INSERT INTO stg.buffer_settings (user_id, before_meeting_minutes, after_meeting_minutes)
    VALUES (user1_id, 15, 15),
           (user2_id, 10, 10),
           (user3_id, 5, 5);

    -- Availability exceptions
    INSERT INTO stg.availability_exceptions (id, user_id, date, is_available, reason)
    VALUES (gen_random_uuid(), user1_id, CURRENT_DATE + 7, false, 'Vacation'),
           (gen_random_uuid(), user2_id, CURRENT_DATE + 14, false, 'Public Holiday');

  END
$$;
