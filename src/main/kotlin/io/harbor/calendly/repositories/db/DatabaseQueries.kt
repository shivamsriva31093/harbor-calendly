package io.harbor.calendly.repositories.db

object DatabaseQueries {
  const val CREATE_SCHEMA = """
        CREATE TABLE IF NOT EXISTS users (
            id UUID PRIMARY KEY,
            email TEXT UNIQUE NOT NULL,
            timezone TEXT NOT NULL
        );

        CREATE TABLE IF NOT EXISTS availability_settings (
            user_id UUID REFERENCES users(id),
            day_of_week INTEGER,
            start_time TIME,
            end_time TIME,
            PRIMARY KEY (user_id, day_of_week, start_time)
        );

        CREATE TABLE IF NOT EXISTS events (
            id UUID PRIMARY KEY,
            title TEXT NOT NULL,
            description TEXT,
            start_time TIMESTAMPTZ NOT NULL,
            end_time TIMESTAMPTZ NOT NULL,
            created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
        );

        CREATE TABLE IF NOT EXISTS event_participants (
            event_id UUID REFERENCES events(id),
            user_id UUID REFERENCES users(id),
            status TEXT NOT NULL,
            PRIMARY KEY (event_id, user_id)
        );
    """
}
