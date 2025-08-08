// User Bookings JavaScript - Client-side filtering approach
const API_BASE = '/api';

let allBookings = [];

// Initialize the page
document.addEventListener('DOMContentLoaded', function () {
    console.log('=== User Bookings: Initializing ===');
    loadUserBookings();
    setupTabFiltering();
});

function setupTabFiltering() {
    // Get all tab buttons
    const tabButtons = document.querySelectorAll('[data-bs-toggle="tab"]');
    
    tabButtons.forEach(button => {
        button.addEventListener('click', function (e) {
            e.preventDefault();
            
            // Remove active class from all tabs
            tabButtons.forEach(btn => btn.classList.remove('active'));
            
            // Add active class to clicked tab
            this.classList.add('active');
            
            // Get the target tab content
            const targetId = this.getAttribute('data-bs-target');
            const targetContent = document.querySelector(targetId);
            
            // Hide all tab contents
            document.querySelectorAll('.tab-pane').forEach(pane => {
                pane.classList.remove('show', 'active');
            });
            
            // Show target content
            if (targetContent) {
                targetContent.classList.add('show', 'active');
            }
            
            // Filter bookings based on tab
            const status = this.getAttribute('data-status');
            filterBookings(status);
        });
    });
}

function filterBookings(status) {
    console.log('=== Filtering bookings ===');
    console.log('Filter:', status);
    console.log('All bookings:', allBookings);
    
    let filteredBookings;
    
    if (status === 'all' || !status) {
        filteredBookings = allBookings;
    } else {
        filteredBookings = allBookings.filter(booking => 
            booking.status.toUpperCase() === status.toUpperCase()
        );
    }
    
    console.log('Filtered bookings:', filteredBookings);
    
    // Display filtered bookings
    displayBookings(filteredBookings, status);
}

function displayBookings(bookings, status) {
    console.log('=== Displaying bookings ===');
    console.log('Filter:', status);
    console.log('Bookings to display:', bookings);
    
    const containerId = status === 'all' || !status ? 'all-bookings' : `${status}-bookings`;
    const container = document.getElementById(containerId);
    
    if (!container) {
        console.error('Container not found:', containerId);
        return;
    }
    
    if (bookings.length === 0) {
        container.innerHTML = `
            <div class="col-12">
                <div class="empty-state">
                    <div class="empty-state-icon">
                        <i class="bi bi-calendar-x"></i>
                    </div>
                    <div class="empty-state-title">No Bookings Found</div>
                    <div class="empty-state-text">
                        ${status === 'all' || !status ? 
                            "You haven't made any bookings yet." : 
                            `You don't have any ${status.toLowerCase()} bookings.`}
                    </div>
                    <a href="/booking" class="empty-state-btn">
                        <i class="bi bi-plus-circle me-2"></i>Book a Venue
                    </a>
                </div>
            </div>
        `;
        return;
    }
    
    container.innerHTML = '';
    
    bookings.forEach(booking => {
        const bookingCard = createBookingCard(booking);
        container.appendChild(bookingCard);
    });
}

function createBookingCard(booking) {
    const card = document.createElement('div');
    card.className = 'col-lg-6 col-xl-4 mb-4';
    card.setAttribute('data-status', booking.status.toLowerCase());
    
    const statusClass = getStatusClass(booking.status);
    const statusIcon = getStatusIcon(booking.status);
    
    card.innerHTML = `
        <div class="booking-card">
            <div class="booking-header">
                <div class="booking-title">${booking.title}</div>
                <div class="booking-venue">
                    <i class="bi bi-building me-1"></i>${booking.venue?.name || 'Unknown Venue'}
                </div>
                <div class="booking-date">
                    <i class="bi bi-calendar me-1"></i>${formatDate(booking.bookingDate)}
                </div>
            </div>
            <div class="booking-body">
                <div class="booking-details">
                    <div class="detail-item">
                        <div class="detail-icon time">
                            <i class="bi bi-clock"></i>
                        </div>
                        <div class="detail-text">
                            <div class="detail-label">Time</div>
                            <div class="detail-value">${booking.startTime} - ${booking.endTime}</div>
                        </div>
                    </div>
                    <div class="detail-item">
                        <div class="detail-icon capacity">
                            <i class="bi bi-people"></i>
                        </div>
                        <div class="detail-text">
                            <div class="detail-label">Capacity</div>
                            <div class="detail-value">${booking.venue?.capacity || 'N/A'} people</div>
                        </div>
                    </div>
                    ${booking.purpose ? `
                    <div class="detail-item">
                        <div class="detail-icon purpose">
                            <i class="bi bi-chat-text"></i>
                        </div>
                        <div class="detail-text">
                            <div class="detail-label">Purpose</div>
                            <div class="detail-value">${booking.purpose}</div>
                        </div>
                    </div>
                    ` : ''}
                </div>
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <span class="status-badge ${statusClass}">
                        <i class="bi ${statusIcon}"></i>
                        ${booking.status}
                    </span>
                </div>
                <div class="booking-actions">
                    ${booking.status === 'PENDING' ? `
                        <button class="action-btn danger" onclick="cancelBooking(${booking.id})">
                            <i class="bi bi-x-circle"></i>Cancel
                        </button>
                    ` : ''}
                    ${booking.status === 'CONFIRMED' ? `
                        <a href="/booking/${booking.id}" class="action-btn primary">
                            <i class="bi bi-eye"></i>View Details
                        </a>
                    ` : ''}
                    <button class="action-btn secondary" onclick="copyBookingInfo(${booking.id})">
                        <i class="bi bi-clipboard"></i>Copy Info
                    </button>
                </div>
            </div>
        </div>
    `;
    
    return card;
}

function getStatusClass(status) {
    switch (status.toUpperCase()) {
        case 'CONFIRMED': return 'status-confirmed';
        case 'PENDING': return 'status-pending';
        case 'REJECTED': return 'status-rejected';
        case 'CANCELLED': return 'status-cancelled';
        default: return 'status-pending';
    }
}

function getStatusIcon(status) {
    switch (status.toUpperCase()) {
        case 'CONFIRMED': return 'bi-check-circle';
        case 'PENDING': return 'bi-clock';
        case 'REJECTED': return 'bi-x-circle';
        case 'CANCELLED': return 'bi-x-circle';
        default: return 'bi-clock';
    }
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
}

async function loadUserBookings() {
    try {
        console.log('=== User Bookings: Loading user bookings ===');
        console.log('User Bookings: Making fetch request to /api/bookings/user');
        
        const response = await fetch(`${API_BASE}/bookings/user`);
        console.log('User Bookings: Response status:', response.status);
        console.log('User Bookings: Response ok:', response.ok);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status} - ${response.statusText}`);
        }
        
        const data = await response.json();
        console.log('User Bookings: Response data:', data);
        console.log('User Bookings: Bookings array:', data.bookings);
        
        if (data.success) {
            allBookings = data.bookings;
            console.log('User Bookings: Bookings loaded successfully:', allBookings);
            console.log('User Bookings: Number of bookings:', allBookings.length);
            
            // Log each booking for debugging
            allBookings.forEach((booking, index) => {
                console.log(`Booking ${index + 1}:`, {
                    id: booking.id,
                    title: booking.title,
                    status: booking.status,
                    venue: booking.venue,
                    bookingDate: booking.bookingDate,
                    startTime: booking.startTime,
                    endTime: booking.endTime
                });
            });
            
            // Display all bookings initially
            displayBookings(allBookings, 'all');
            
        } else {
            console.error('User Bookings: Failed to load bookings:', data.error);
            showError('Failed to load bookings: ' + (data.error || 'Unknown error'));
        }
    } catch (error) {
        console.error('=== User Bookings: ERROR loading bookings ===');
        console.error('User Bookings: Error details:', error);
        console.error('User Bookings: Error message:', error.message);
        console.error('User Bookings: Error stack:', error.stack);
        showError('Error loading bookings: ' + error.message);
    }
}

function showError(message) {
    console.error('User Bookings: Error:', message);
    // You can implement a proper error display here
    alert('Error: ' + message);
}

// Global functions for booking actions
function cancelBooking(bookingId) {
    if (confirm('Are you sure you want to cancel this booking?')) {
        console.log('Cancelling booking:', bookingId);
        // Implement cancel booking logic here
    }
}

function copyBookingInfo(bookingId) {
    console.log('Copying booking info:', bookingId);
    // Implement copy booking info logic here
    alert('Booking info copied to clipboard!');
} 