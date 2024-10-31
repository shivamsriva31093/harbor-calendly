# Calendar Availability API Specification

## Core Resources

### Users
- `GET /users/{userId}/availability` - Get user's availability settings
- `PUT /users/{userId}/availability` - Update user's availability settings
- `GET /users/{userId}/schedule` - Get user's scheduled events

### Availability Windows
```json
{
  "recurring": {
    "monday": [{"start": "09:00", "end": "17:00"}],
    "tuesday": [{"start": "09:00", "end": "17:00"}],
    // ... other days
  },
  "exceptions": [
    {
      "date": "2024-11-01",
      "available": false,
      "reason": "holiday"
    }
  ]
}
```

### Schedule Overlap
- `GET /schedule/overlap`
  - Query params:
    - `userIds`: array of user IDs to check
    - `startDate`: start of period to check
    - `endDate`: end of period to check
  - Returns available time slots where all users are free

## Database Schema

```sql
CREATE TABLE users (
  id UUID PRIMARY KEY,
  email TEXT UNIQUE NOT NULL,
  timezone TEXT NOT NULL
);

CREATE TABLE availability_settings (
  user_id UUID REFERENCES users(id),
  day_of_week INTEGER,
  start_time TIME,
  end_time TIME,
  PRIMARY KEY (user_id, day_of_week, start_time)
);

CREATE TABLE availability_exceptions (
  id UUID PRIMARY KEY,
  user_id UUID REFERENCES users(id),
  date DATE,
  is_available BOOLEAN,
  reason TEXT
);

CREATE TABLE events (
  id UUID PRIMARY KEY,
  start_time TIMESTAMP WITH TIME ZONE,
  end_time TIMESTAMP WITH TIME ZONE,
  title TEXT,
  description TEXT
);

CREATE TABLE event_participants (
  event_id UUID REFERENCES events(id),
  user_id UUID REFERENCES users(id),
  PRIMARY KEY (event_id, user_id)
);
```

# Enhanced Calendar API Features

## 1. Smart Scheduling Capabilities

### Buffer Times
```json
{
  "bufferSettings": {
    "beforeMeeting": 15,  // minutes
    "afterMeeting": 10,   // minutes
    "applyTo": ["external", "all"]
  }
}
```

### Intelligent Time Suggestions
- Algorithm considers:
  - Historical meeting patterns
  - Productivity hours
  - Time zone differences
  - Travel time between locations

### Meeting Duration Presets
```json
{
  "meetingPresets": [
    {
      "name": "Quick Sync",
      "duration": 15,
      "color": "#FF4444",
      "defaultBuffer": 5
    },
    {
      "name": "Team Meeting",
      "duration": 45,
      "color": "#4444FF",
      "defaultBuffer": 10
    }
  ]
}
```

## 2. Advanced Availability Management

### Working Hours Optimization
- Peak productivity tracking
- Automatic meeting-free blocks
- Focus time recommendations

### Location-Based Availability
```json
{
  "locations": [
    {
      "name": "Main Office",
      "address": "123 Business St",
      "availabilityRules": {
        "monday": [{"start": "09:00", "end": "17:00"}]
      }
    },
    {
      "name": "Home Office",
      "availabilityRules": {
        "monday": [{"start": "07:00", "end": "20:00"}]
      }
    }
  ]
}
```

### Team Calendar Management
- Group availability views
- Department-wide blocked times
- Vacation calendar integration

## 3. Analytics and Insights

### Meeting Analytics
```json
{
  "meetingMetrics": {
    "weeklyStats": {
      "totalMeetingHours": 12,
      "averageDuration": 45,
      "mostFrequentAttendees": ["userId1", "userId2"],
      "peakMeetingTimes": ["10:00", "14:00"]
    }
  }
}
```

### Calendar Health Scores
- Meeting density analysis
- Focus time ratio
- Break time adequacy
- Work-life balance indicators

## 4. Integration Capabilities

### External Calendar Sync
- Two-way sync with popular calendars
- Conflict resolution
- Real-time updates

### Communication Platform Integration
```json
{
  "integrations": {
    "slack": {
      "status_sync": true,
      "meeting_reminders": true,
      "automatic_dnd": true
    },
    "teams": {
      "presence_sync": true,
      "meeting_joins": true
    }
  }
}
```

## 5. Advanced Scheduling Logic

### Priority-Based Scheduling
```json
{
  "meetingPriorities": {
    "client": 1,
    "team": 2,
    "internal": 3,
    "optional": 4
  },
  "reschedulingRules": {
    "allowBump": true,
    "minPriorityDiff": 2
  }
}
```

### Smart Rescheduling
- Conflict resolution
- Mass calendar event updates
- Attendee availability optimization

## 6. Time Zone Intelligence

### Multi-timezone Support
```json
{
  "timezonePreferences": {
    "display": ["UTC", "Local"],
    "workingHours": {
      "start": "09:00",
      "end": "17:00",
      "respectsDST": true
    }
  }
}
```

### Timezone-Aware Scheduling
- Working hours across zones
- Preferred meeting times
- Local holiday awareness
