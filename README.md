# Harbor Calendly REST API

A robust calendar scheduling API built with Vert.x 4, implementing features similar to Calendly. The application enables efficient event scheduling by managing user availability, coordinating meetings, and handling participant responses.

## Core Features

### 1. User Management
- Create and manage user profiles
- Timezone support for users
- User availability management
- Email notification preferences

### 2. Availability Management
- Set recurring availability windows
  - Multiple time slots per day
  - Customizable for each day of the week
- Handle availability exceptions (holidays, time off)
- Buffer time settings between meetings
- Timezone-aware availability calculations

### 3. Event Scheduling
- Schedule events with multiple participants
- Automatic availability checking
- Prevent double-booking
- Support for event metadata (location, conference links)
- Handle participant responses (Accept/Decline/Tentative)
- Event modifications and cancellations
- Find overlapping available time slots for multiple participants

### 4. Notification System
- Email notifications for:
  - Event invitations
  - Event updates
  - Event cancellations
  - Participant response updates
  - Complete response summaries
- Customizable email templates using Handlebars
- Asynchronous notification processing

## Technical Implementation

### Architecture
- Vertx 4 with Kotlin Coroutines
- Event-driven architecture using Vert.x Event Bus
- PostgreSQL database with GIST index for efficient time range queries
- Service proxy pattern for better modularity

### Key Components
1. **Verticles**
  - MainVerticle: Application orchestration
  - RestApiVerticle: HTTP endpoint handling
  - DatabaseVerticle: Database operations
  - NotificationVerticle: Email notifications
  - SchedulerVerticle: Event scheduling logic

2. **Services**
  - UserService: User management
  - AvailabilityService: Availability windows
  - SchedulerService: Event scheduling
  - NotificationService: Email notifications

3. **Database Schema**
```sql
-- users
-- availability_settings
-- availability_exceptions
-- events
-- event_participants
-- buffer_settings

```
The files are stored in `db` folder

-`roles.sql` <br>
-`seed.sql` <br>
-`v0.sql`

## API Endpoints

### User Management
```http
POST    /users                    # Create user
GET     /users/{userId}          # Get user details
PUT     /users/{userId}          # Update user
DELETE  /users/{userId}          # Delete user
GET     /users/{userId}/timezone # Get user's timezone
```

### Availability Management
```http
POST    /users/{userId}/availability          # Add availability window
GET     /users/{userId}/availability          # Get availability windows
PUT     /users/{userId}/availability/{id}     # Update availability window
DELETE  /users/{userId}/availability/{id}     # Delete availability window
POST    /users/{userId}/availability/exception # Set availability exception
```

### Event Management
```http
POST    /events                              # Schedule event
GET     /events/{eventId}                    # Get event details
PUT     /events/{eventId}                    # Update event
DELETE  /events/{eventId}                    # Cancel event
GET     /users/{userId}/events               # Get user's events
PUT     /events/{eventId}/participants/{userId}/response  # Update participant response
GET     /schedule/overlap                    # Find available slots
```

## Setup and Installation

### Prerequisites
- JDK 11 or higher
- PostgreSQL 12 or higher
- Gradle 7.x

### Configuration
```json
{
  "database": {
    "host": "localhost",
    "port": 5432,
    "database": "calendar_db",
    "user": "postgres",
    "password": "postgres"
  },
  "mail": {
    "host": "smtp.gmail.com",
    "port": 587,
    "username": "your-email@gmail.com",
    "password": "your-app-password",
    "from": "Calendar App <your-email@gmail.com>"
  },
  "server": {
    "port": 8080
  }
}
```

### Build and Run
```bash
# Build the project
./gradlew build

# Run the application
./gradlew run
```

## Key Features Implemented

1. **Smart Scheduling**
  - Automatic time slot suggestions
  - Conflict prevention
  - Buffer time management
  - Timezone handling

2. **Efficient Data Storage**
  - GIST index for time range queries
  - Optimized availability lookups
  - Transaction support

3. **Robust Error Handling**
  - Comprehensive error responses
  - Input validation
  - Constraint checking
  - Transaction rollbacks

4. **Notification System**
  - Asynchronous email processing
  - HTML email templates
  - Event-based notifications
  - Failure handling and retries

## Testing

The application includes comprehensive test coverage for:
- Unit tests for services
- Integration tests for API endpoints
- Database operation tests
- Event bus communication tests

### Running Tests
```bash
# Run all tests
./gradlew test

# Run specific test category
./gradlew test --tests "io.harbor.calendly.*"
```

## Design Considerations

1. **Scalability**
  - Vertx's event-driven architecture
  - Efficient database indexing
  - Asynchronous operations
  - Connection pooling

2. **Maintainability**
  - Clear separation of concerns
  - Service proxy pattern
  - Typed responses
  - Comprehensive documentation

3. **Performance**
  - Optimized database queries
  - Caching possibilities
  - Batch operations
  - Efficient time slot calculations

## Potential Improvements

1. **Features**
  - Recurring meetings support
  - Calendar integration (Google, Outlook)
  - Meeting reminders
  - Resource booking

2. **Technical**
  - Caching implementation
  - Rate limiting
  - API versioning
  - Metrics collection

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.
