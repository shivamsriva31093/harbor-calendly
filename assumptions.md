# Application Assumptions & Constraints

## 1. User Management

### Authentication & Authorization
- Authentication is handled externally (not part of this implementation)
- User IDs are provided and validated by the external auth system
- All API endpoints are secured except those explicitly marked as public
- Email addresses are unique across the system

### Timezone Handling
- Users must provide their timezone on registration
- All times are stored in UTC in the database
- Times are converted to user's timezone only for display
- Daylight Saving Time transitions are handled by the timezone library

## 2. Availability Management

### Time Slots
- Minimum time slot duration is 15 minutes
- Time slots must be within the same day (no overnight slots)
- Users can have multiple availability windows per day
- Time slots cannot overlap for the same user
- Start time must be before end time

### Recurring Availability
- Availability patterns are weekly recurring
- Changes to availability don't affect existing scheduled events
- Exceptions (like holidays) override regular availability
- Maximum future date for setting availability is 6 months

## 3. Event Scheduling

### Event Creation
- Events must have at least one participant besides the organizer
- Event duration must be at least 15 minutes
- Events cannot be scheduled in the past
- Event title is mandatory, description is optional
- Maximum number of participants per event is 100

### Scheduling Conflicts
- Users cannot be double-booked
- Cancelled events don't block the time slot for new events
- Buffer time between events is user-configurable
- Events can only be modified by the organizer

### Participant Responses
- Default response status is 'PENDING'
- Valid responses are: PENDING, ACCEPTED, DECLINED, TENTATIVE
- Responses can be changed until event start time
- All responses are tracked in history

## 4. Notifications

### Email Notifications
- All users have a valid email address
- Email delivery is not guaranteed (best effort)
- Notifications are sent asynchronously
- Failed notifications are retried up to 3 times
- HTML email is supported by recipients

### Notification Types
- Event invitations
- Event updates
- Event cancellations
- Response updates
- Reminder notifications

## 5. Technical Constraints

### Database
- PostgreSQL 12 or higher is required
- GIST extension is available for range queries
- Maximum connections are limited by database plan
- All tables have appropriate indexes

### API Limits
- Rate limiting is handled by API gateway
- Maximum request body size is 1MB
- Request timeout is 30 seconds
- Batch operations limited to 100 items

### Performance
- Response time < 500ms for 95% of requests
- Maximum concurrent users: 1000
- Data retention period: 1 year
- Maximum events per user per day: 50

## 6. Business Rules

### Scheduling Windows
- Events can be scheduled up to 1 year in advance
- Minimum notice period for new events: 15 minutes
- Maximum event duration: 8 hours
- Working hours are defined by user availability

### Modifications
- Events can be modified until start time
- Cancelled events cannot be reactivated
- Event history is maintained indefinitely
- Participant list can be modified until event starts

### Calendar Integration
- External calendar sync is not implemented
- No support for recurring events
- No resource booking (rooms, equipment)
- No calendar sharing features

## 7. Error Handling

### Validation
- All input times must be valid ISO-8601 format
- Email addresses must be valid format
- Timezone must be valid IANA timezone
- UUIDs must be valid RFC 4122 format

### Conflict Resolution
- First-come-first-served for competing event creation
- Overlapping availability windows are merged
- Declined events don't block the time slot
- Buffer times are enforced between events

## 8. Data Management

### Storage
- Event attachments are not supported
- Maximum text field length: 1000 characters
- Metadata field limited to 10KB
- User preferences stored as JSON

### Cleanup
- Cancelled events are soft deleted
- Completed events are archived after 30 days
- User data is retained until explicit deletion
- Audit logs kept for 1 year

## 9. Security Assumptions

### Data Access
- Users can only view their own data
- Event details visible to all participants
- Participant lists are visible to all participants
- Historical data is read-only

### Privacy
- Email addresses are not shared between users
- User timezone information is private
- Response history is only visible to event organizer
- User's calendar is private by default

## 10. Infrastructure

### Deployment
- Single region deployment
- No specific high availability requirements
- Daily database backups
- Maximum 2GB memory per instance

### Monitoring
- Basic health checks are sufficient
- No 24/7 support requirement
