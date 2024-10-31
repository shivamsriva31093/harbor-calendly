# Calendly API Endpoints

## User Management
```
POST    /users                    # Create new user
GET     /users/{userId}          # Get user details
PUT     /users/{userId}          # Update user
DELETE  /users/{userId}          # Delete user
GET     /users/{userId}/timezone # Get user's timezone

```

## Availability Management
```
POST    /users/{userId}/availability          # Add new availability window
GET     /users/{userId}/availability          # Get all availability windows
PUT     /users/{userId}/availability/{id}     # Update specific availability window
DELETE  /users/{userId}/availability/{id}     # Remove specific availability window
POST    /users/{userId}/availability/exception # Set availability exception
GET     /schedule/overlap                     # Find overlapping availability slots
```

## Event Management
```
POST    /events                              # Schedule new event
GET     /events/{eventId}                    # Get event details
PUT     /events/{eventId}                    # Update event
DELETE  /events/{eventId}                    # Cancel event
GET     /users/{userId}/events               # Get user's events
PUT     /events/{eventId}/participants/{userId}/response  # Update participant response
```

## Request/Response Examples

### Find Overlapping Slots
```json
GET /schedule/overlap?userIds=["user1","user2"]&startDate=2024-01-01&endDate=2024-01-07

Response:
{
  "success": true,
  "data": {
    "slots": [
      {
        "start": "2024-01-01T09:00:00Z",
        "end": "2024-01-01T10:00:00Z"
      }
    ]
  }
}
```

### Create Event
```json
POST /events
{
  "title": "Team Meeting",
  "description": "Weekly sync",
  "startTime": "2024-01-01T09:00:00Z",
  "endTime": "2024-01-01T10:00:00Z",
  "organizerId": "user1",
  "participants": [
    {
      "userId": "user2",
      "notificationPreference": "EMAIL"
    }
  ]
}
```

### Update Participant Response
```json
PUT /events/{eventId}/participants/{userId}/response
{
  "response": "ACCEPTED"
}
```

# User Management APIs

### Create User
```http
POST /users
Content-Type: application/json

{
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "timezone": "America/New_York"
}
```

### Get User
```http
GET /users/550e8400-e29b-41d4-a716-446655440000
Content-Type: application/json
```

### Update User
```http
PUT /users/550e8400-e29b-41d4-a716-446655440000
Content-Type: application/json

{
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe Updated",
    "timezone": "Europe/London"
}
```

# Availability Management APIs

### Add Availability Window
```http
POST /users/550e8400-e29b-41d4-a716-446655440000/availability
Content-Type: application/json

{
    "dayOfWeek": 1,
    "startTime": "09:00:00",
    "endTime": "17:00:00"
}
```

### Get User's Availability
```http
GET /users/550e8400-e29b-41d4-a716-446655440000/availability
Content-Type: application/json
```

### Update Availability Window
```http
PUT /users/550e8400-e29b-41d4-a716-446655440000/availability/window-123
Content-Type: application/json

{
    "dayOfWeek": 1,
    "startTime": "10:00:00",
    "endTime": "18:00:00"
}
```

### Find Overlapping Slots
```http
GET /schedule/overlap?userIds=["550e8400-e29b-41d4-a716-446655440000","661f9511-f3a2-52e5-b827-557766551111"]&startDate=2024-01-01&endDate=2024-01-07
Content-Type: application/json
```

# Event Management APIs

### Create Event
```http
POST /events
Content-Type: application/json

{
    "title": "Team Weekly Sync",
    "description": "Weekly team sync meeting",
    "startTime": "2024-01-01T10:00:00Z",
    "endTime": "2024-01-01T11:00:00Z",
    "organizerId": "550e8400-e29b-41d4-a716-446655440000",
    "participants": [
        {
            "userId": "661f9511-f3a2-52e5-b827-557766551111",
            "response": "PENDING",
            "notificationPreference": "EMAIL"
        }
    ],
    "metadata": {
        "location": "Conference Room A",
        "conferenceLink": "https://meet.example.com/123",
        "recurringPattern": {
            "frequency": "WEEKLY",
            "interval": 1,
            "endDate": "2024-03-31"
        }
    }
}
```

### Get Event
```http
GET /events/772a8400-f1c3-41d4-b938-339755440000
Content-Type: application/json
```

### Update Event
```http
PUT /events/772a8400-f1c3-41d4-b938-339755440000
Content-Type: application/json

{
    "title": "Updated Team Weekly Sync",
    "description": "Weekly team sync meeting with updated agenda",
    "startTime": "2024-01-01T11:00:00Z",
    "endTime": "2024-01-01T12:00:00Z",
    "organizerId": "550e8400-e29b-41d4-a716-446655440000",
    "participants": [
        {
            "userId": "661f9511-f3a2-52e5-b827-557766551111",
            "response": "PENDING",
            "notificationPreference": "EMAIL"
        }
    ],
    "metadata": {
        "location": "Conference Room B",
        "conferenceLink": "https://meet.example.com/123"
    }
}
```

### Cancel Event
```http
DELETE /events/772a8400-f1c3-41d4-b938-339755440000
Content-Type: application/json
```

### Get User's Events
```http
GET /users/550e8400-e29b-41d4-a716-446655440000/events?startDate=2024-01-01&endDate=2024-01-31
Content-Type: application/json
```

### Update Participant Response
```http
PUT /events/772a8400-f1c3-41d4-b938-339755440000/participants/661f9511-f3a2-52e5-b827-557766551111/response
Content-Type: application/json

{
    "response": "ACCEPTED"
}
```

# cURL Examples

### Create User
```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "timezone": "America/New_York"
  }'
```

### Create Event
```bash
curl -X POST http://localhost:8080/events \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Team Weekly Sync",
    "description": "Weekly team sync meeting",
    "startTime": "2024-01-01T10:00:00Z",
    "endTime": "2024-01-01T11:00:00Z",
    "organizerId": "550e8400-e29b-41d4-a716-446655440000",
    "participants": [
        {
            "userId": "661f9511-f3a2-52e5-b827-557766551111",
            "response": "PENDING",
            "notificationPreference": "EMAIL"
        }
    ]
  }'
```

### Find Available Slots
```bash
curl -X GET "http://localhost:8080/schedule/overlap?userIds=[\"550e8400-e29b-41d4-a716-446655440000\",\"661f9511-f3a2-52e5-b827-557766551111\"]&startDate=2024-01-01&endDate=2024-01-07" \
  -H "Content-Type: application/json"
```

### Update Participant Response
```bash
curl -X PUT "http://localhost:8080/events/772a8400-f1c3-41d4-b938-339755440000/participants/661f9511-f3a2-52e5-b827-557766551111/response" \
  -H "Content-Type: application/json" \
  -d '{
    "response": "ACCEPTED"
  }'
```
