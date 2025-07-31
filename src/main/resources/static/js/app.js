// Smart Slot Booking System - Main Application JavaScript

// Global variables
let currentUser = null;
let currentSection = 'home';
let venues = [];
let userBookings = [];

// API Base URL
const API_BASE = '/api';

// Initialize application
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
});

function initializeApp() {
    // Check for existing authentication
    checkAuthStatus();
    
    // Load initial data
    loadVenues();
    
    // Set up event listeners
    setupEventListeners();
    
    // Initialize date inputs with today's date
    const today = new Date().toISOString().split('T')[0];
    const bookingDateInput = document.getElementById('booking-date');
    if (bookingDateInput) {
        bookingDateInput.min = today;
        bookingDateInput.value = today;
    }
    
    // Initialize calendar dropdowns
    initializeCalendarDropdowns();
    
    // Show home section by default
    showSection('home');
}

function setupEventListeners() {
    // Navigation links
    document.querySelectorAll('.nav-link').forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const href = this.getAttribute('href');
            if (href && href.startsWith('#')) {
                const section = href.substring(1);
                navigateToSection(section);
            }
        });
    });
    
    // Booking form
    const bookingForm = document.getElementById('booking-form');
    if (bookingForm) {
        bookingForm.addEventListener('submit', handleBookingSubmit);
    }
    
    // Recurring booking checkbox
    const recurringCheckbox = document.getElementById('recurring-booking');
    if (recurringCheckbox) {
        recurringCheckbox.addEventListener('change', function() {
            const recurringOptions = document.getElementById('recurring-options');
            if (recurringOptions) {
                recurringOptions.style.display = this.checked ? 'block' : 'none';
            }
        });
    }
    
    // Calendar venue selection
    const calendarVenueSelect = document.getElementById('calendar-venue-select');
    if (calendarVenueSelect) {
        calendarVenueSelect.addEventListener('change', loadCalendarView);
    }
    
    // Calendar month/year selection
    const calendarMonth = document.getElementById('calendar-month');
    const calendarYear = document.getElementById('calendar-year');
    if (calendarMonth) calendarMonth.addEventListener('change', loadCalendarView);
    if (calendarYear) calendarYear.addEventListener('change', loadCalendarView);
}

// Navigation functions
function navigateToSection(section) {
    // Check authentication for protected sections
    const protectedSections = ['booking', 'my-bookings', 'admin', 'chatbot'];
    if (protectedSections.includes(section) && !currentUser) {
        showLoginModal();
        return;
    }
    
    // Check admin access
    if (section === 'admin' && (!currentUser || currentUser.role !== 'ADMIN')) {
        showAlert('Access denied. Admin privileges required.', 'danger');
        return;
    }
    
    showSection(section);
    currentSection = section;
    
    // Load section-specific data
    switch (section) {
        case 'venues':
            loadVenues();
            break;
        case 'booking':
            loadVenuesForBooking();
            break;
        case 'my-bookings':
            loadUserBookings();
            break;
        case 'admin':
            loadAdminDashboard();
            break;
        case 'chatbot':
            loadChatSuggestions();
            break;
        case 'calendar':
            loadVenuesForCalendar();
            break;
    }
}

function showSection(sectionId) {
    // Hide all sections
    document.querySelectorAll('main section').forEach(section => {
        section.style.display = 'none';
    });
    
    // Show target section
    const targetSection = document.getElementById(sectionId);
    if (targetSection) {
        targetSection.style.display = 'block';
        targetSection.classList.add('fade-in-up');
    }
    
    // Update navigation
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
        if (link.getAttribute('href') === `#${sectionId}`) {
            link.classList.add('active');
        }
    });
}

// Authentication functions
function checkAuthStatus() {
    const token = localStorage.getItem('authToken');
    const userData = localStorage.getItem('userData');
    
    if (token && userData) {
        try {
            currentUser = JSON.parse(userData);
            updateUIForAuthenticatedUser();
        } catch (error) {
            console.error('Error parsing user data:', error);
            logout();
        }
    }
}

function updateUIForAuthenticatedUser() {
    if (!currentUser) return;
    
    // Update navigation
    document.getElementById('auth-section').style.display = 'none';
    document.getElementById('user-section').style.display = 'block';
    document.getElementById('user-name').textContent = currentUser.name || currentUser.email;
    
    // Show role-specific navigation
    if (currentUser.role === 'ADMIN' || currentUser.role === 'STAFF') {
        document.getElementById('nav-admin').style.display = 'block';
    }
    
    document.getElementById('nav-chatbot').style.display = 'block';
}

function logout() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userData');
    currentUser = null;
    
    // Update UI
    document.getElementById('auth-section').style.display = 'block';
    document.getElementById('user-section').style.display = 'none';
    document.getElementById('nav-admin').style.display = 'none';
    document.getElementById('nav-chatbot').style.display = 'none';
    
    // Redirect to home
    navigateToSection('home');
    
    showAlert('Logged out successfully', 'success');
}

// Venue functions
async function loadVenues() {
    try {
        showLoading(true);
        const response = await fetch(`${API_BASE}/venues`);
        const data = await response.json();
        
        if (data.success) {
            venues = data.venues;
            displayVenues(venues);
        } else {
            showAlert('Failed to load venues', 'danger');
        }
    } catch (error) {
        console.error('Error loading venues:', error);
        showAlert('Error loading venues', 'danger');
    } finally {
        showLoading(false);
    }
}

function displayVenues(venueList) {
    const venuesGrid = document.getElementById('venues-grid');
    if (!venuesGrid) return;
    
    venuesGrid.innerHTML = '';
    
    venueList.forEach(venue => {
        const amenities = venue.amenities ? JSON.parse(venue.amenities) : [];
        const venueCard = document.createElement('div');
        venueCard.className = 'col-lg-4 col-md-6 mb-4';
        
        venueCard.innerHTML = `
            <div class="card venue-card h-100">
                <img src="https://images.unsplash.com/photo-1497366216548-37526070297c?w=400&h=200&fit=crop" 
                     class="card-img-top" alt="${venue.name}">
                <div class="card-body">
                    <h5 class="card-title">${venue.name}</h5>
                    <p class="card-text">${venue.description}</p>
                    <div class="row mb-2">
                        <div class="col-6">
                            <small class="text-muted">
                                <i class="bi bi-people me-1"></i>
                                Capacity: ${venue.capacity}
                            </small>
                        </div>
                        <div class="col-6">
                            <small class="text-muted">
                                <i class="bi bi-geo-alt me-1"></i>
                                ${venue.location}
                            </small>
                        </div>
                    </div>
                    <div class="venue-amenities">
                        ${amenities.map(amenity => 
                            `<span class="amenity-badge">${amenity}</span>`
                        ).join('')}
                    </div>
                    <div class="mt-3">
                        <small class="text-success fw-bold">
                            $${venue.hourly_rate}/hour
                        </small>
                    </div>
                </div>
                <div class="card-footer bg-transparent">
                    <button class="btn btn-primary w-100" onclick="bookVenue('${venue.id}')">
                        <i class="bi bi-calendar-plus me-2"></i>
                        Book Now
                    </button>
                </div>
            </div>
        `;
        
        venuesGrid.appendChild(venueCard);
    });
}

function bookVenue(venueId) {
    // Pre-select venue in booking form
    navigateToSection('booking');
    setTimeout(() => {
        const venueSelect = document.getElementById('venue-select');
        if (venueSelect) {
            venueSelect.value = venueId;
        }
    }, 100);
}

async function loadVenuesForBooking() {
    const venueSelect = document.getElementById('venue-select');
    if (!venueSelect) return;
    
    venueSelect.innerHTML = '<option value="">Select a venue...</option>';
    
    venues.forEach(venue => {
        const option = document.createElement('option');
        option.value = venue.id;
        option.textContent = `${venue.name} (Capacity: ${venue.capacity})`;
        venueSelect.appendChild(option);
    });
}

async function loadVenuesForCalendar() {
    const venueSelect = document.getElementById('calendar-venue-select');
    if (!venueSelect) return;
    
    venueSelect.innerHTML = '<option value="">Select a venue to view calendar...</option>';
    
    venues.forEach(venue => {
        const option = document.createElement('option');
        option.value = venue.id;
        option.textContent = venue.name;
        venueSelect.appendChild(option);
    });
}

// Booking functions
async function handleBookingSubmit(e) {
    e.preventDefault();
    
    if (!currentUser) {
        showLoginModal();
        return;
    }
    
    const formData = new FormData(e.target);
    const bookingData = {
        venue_id: document.getElementById('venue-select').value,
        title: document.getElementById('booking-title').value,
        booking_date: document.getElementById('booking-date').value,
        start_time: document.getElementById('start-time').value,
        end_time: document.getElementById('end-time').value,
        purpose: document.getElementById('booking-purpose').value,
        contact_number: document.getElementById('contact-number').value,
        expected_attendees: parseInt(document.getElementById('expected-attendees').value) || null,
        special_requirements: document.getElementById('special-requirements').value,
        user_name: currentUser.name || currentUser.email
    };
    
    // Add recurring info if enabled
    const recurringCheckbox = document.getElementById('recurring-booking');
    if (recurringCheckbox && recurringCheckbox.checked) {
        bookingData.recurring_info = {
            type: document.getElementById('recurring-type').value,
            count: parseInt(document.getElementById('recurring-count').value)
        };
    }
    
    try {
        showLoading(true);
        const response = await fetch(`${API_BASE}/bookings`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            },
            body: JSON.stringify(bookingData)
        });
        
        const data = await response.json();
        
        if (data.success) {
            // Show OTP modal
            showOTPModal(data.booking_id, data.otp);
        } else {
            showAlert(data.error || 'Failed to create booking', 'danger');
        }
    } catch (error) {
        console.error('Error creating booking:', error);
        showAlert('Error creating booking', 'danger');
    } finally {
        showLoading(false);
    }
}

async function checkAvailability() {
    const venueId = document.getElementById('venue-select').value;
    const date = document.getElementById('booking-date').value;
    
    if (!venueId || !date) {
        showAlert('Please select a venue and date first', 'warning');
        return;
    }
    
    try {
        showLoading(true);
        const response = await fetch(`${API_BASE}/bookings/availability?venue_id=${venueId}&date=${date}`);
        const data = await response.json();
        
        if (data.success) {
            displayAvailability(data);
        } else {
            showAlert('Failed to check availability', 'danger');
        }
    } catch (error) {
        console.error('Error checking availability:', error);
        showAlert('Error checking availability', 'danger');
    } finally {
        showLoading(false);
    }
}

function displayAvailability(data) {
    const venue = data.venue;
    const slots = data.slots;
    
    let availabilityHTML = `
        <div class="modal fade" id="availabilityModal" tabindex="-1">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">Availability for ${venue.name}</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body">
                        <p><strong>Date:</strong> ${data.date}</p>
                        <div class="row">
    `;
    
    slots.forEach(slot => {
        const statusClass = slot.available ? 'success' : 'danger';
        const statusText = slot.available ? 'Available' : 'Booked';
        
        availabilityHTML += `
            <div class="col-md-6 mb-2">
                <div class="card border-${statusClass}">
                    <div class="card-body py-2">
                        <div class="d-flex justify-content-between align-items-center">
                            <span>${slot.start_time} - ${slot.end_time}</span>
                            <span class="badge bg-${statusClass}">${statusText}</span>
                        </div>
                    </div>
                </div>
            </div>
        `;
    });
    
    availabilityHTML += `
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    // Remove existing modal if any
    const existingModal = document.getElementById('availabilityModal');
    if (existingModal) {
        existingModal.remove();
    }
    
    // Add new modal to body
    document.body.insertAdjacentHTML('beforeend', availabilityHTML);
    
    // Show modal
    const modal = new bootstrap.Modal(document.getElementById('availabilityModal'));
    modal.show();
}

async function loadUserBookings() {
    if (!currentUser) return;
    
    try {
        showLoading(true);
        const response = await fetch(`${API_BASE}/bookings/my-bookings`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });
        
        const data = await response.json();
        
        if (data.success) {
            userBookings = data.bookings;
            displayUserBookings(userBookings);
        } else {
            showAlert('Failed to load bookings', 'danger');
        }
    } catch (error) {
        console.error('Error loading bookings:', error);
        showAlert('Error loading bookings', 'danger');
    } finally {
        showLoading(false);
    }
}

function displayUserBookings(bookings) {
    const container = document.getElementById('my-bookings-container');
    if (!container) return;
    
    if (bookings.length === 0) {
        container.innerHTML = `
            <div class="text-center py-5">
                <i class="bi bi-calendar-x display-1 text-muted"></i>
                <h3 class="mt-3">No bookings found</h3>
                <p class="text-muted">You haven't made any bookings yet.</p>
                <button class="btn btn-primary" onclick="navigateToSection('booking')">
                    Make Your First Booking
                </button>
            </div>
        `;
        return;
    }
    
    let bookingsHTML = '<div class="row">';
    
    bookings.forEach(booking => {
        const venue = venues.find(v => v.id === booking.venue_id);
        const venueName = venue ? venue.name : booking.venue_id;
        
        bookingsHTML += `
            <div class="col-lg-6 mb-4">
                <div class="card">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <h6 class="mb-0">${booking.title}</h6>
                        <span class="status-badge status-${booking.status.toLowerCase()}">${booking.status}</span>
                    </div>
                    <div class="card-body">
                        <p class="card-text">${booking.description || 'No description'}</p>
                        <div class="row text-sm">
                            <div class="col-6">
                                <strong>Venue:</strong><br>
                                ${venueName}
                            </div>
                            <div class="col-6">
                                <strong>Date:</strong><br>
                                ${booking.booking_date}
                            </div>
                            <div class="col-6 mt-2">
                                <strong>Time:</strong><br>
                                ${booking.start_time} - ${booking.end_time}
                            </div>
                            <div class="col-6 mt-2">
                                <strong>Attendees:</strong><br>
                                ${booking.expected_attendees || 'Not specified'}
                            </div>
                        </div>
                    </div>
                    <div class="card-footer">
                        ${booking.status === 'PENDING' || booking.status === 'APPROVED' ? 
                            `<button class="btn btn-outline-danger btn-sm" onclick="cancelBooking('${booking.id}')">
                                <i class="bi bi-x-circle me-1"></i>Cancel
                            </button>` : ''
                        }
                        <small class="text-muted float-end">
                            Created: ${new Date(booking.created_at).toLocaleDateString()}
                        </small>
                    </div>
                </div>
            </div>
        `;
    });
    
    bookingsHTML += '</div>';
    container.innerHTML = bookingsHTML;
}

async function cancelBooking(bookingId) {
    if (!confirm('Are you sure you want to cancel this booking?')) {
        return;
    }
    
    try {
        showLoading(true);
        const response = await fetch(`${API_BASE}/bookings/${bookingId}/cancel`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });
        
        const data = await response.json();
        
        if (data.success) {
            showAlert('Booking cancelled successfully', 'success');
            loadUserBookings(); // Reload bookings
        } else {
            showAlert(data.error || 'Failed to cancel booking', 'danger');
        }
    } catch (error) {
        console.error('Error cancelling booking:', error);
        showAlert('Error cancelling booking', 'danger');
    } finally {
        showLoading(false);
    }
}

// Calendar functions
function initializeCalendarDropdowns() {
    const monthSelect = document.getElementById('calendar-month');
    const yearSelect = document.getElementById('calendar-year');
    
    if (monthSelect) {
        const months = [
            'January', 'February', 'March', 'April', 'May', 'June',
            'July', 'August', 'September', 'October', 'November', 'December'
        ];
        
        months.forEach((month, index) => {
            const option = document.createElement('option');
            option.value = index + 1;
            option.textContent = month;
            if (index + 1 === new Date().getMonth() + 1) {
                option.selected = true;
            }
            monthSelect.appendChild(option);
        });
    }
    
    if (yearSelect) {
        const currentYear = new Date().getFullYear();
        for (let year = currentYear; year <= currentYear + 2; year++) {
            const option = document.createElement('option');
            option.value = year;
            option.textContent = year;
            if (year === currentYear) {
                option.selected = true;
            }
            yearSelect.appendChild(option);
        }
    }
}

async function loadCalendarView() {
    const venueId = document.getElementById('calendar-venue-select').value;
    const month = document.getElementById('calendar-month').value;
    const year = document.getElementById('calendar-year').value;
    
    if (!venueId) {
        document.getElementById('calendar-container').innerHTML = `
            <div class="text-center text-muted">
                <i class="bi bi-calendar3 display-1"></i>
                <p>Select a venue to view the calendar</p>
            </div>
        `;
        return;
    }
    
    try {
        showLoading(true);
        const response = await fetch(`${API_BASE}/bookings/calendar-view?venue_id=${venueId}&month=${month}&year=${year}`);
        const data = await response.json();
        
        if (data.success) {
            displayCalendar(data);
        } else {
            showAlert('Failed to load calendar', 'danger');
        }
    } catch (error) {
        console.error('Error loading calendar:', error);
        showAlert('Error loading calendar', 'danger');
    } finally {
        showLoading(false);
    }
}

function displayCalendar(data) {
    const container = document.getElementById('calendar-container');
    if (!container) return;
    
    const venue = data.venue;
    const calendar = data.calendar;
    
    let calendarHTML = `
        <div class="card">
            <div class="card-header">
                <h5 class="mb-0">${venue.name} - ${getMonthName(data.month)} ${data.year}</h5>
            </div>
            <div class="card-body">
                <div class="calendar-grid">
                    <div class="calendar-day fw-bold text-center py-2">Sun</div>
                    <div class="calendar-day fw-bold text-center py-2">Mon</div>
                    <div class="calendar-day fw-bold text-center py-2">Tue</div>
                    <div class="calendar-day fw-bold text-center py-2">Wed</div>
                    <div class="calendar-day fw-bold text-center py-2">Thu</div>
                    <div class="calendar-day fw-bold text-center py-2">Fri</div>
                    <div class="calendar-day fw-bold text-center py-2">Sat</div>
    `;
    
    // Get first day of month and calculate starting position
    const firstDay = new Date(data.year, data.month - 1, 1);
    const startingDayOfWeek = firstDay.getDay();
    
    // Add empty cells for days before the first day of the month
    for (let i = 0; i < startingDayOfWeek; i++) {
        calendarHTML += '<div class="calendar-day other-month"></div>';
    }
    
    // Add days of the month
    Object.keys(calendar).sort().forEach(dateStr => {
        const dayData = calendar[dateStr];
        const dayNumber = new Date(dateStr).getDate();
        const availabilityClass = dayData.availability === 'full' ? '' : 
                                 dayData.availability === 'partial' ? 'has-bookings' : 'fully-booked';
        
        calendarHTML += `
            <div class="calendar-day ${availabilityClass}" onclick="showDayDetails('${dateStr}', ${JSON.stringify(dayData).replace(/"/g, '&quot;')})">
                <div class="calendar-day-number">${dayNumber}</div>
                ${dayData.bookings.slice(0, 2).map(booking => 
                    `<div class="calendar-booking">${booking.title}</div>`
                ).join('')}
                ${dayData.booking_count > 2 ? `<div class="calendar-booking">+${dayData.booking_count - 2} more</div>` : ''}
            </div>
        `;
    });
    
    calendarHTML += `
                </div>
                <div class="mt-3">
                    <div class="d-flex justify-content-center gap-4">
                        <div><span class="badge bg-success me-2"></span>Available</div>
                        <div><span class="badge bg-warning me-2"></span>Partially Booked</div>
                        <div><span class="badge bg-danger me-2"></span>Fully Booked</div>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    container.innerHTML = calendarHTML;
}

function getMonthName(monthNumber) {
    const months = [
        'January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December'
    ];
    return months[monthNumber - 1];
}

function showDayDetails(dateStr, dayData) {
    const date = new Date(dateStr);
    const formattedDate = date.toLocaleDateString('en-US', { 
        weekday: 'long', 
        year: 'numeric', 
        month: 'long', 
        day: 'numeric' 
    });
    
    let modalHTML = `
        <div class="modal fade" id="dayDetailsModal" tabindex="-1">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">${formattedDate}</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body">
    `;
    
    if (dayData.bookings.length === 0) {
        modalHTML += '<p class="text-muted">No bookings for this day.</p>';
    } else {
        modalHTML += '<div class="list-group">';
        dayData.bookings.forEach(booking => {
            modalHTML += `
                <div class="list-group-item">
                    <div class="d-flex justify-content-between align-items-start">
                        <div>
                            <h6 class="mb-1">${booking.title}</h6>
                            <p class="mb-1">${booking.start_time} - ${booking.end_time}</p>
                            <small class="text-muted">${booking.user_name || 'Unknown User'}</small>
                        </div>
                        <span class="status-badge status-${booking.status.toLowerCase()}">${booking.status}</span>
                    </div>
                </div>
            `;
        });
        modalHTML += '</div>';
    }
    
    modalHTML += `
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                        <button type="button" class="btn btn-primary" onclick="bookForDate('${dateStr}')">Book This Date</button>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    // Remove existing modal if any
    const existingModal = document.getElementById('dayDetailsModal');
    if (existingModal) {
        existingModal.remove();
    }
    
    // Add new modal to body
    document.body.insertAdjacentHTML('beforeend', modalHTML);
    
    // Show modal
    const modal = new bootstrap.Modal(document.getElementById('dayDetailsModal'));
    modal.show();
}

function bookForDate(dateStr) {
    // Close modal
    const modal = bootstrap.Modal.getInstance(document.getElementById('dayDetailsModal'));
    if (modal) {
        modal.hide();
    }
    
    // Navigate to booking section and pre-fill date
    navigateToSection('booking');
    setTimeout(() => {
        const dateInput = document.getElementById('booking-date');
        if (dateInput) {
            dateInput.value = dateStr;
        }
    }, 100);
}

// Utility functions
function showAlert(message, type = 'info') {
    // Remove existing alerts
    const existingAlerts = document.querySelectorAll('.alert-custom');
    existingAlerts.forEach(alert => alert.remove());
    
    const alertHTML = `
        <div class="alert alert-${type} alert-dismissible fade show alert-custom" role="alert" 
             style="position: fixed; top: 100px; right: 20px; z-index: 1050; min-width: 300px;">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
    
    document.body.insertAdjacentHTML('beforeend', alertHTML);
    
    // Auto-dismiss after 5 seconds
    setTimeout(() => {
        const alert = document.querySelector('.alert-custom');
        if (alert) {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }
    }, 5000);
}

function showLoading(show) {
    const spinner = document.getElementById('loading-spinner');
    if (spinner) {
        spinner.style.display = show ? 'flex' : 'none';
    }
}

function showLoginModal() {
    // Redirect to Firebase login page
    window.location.href = '/login';
}

function showDemoLogin() {
    // Redirect to Firebase login page
    window.location.href = '/login';
}



