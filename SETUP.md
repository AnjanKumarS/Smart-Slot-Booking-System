# Smart Slot Booking System - Setup Guide

## Prerequisites
- Java 11 or higher
- Maven
- Railway account with MySQL database
- Firebase project with Authentication enabled

## Environment Variables Setup

Create a `.env` file in your project root or set these environment variables:

```bash
# MySQL Database (Railway)
MYSQL_PASSWORD=your_railway_mysql_password

# Firebase Configuration
FIREBASE_PROJECT_ID=your-firebase-project-id
FIREBASE_DATABASE_URL=https://your-firebase-project-id.firebaseio.com

# Email Configuration (Gmail)
GMAIL_USERNAME=your-email@gmail.com
GMAIL_PASSWORD=your-gmail-app-password

# OpenAI (Optional)
OPENAI_API_KEY=your-openai-api-key
```

## Database Configuration

1. **Railway MySQL Setup:**
   - Create a MySQL database on Railway
   - Use the connection URL: `mysql://root:<password>@nozomi.proxy.rlwy.net:40793/railway`
   - Update `application.properties` with your actual password

2. **Database Schema:**
   - The application will automatically create tables using JPA/Hibernate
   - Set `spring.jpa.hibernate.ddl-auto=update` for production

## Firebase Configuration

1. **Firebase Project Setup:**
   - Create a Firebase project at https://console.firebase.google.com/
   - Enable Authentication (Email/Password, Google, etc.)
   - Go to Project Settings > Service Accounts
   - Click "Generate new private key" to download service account JSON

2. **Service Account Key:**
   - Rename the downloaded JSON file to `firebase-service-account.json`
   - Place it in `src/main/resources/` directory
   - The file should contain your project's service account credentials

3. **Firebase Database URL (Optional):**
   - If using Firebase Realtime Database, set the URL:
   ```bash
   FIREBASE_DATABASE_URL=https://your-project-id.firebaseio.com
   ```
   - Or hardcode it in `application.properties`

4. **Verification:**
   - When you run the application, you should see:
   ```
   ✅ Firebase successfully initialized
   ```

## Running the Application

### Development
```bash
./mvnw spring-boot:run
```

### Production
```bash
./mvnw clean package
java -jar target/smart-slot-booking-system-0.0.1-SNAPSHOT.jar
```

## Testing

The application uses H2 database for testing:
```bash
./mvnw test
```

## API Endpoints

- **Authentication:** `/api/auth/**`
- **Bookings:** `/api/bookings/**`
- **Venues:** `/api/venues/**`
- **Analytics:** `/api/analytics/**`

## Security

- Firebase handles user authentication
- Spring Security is configured to work with Firebase tokens
- CORS is enabled for frontend integration
- CSRF is disabled for API endpoints

## Troubleshooting

1. **Database Connection Issues:**
   - Verify Railway MySQL credentials
   - Check network connectivity
   - Ensure MySQL server is running

2. **Firebase Issues:**
   - Verify service account key is correct
   - Check Firebase project configuration
   - Ensure Authentication is enabled

3. **Build Issues:**
   - Run `./mvnw clean install` to rebuild
   - Check Java version compatibility
   - Verify all dependencies are resolved 