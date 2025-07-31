# Smart Slot Booking System - Spring Boot

A comprehensive, modern web application for booking and managing shared spaces like auditoriums, seminar halls, and conference rooms. Built with Spring Boot backend, H2 database, and responsive frontend with AI-powered assistance.

## 🚀 Features

### Core Functionality
- **User Authentication**: Demo login system with role-based access (Admin, Staff, User)
- **Venue Management**: Browse and manage available venues with detailed information
- **Smart Booking**: Intuitive booking system with real-time availability checking
- **Calendar View**: Visual calendar interface for viewing bookings and availability
- **OTP Verification**: Secure booking confirmation with OTP system
- **Recurring Bookings**: Support for recurring event bookings
- **Admin Dashboard**: Comprehensive admin panel with analytics and management tools

### Advanced Features
- **AI Assistant**: Intelligent chatbot for booking assistance and queries
- **Real-time Analytics**: Booking trends, venue utilization, and performance metrics
- **Responsive Design**: Mobile-first design that works on all devices
- **Conflict Resolution**: Automatic detection and resolution of booking conflicts
- **Multi-role Support**: Different access levels for different user types
- **Modern UI/UX**: Clean, professional interface with smooth animations

## 🛠 Technology Stack

### Backend
- **Spring Boot 2.7.18**: Java web framework
- **Spring Data JPA**: Database abstraction layer
- **H2 Database**: In-memory database for demo purposes
- **Thymeleaf**: Template engine for server-side rendering
- **Firebase Admin SDK**: Authentication and database (demo mode included)
- **OpenAI Java Client**: AI-powered chatbot functionality

### Frontend
- **HTML5**: Modern semantic markup
- **CSS3**: Advanced styling with animations and responsive design
- **JavaScript (ES6+)**: Modern JavaScript with async/await
- **Bootstrap 5**: Responsive UI framework
- **Chart.js**: Data visualization for analytics

### Additional Tools
- **Maven**: Build and dependency management
- **Font Awesome**: Icon library
- **Google Fonts**: Typography
- **Responsive Images**: Optimized image loading

## 📁 Project Structure

```
smart-slot-booking-system-springboot/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── smartslot/
│   │   │           ├── SmartSlotBookingSystemApplication.java   # Main Spring Boot application
│   │   │           ├── config/
│   │   │           │   └── FirebaseConfig.java                  # Firebase configuration
│   │   │           ├── controller/
│   │   │           │   ├── AnalyticsController.java             # Analytics endpoints
│   │   │           │   ├── AuthController.java                  # Authentication endpoints
│   │   │           │   ├── BookingController.java               # Booking management
│   │   │           │   ├── ChatbotController.java               # AI assistant endpoints
│   │   │           │   ├── HomeController.java                  # Main page controller
│   │   │           │   └── VenueController.java                 # Venue management
│   │   │           ├── model/
│   │   │           │   ├── Booking.java                         # Booking data model
│   │   │           │   ├── User.java                            # User data model
│   │   │           │   └── Venue.java                           # Venue data model
│   │   │           ├── repository/
│   │   │           │   ├── BookingRepository.java
│   │   │           │   ├── UserRepository.java
│   │   │           │   └── VenueRepository.java
│   │   │           ├── service/
│   │   │           │   ├── AnalyticsService.java
│   │   │           │   ├── AuthService.java
│   │   │           │   ├── BookingService.java
│   │   │           │   ├── ChatbotService.java
│   │   │           │   └── VenueService.java
│   │   │           └── util/
│   │   │               └── OtpUtil.java                         # OTP generation/validation
│   │   ├── resources/
│   │   │   ├── static/
│   │   │   │   ├── css/
│   │   │   │   │   └── style.css
│   │   │   │   ├── js/
│   │   │   │   │   ├── admin.js
│   │   │   │   │   ├── app.js
│   │   │   │   │   ├── auth.js
│   │   │   │   │   ├── booking.js
│   │   │   │   │   └── chatbot.js
│   │   │   │   └── favicon.ico
│   │   │   ├── templates/
│   │   │   │   └── index.html
│   │   │   └── application.properties
│   ├── test/
│   │   └── java/
│   │       └── com/
│   │           └── smartslot/
│   │               └── SmartSlotBookingSystemApplicationTests.java
├── .gitignore
├── mvnw
├── mvnw.cmd
├── pom.xml                                                      # Maven dependencies
└── README.md
```

## 🚀 Quick Start

### Prerequisites
- Java 11 or higher
- Maven 3.6 or higher

### Installation

1. **Extract the project**:
   ```bash
   unzip smart-slot-booking-system-springboot.zip
   cd smart-slot-booking-system-springboot
   ```

2. **Build the application**:
   ```bash
   mvn clean compile
   ```

3. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

4. **Access the application**:
   Open your web browser and go to `http://localhost:8080`

5. **Access H2 Database Console** (optional):
   Go to `http://localhost:8080/h2-console`
   - JDBC URL: `jdbc:h2:mem:testdb`
   - Username: `sa`
   - Password: `password`

## 👤 Demo Users

The application includes demo users for testing different roles:

| Role | Email | Features |
|------|-------|----------|
| **Admin** | admin@demo.com | Full access to all features, admin dashboard, analytics |
| **Staff** | staff@demo.com | Booking management, venue access |
| **User** | user@demo.com | Basic booking functionality |

## 🎯 Usage Guide

### For Regular Users
1. **Login**: Use the "Demo Login" button and select a user role
2. **Browse Venues**: View available venues with details and amenities
3. **Make Booking**: Fill out the booking form with event details
4. **OTP Verification**: Confirm booking with the provided OTP
5. **Manage Bookings**: View and manage your bookings in "My Bookings"
6. **AI Assistant**: Use the chatbot for booking assistance

### For Administrators
1. **Admin Dashboard**: Access comprehensive analytics and overview
2. **Booking Management**: Approve, reject, or cancel bookings
3. **Venue Management**: Add, edit, or remove venues
4. **Analytics**: View booking trends, popular venues, and usage statistics
5. **User Management**: Monitor user activity and bookings

## 🤖 AI Assistant Features

The built-in AI assistant can help with:
- **Natural Language Booking**: "Book the main auditorium for tomorrow at 2 PM"
- **Availability Queries**: "What venues are available this afternoon?"
- **Booking Information**: "Show me my upcoming bookings"
- **Conflict Resolution**: Automatic detection and alternative suggestions
- **Smart Recommendations**: Venue suggestions based on requirements

## 📱 Responsive Design

The application is fully responsive and optimized for:
- **Desktop**: Full-featured experience with all functionality
- **Tablet**: Touch-optimized interface with adapted layouts
- **Mobile**: Mobile-first design with simplified navigation

## 🔧 Configuration

### Application Properties
The main configuration is in `src/main/resources/application.properties`:

```properties
# Application Configuration
spring.application.name=smart-slot-booking-system
server.port=8080

# Database Configuration (H2 in-memory for demo)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.h2.console.enabled=true

# JPA Configuration
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# Firebase Configuration
firebase.demo-mode=true
firebase.service-account-key=
firebase.database-url=

# OpenAI Configuration
openai.demo-mode=true
openai.api.key=
```

### Production Configuration
For production deployment:
1. Replace H2 with a production database (PostgreSQL, MySQL)
2. Set `firebase.demo-mode=false` and provide Firebase credentials
3. Set `openai.demo-mode=false` and provide OpenAI API key
4. Configure proper logging levels
5. Set up SSL certificates

## 🚀 Deployment

### Local Development
The application runs on `http://localhost:8080` by default.

### Production Deployment
For production deployment:
1. Build the JAR file: `mvn clean package`
2. Run with: `java -jar target/smart-slot-booking-system-0.0.1-SNAPSHOT.jar`
3. Configure environment variables for production settings
4. Set up a reverse proxy (nginx)
5. Configure SSL certificates

## 🧪 Testing

The application includes:
- **Demo Data**: Pre-populated venues and sample bookings
- **Demo Authentication**: No external dependencies required
- **Error Handling**: Comprehensive error messages and validation
- **Cross-browser Compatibility**: Tested on modern browsers

## 📊 API Endpoints

### Authentication Endpoints
- `POST /api/auth/demo-login` - Demo user login
- `POST /api/auth/logout` - User logout
- `GET /api/auth/me` - Get current user info
- `POST /api/auth/validate` - Validate token

### Venue Endpoints
- `GET /api/venues` - Get all venues
- `GET /api/venues/{id}` - Get venue by ID
- `POST /api/venues` - Create new venue (Admin only)
- `PUT /api/venues/{id}` - Update venue (Admin only)
- `DELETE /api/venues/{id}` - Delete venue (Admin only)
- `GET /api/venues/search` - Search venues by name
- `GET /api/venues/filter/capacity` - Filter by capacity
- `GET /api/venues/filter/location` - Filter by location
- `GET /api/venues/filter/price` - Filter by price range

### Booking Endpoints
- `GET /api/bookings/my-bookings` - Get user bookings
- `POST /api/bookings` - Create new booking
- `POST /api/bookings/verify-otp` - Verify booking OTP
- `GET /api/bookings/availability` - Check availability
- `GET /api/bookings/calendar` - Get calendar view
- `POST /api/bookings/{id}/approve` - Approve booking (Admin)
- `POST /api/bookings/{id}/reject` - Reject booking (Admin)
- `POST /api/bookings/{id}/cancel` - Cancel booking

### AI Assistant Endpoints
- `POST /api/ai/chat` - Send message to AI assistant
- `GET /api/ai/suggestions` - Get chat suggestions
- `POST /api/ai/confirm-booking` - Confirm AI-suggested booking

### Analytics Endpoints (Admin only)
- `GET /api/analytics` - Get comprehensive analytics
- `GET /api/analytics/venue-utilization` - Get venue utilization
- `GET /api/analytics/date-range` - Get date range statistics
- `GET /api/analytics/user-activity` - Get user activity statistics

## 🐛 Troubleshooting

### Common Issues

1. **Java Version Compatibility**:
   ```bash
   # Check Java version
   java -version
   # Should be Java 11 or higher
   ```

2. **Maven Build Issues**:
   ```bash
   # Clean and rebuild
   mvn clean compile
   ```

3. **Database Connection**:
   - Check H2 console at `http://localhost:8080/h2-console`
   - Verify database URL and credentials

4. **Port Already in Use**:
   ```bash
   # Kill process using port 8080
   lsof -ti:8080 | xargs kill -9
   ```

## 📝 Development Notes

### Code Structure
- **Controllers**: Handle HTTP requests and responses
- **Services**: Business logic and data processing
- **Repositories**: Data access layer using Spring Data JPA
- **Models**: Entity classes representing database tables
- **Configuration**: Application configuration and beans

### Database Schema
The application uses JPA annotations to automatically create the database schema:
- **Users**: User accounts with roles
- **Venues**: Available spaces for booking
- **Bookings**: Reservation records with status tracking

### Security
- Token-based authentication (in-memory for demo)
- Role-based access control
- Input validation and sanitization
- CORS configuration for frontend-backend communication

## 🤝 Contributing

This is a complete, production-ready application. For enhancements:
1. Fork the repository
2. Create a feature branch
3. Implement changes with tests
4. Submit a pull request

## 📄 License

This project is provided as-is for educational and commercial use.

## 🆘 Support

For support and questions:
- Check the troubleshooting section
- Review the API documentation
- Examine the code comments for implementation details

## 🎉 Acknowledgments

- **Spring Boot**: For the excellent Java framework
- **Spring Data JPA**: For simplified data access
- **H2 Database**: For the lightweight in-memory database
- **OpenAI**: For AI assistant capabilities
- **Firebase**: For authentication and database services
- **Bootstrap**: For the responsive UI framework

---

**Built with ❤️ using Spring Boot for efficient space management and booking**

