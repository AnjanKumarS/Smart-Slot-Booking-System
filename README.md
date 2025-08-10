# 🎓 RVCE Smart Slot Booking System

A comprehensive, modern web application for booking and managing shared spaces at RVCE (R.V. College of Engineering). Built with Spring Boot backend, MySQL database, and responsive frontend with AI-powered assistance. **Exclusively for RVCE students and staff with @rvce.edu.in email addresses.**

## 🚀 Latest Features (v2.0)

### 🔒 **Security & Access Control**
- **Email Domain Restriction**: Only @rvce.edu.in email addresses allowed
- **Firebase Authentication**: Secure Google login integration
- **Role-Based Access**: Admin, Staff, and User roles with appropriate permissions
- **Session Management**: Secure session handling with automatic logout

### 🎯 **Enhanced Admin Panel**
- **Admin Dashboard**: Modern UI with real-time statistics and booking management
- **Venue Management**: Full CRUD operations for venues (Create, Read, Update, Delete)
- **Booking Approval System**: Approve, reject, or cancel user bookings
- **Analytics Dashboard**: Comprehensive charts and booking statistics
- **Database Management**: Tools to fix and maintain database integrity

### 🤖 **AI-Powered Chatbot Assistant**
- **Natural Language Processing**: Understand booking requests in plain English
- **Smart Suggestions**: Context-aware booking recommendations
- **RVCE-Specific Responses**: Tailored for college venue booking needs
- **Interactive Help**: Step-by-step booking guidance
- **Status Tracking**: Real-time booking status updates

### 📧 **Enhanced Email System**
- **Professional Templates**: Beautiful, informative email notifications
- **Booking Confirmations**: Detailed confirmation emails with venue information
- **Status Updates**: Real-time notifications for booking approval/rejection
- **OTP Delivery**: Secure OTP delivery for booking verification

### 🏢 **Venue Management System**
- **College Venues**: CS Auditorium, ISE Seminar Hall, Main Auditorium
- **Free Booking**: All venues free for RVCE students and staff
- **Capacity Management**: Venue capacity and availability tracking
- **Amenities Display**: Detailed venue information and facilities

## 🛠 Technology Stack

### Backend
- **Spring Boot 2.7.18**: Java web framework
- **Spring Data JPA**: Database abstraction layer
- **MySQL Database**: Production-ready database (Railway DB hosting)
- **Thymeleaf**: Template engine for server-side rendering
- **Firebase Admin SDK**: Authentication and user management
- **Spring Security**: Role-based access control
- **JavaMail**: Email notification system

### Frontend
- **HTML5**: Modern semantic markup
- **CSS3**: Advanced styling with animations and responsive design
- **JavaScript (ES6+)**: Modern JavaScript with async/await
- **Bootstrap 5**: Responsive UI framework
- **Chart.js**: Data visualization for analytics
- **Bootstrap Icons**: Modern icon library

### Additional Tools
- **Maven**: Build and dependency management
- **Google Fonts**: Typography (Inter font family)
- **Responsive Images**: Optimized image loading
- **Toast Notifications**: User feedback system

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
│   │   │           │   ├── DataInitializer.java                 # Database initialization
│   │   │           │   ├── FirebaseAuthenticationFilter.java   # Firebase auth filter
│   │   │           │   ├── FirebaseConfig.java                  # Firebase configuration
│   │   │           │   └── SecurityConfig.java                  # Security configuration
│   │   │           ├── controller/
│   │   │           │   ├── AdminController.java                 # Admin panel endpoints
│   │   │           │   ├── AnalyticsController.java             # Analytics endpoints
│   │   │           │   ├── AuthController.java                  # Authentication endpoints
│   │   │           │   ├── BookingApiController.java            # Booking API endpoints
│   │   │           │   ├── BookingController.java               # Booking management
│   │   │           │   ├── ChatbotController.java               # AI assistant endpoints
│   │   │           │   ├── CustomErrorController.java           # Error handling
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
│   │   │           │   ├── FirebaseUserDetailsService.java
│   │   │           │   └── VenueService.java
│   │   │           └── util/
│   │   │               ├── EmailUtil.java                       # Email functionality
│   │   │               ├── FirebaseTokenValidator.java         # Firebase token validation
│   │   │               └── OtpUtil.java                         # OTP generation/validation
│   │   ├── resources/
│   │   │   ├── static/
│   │   │   │   ├── css/
│   │   │   │   │   └── style.css
│   │   │   │   ├── js/
│   │   │   │   │   ├── app.js
│   │   │   │   │   ├── auth.js
│   │   │   │   │   ├── booking.js
│   │   │   │   │   ├── chatbot.js
│   │   │   │   │   ├── dashboard.js
│   │   │   │   │   ├── firebase-config.js
│   │   │   │   │   ├── staff-dashboard.js
│   │   │   │   │   └── user-bookings.js
│   │   │   │   └── favicon.ico
│   │   │   ├── templates/
│   │   │   │   ├── admin-dashboard.html                        # Admin panel
│   │   │   │   ├── admin-login.html                            # Admin login page
│   │   │   │   ├── admin-venues.html                           # Venue management
│   │   │   │   ├── analytics-dashboard.html                    # Analytics dashboard
│   │   │   │   ├── booking.html                                # Booking page
│   │   │   │   ├── chatbot.html                                # AI assistant
│   │   │   │   ├── dashboard.html                              # User dashboard
│   │   │   │   ├── error.html                                  # Error page
│   │   │   │   ├── fragments/
│   │   │   │   │   └── navbar.html                             # Navigation bar
│   │   │   │   ├── landing.html                                # Landing page
│   │   │   │   ├── login.html                                  # Login page
│   │   │   │   ├── register.html                               # Registration page
│   │   │   │   ├── staff-dashboard.html                        # Staff dashboard
│   │   │   │   ├── user-bookings.html                          # User bookings
│   │   │   │   ├── user-dashboard.html                         # User dashboard
│   │   │   │   └── verify-booking.html                         # OTP verification
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
- MySQL database (or Railway DB for hosting)
- Firebase project (for authentication)

### Installation

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd smart-slot-booking-system-springboot
   ```

2. **Configure database**:
   Update `src/main/resources/application.properties` with your database credentials:
   ```properties
   spring.datasource.url=jdbc:mysql://your-database-url
   spring.datasource.username=your-username
   spring.datasource.password=your-password
   ```

3. **Configure Firebase**:
   Add your Firebase configuration to `src/main/resources/static/js/firebase-config.js`

4. **Build the application**:
   ```bash
   mvn clean compile
   ```

5. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

6. **Access the application**:
   Open your web browser and go to `http://localhost:8080`

## 👤 User Access

### 🔒 **Email Domain Restriction**
- **Only @rvce.edu.in emails allowed**
- **Firebase Authentication**: Secure Google login
- **Automatic Role Assignment**: Based on email domain

### 👥 **User Roles**

| Role | Access Level | Features |
|------|-------------|----------|
| **Admin** | Full Access | Admin dashboard, venue management, booking approval, analytics |
| **Staff** | Staff Access | Booking management, venue access, user support |
| **User** | Basic Access | Book venues, view bookings, use chatbot |

### 🔑 **Admin Access**
- **Username**: `admin`
- **Password**: `admin123`
- **Access**: Full administrative privileges

## 🎯 Usage Guide

### For RVCE Students & Staff

#### 1. **Login Process**
- Visit the application homepage
- Click "Sign In" or "Register"
- **Use your @rvce.edu.in email address**
- Choose Google login for convenience
- **Non-RVCE emails will be blocked**

#### 2. **Browse Venues**
- View available venues: CS Auditorium, ISE Seminar Hall, Main Auditorium
- See capacity, location, and amenities
- All venues are **free for RVCE community**

#### 3. **Make a Booking**
- Select venue, date, and time
- Fill in event details and purpose
- Submit booking request
- **Receive OTP for verification**
- Complete OTP verification
- **Wait for admin approval**

#### 4. **Manage Bookings**
- View all your bookings in "My Bookings"
- Check booking status (Pending, Confirmed, Rejected)
- Cancel pending bookings if needed
- Receive email notifications for status changes

#### 5. **AI Assistant**
- Use the chatbot for booking assistance
- Ask questions in natural language
- Get venue recommendations
- Check availability quickly

### For Administrators

#### 1. **Admin Login**
- Use admin credentials: `admin` / `admin123`
- Access comprehensive admin dashboard

#### 2. **Booking Management**
- **Requests Pending**: Review and approve/reject booking requests
- **Manage Programs**: View all confirmed bookings
- **Cancel Bookings**: Cancel any booking if needed

#### 3. **Venue Management**
- **Add New Venues**: Create new venue entries
- **Edit Venues**: Update venue information
- **Delete Venues**: Remove venues from system
- **Fix Database**: Repair database issues

#### 4. **Analytics Dashboard**
- **User Booking Analytics**: Charts showing booking patterns
- **Venue Utilization**: Track venue usage statistics
- **Booking Trends**: Monitor booking trends over time

## 🤖 AI Assistant Features

### 🎯 **Smart Booking Assistance**
```
🎯 "Book CS Auditorium for tomorrow at 2 PM"
📅 "Check availability for ISE Seminar Hall"
📋 "Show me my current bookings"
🏢 "What venues are available today?"
❌ "Cancel my booking for today"
🔍 "Find a venue for 50 people"
💡 "How do I book a venue?"
⏰ "What are the booking hours?"
```

### 🎓 **RVCE-Specific Responses**
- **College Venue Information**: Detailed venue descriptions
- **Free Booking Reminders**: Emphasize free access for RVCE community
- **Admin Approval Process**: Explain the approval workflow
- **Email Notifications**: Inform about email confirmations

### 📊 **Enhanced Response Format**
```
🏢 RVCE Available Venues

📍 CS Auditorium
   📍 Location: CS Building, Ground Floor
   👥 Capacity: 300 people
   📝 Computer Science Department's main auditorium with advanced AV systems
   💰 Free for RVCE students & staff

💡 To book a venue, simply say: "Book [venue name] for [date] at [time]"
```

## 📧 Email System

### 📬 **Professional Email Templates**

#### **Booking Confirmation Email**
```
Dear [User Name],

🎉 Your booking has been CONFIRMED!

Booking Details:
• Booking ID: [ID]
• Venue: [Venue Name]
• Date: [Date]
• Time: [Start Time] - [End Time]
• Purpose: [Purpose]

Your venue booking is now confirmed and ready for use. Please arrive on time and enjoy your event!

If you have any questions, please contact the administration.

Best regards,
Smart Slot Booking System
```

#### **Booking Rejection Email**
```
Dear [User Name],

❌ Your booking has been REJECTED

Booking Details:
• Booking ID: [ID]
• Venue: [Venue Name]
• Date: [Date]
• Time: [Start Time] - [End Time]
• Purpose: [Purpose]

Unfortunately, your venue booking request could not be approved at this time. This may be due to:
• Venue unavailability
• Scheduling conflicts
• Administrative requirements

You can submit a new booking request for a different time slot or venue.

If you have any questions, please contact the administration.

Best regards,
Smart Slot Booking System
```

## 🏢 Venue Management

### 📍 **Available Venues**
- **CS Auditorium**: 300 people, CS Building, Ground Floor
- **ISE Seminar Hall**: 150 people, ISE Building, 1st Floor
- **Main Auditorium**: 800 people, Administrative Building, Ground Floor

### 💰 **Pricing**
- **All venues are FREE** for RVCE students and staff
- **No hourly rates** or charges
- **College community access** only

### 🔧 **Admin Venue Management**
- **Add New Venues**: Create venue entries with details
- **Edit Venues**: Update venue information
- **Delete Venues**: Remove venues (soft delete)
- **Database Fix**: Repair database issues automatically

## 📊 Analytics Dashboard

### 📈 **Chart Types**
- **Bar Charts**: User booking statistics
- **Line Charts**: Booking trends over time
- **Pie Charts**: Venue utilization distribution

### 📊 **Metrics Tracked**
- **Total Bookings**: Overall booking count
- **User Activity**: Individual user booking patterns
- **Venue Utilization**: Popular venues and usage
- **Booking Status**: Pending, confirmed, rejected statistics

## 🔧 Configuration

### Application Properties
```properties
# Application Configuration
spring.application.name=smart-slot-booking-system
server.port=8080

# Database Configuration (MySQL)
spring.datasource.url=jdbc:mysql://your-database-url
spring.datasource.username=your-username
spring.datasource.password=your-password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Firebase Configuration
firebase.demo-mode=false
firebase.service-account-key=your-firebase-key
firebase.database-url=your-firebase-url

# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Security Configuration
app.development.mode=false
```

## 🚀 Deployment

### Local Development
```bash
mvn spring-boot:run
```
Access at: `http://localhost:8080`

### Production Deployment
1. **Build JAR**:
   ```bash
   mvn clean package
   ```

2. **Run Application**:
   ```bash
   java -jar target/smart-slot-booking-system-0.0.1-SNAPSHOT.jar
   ```

3. **Environment Setup**:
   - Configure MySQL database
   - Set up Firebase project
   - Configure email settings
   - Set production mode

## 📱 Responsive Design

### 🎨 **Modern UI/UX**
- **Glassmorphism Effects**: Modern glass-like design elements
- **Gradient Backgrounds**: Beautiful color gradients
- **Smooth Animations**: CSS animations and transitions
- **Mobile-First**: Responsive design for all devices

### 📱 **Device Compatibility**
- **Desktop**: Full-featured experience
- **Tablet**: Touch-optimized interface
- **Mobile**: Mobile-first responsive design

## 🔒 Security Features

### 🛡️ **Access Control**
- **Email Domain Validation**: Only @rvce.edu.in emails
- **Role-Based Permissions**: Admin, Staff, User roles
- **Session Management**: Secure session handling
- **Firebase Authentication**: Google login integration

### 🔐 **Data Protection**
- **Input Validation**: Server-side validation
- **SQL Injection Prevention**: Parameterized queries
- **XSS Protection**: Output encoding
- **CSRF Protection**: Cross-site request forgery prevention

## 🧪 Testing

### ✅ **Tested Features**
- **Email Domain Restriction**: Non-RVCE emails blocked
- **Admin Panel**: All CRUD operations working
- **Booking System**: Complete booking workflow
- **Chatbot**: AI assistant functionality
- **Email Notifications**: All email templates
- **Responsive Design**: All device sizes

### 🐛 **Error Handling**
- **Comprehensive Validation**: Input and business logic validation
- **User-Friendly Errors**: Clear error messages
- **Graceful Degradation**: System continues working with errors
- **Logging**: Detailed error logging for debugging

## 📊 API Endpoints

### Authentication
- `POST /api/auth/session` - Create session after Firebase login
- `GET /api/auth/check` - Check authentication status
- `POST /api/auth/logout` - Logout user

### Admin Endpoints
- `GET /admin/panel` - Admin dashboard
- `GET /admin/venues` - Venue management page
- `GET /api/admin/bookings` - Get all bookings
- `POST /api/admin/bookings/{id}/approve` - Approve booking
- `POST /api/admin/bookings/{id}/reject` - Reject booking
- `POST /api/admin/bookings/{id}/cancel` - Cancel booking

### Venue Management
- `GET /api/admin/venues` - Get all venues
- `POST /api/admin/venues` - Create venue
- `PUT /api/admin/venues/{id}` - Update venue
- `DELETE /api/admin/venues/{id}` - Delete venue
- `POST /api/admin/venues/fix` - Fix database issues

### Public Endpoints
- `GET /api/venues` - Get active venues
- `GET /api/bookings/my-bookings` - Get user bookings
- `POST /api/bookings/verify-otp` - Verify booking OTP

### AI Assistant
- `POST /api/chatbot/chat` - Send message to chatbot
- `GET /api/chatbot/suggestions` - Get chat suggestions

## 🐛 Troubleshooting

### Common Issues

1. **Email Domain Restriction**:
   - Ensure using @rvce.edu.in email
   - Check Firebase authentication setup
   - Verify email validation in backend

2. **Database Issues**:
   - Use "Fix Database" button in admin panel
   - Check MySQL connection settings
   - Verify database permissions

3. **Admin Login Issues**:
   - Use correct credentials: `admin` / `admin123`
   - Clear browser cache and cookies
   - Check session management

4. **Email Notifications**:
   - Verify email configuration
   - Check SMTP settings
   - Ensure email credentials are correct

## 🤝 Contributing

### Development Guidelines
1. **Code Style**: Follow Java coding conventions
2. **Testing**: Add tests for new features
3. **Documentation**: Update documentation for changes
4. **Security**: Ensure security best practices

### Feature Requests
- Submit issues for bugs
- Request new features
- Suggest improvements
- Report security vulnerabilities

## 📄 License

This project is developed for RVCE (R.V. College of Engineering) and is provided as-is for educational and institutional use.

## 🆘 Support

### Getting Help
- **Documentation**: Check this README
- **Code Comments**: Review code for implementation details
- **Error Messages**: Check console for detailed error information
- **Admin Panel**: Use built-in tools for troubleshooting

### Contact Information
- **Institution**: R.V. College of Engineering
- **System**: Smart Slot Booking System
- **Access**: @rvce.edu.in email addresses only

## 🎉 Acknowledgments

- **RVCE**: For providing the institutional context
- **Spring Boot**: For the excellent Java framework
- **Firebase**: For authentication and user management
- **MySQL**: For reliable database management
- **Bootstrap**: For responsive UI framework
- **Chart.js**: For data visualization
- **OpenAI**: For AI assistant capabilities

---

**🎓 Built with ❤️ for RVCE students and staff**

**🏢 Smart Slot Booking System - Making venue booking simple and efficient**

