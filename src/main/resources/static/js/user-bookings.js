// User Bookings JavaScript for Smart Slot Booking System

// Global variables
let allBookings = [];
let currentUser = null;

// API Base URL
const API_BASE = '/api';

// Initialize page
document.addEventListener('DOMContentLoaded', function() {
    initializeUserBookings();
});

function initializeUserBookings() {
    // Load bookings
    loadUserBookings();
    
    // Set up event listeners
    setupEventListeners();
    
    // Initialize the first tab (All Bookings)
    setTimeout(() => {
        filterBookings('all');
    }, 100);
}

function setupEventListeners() {
    // Tab change events
    document.querySelectorAll('[data-bs-toggle="tab"]').forEach(tab => {
        tab.addEventListener('shown.bs.tab', function (event) {
            const target = event.target.getAttribute('data-bs-target');
            filterBookings(target);
        });
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
            
            // Populate all tabs
            displayBookings(allBookings, 'all');
            displayBookings(allBookings.filter(b => b.status === 'CONFIRMED'), 'confirmed');
            displayBookings(allBookings.filter(b => b.status === 'PENDING'), 'pending');
            displayBookings(allBookings.filter(b => b.status === 'REJECTED'), 'cancelled');
        } else {
            console.error('User Bookings: Failed to load bookings:', data.error);
            showError('Failed to load bookings: ' + (data.error || 'Unknown error'));
        }
    } catch (error) {
        console.error('=== User Bookings: ERROR loading bookings ===');
        console.error('User Bookings: Error details:', error);
        console.error('User Bookings: Error message:', error.message);
        console.error('User Bookings: Error stack:', error.stack);
        
        // Show more specific error message
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            showError('Network error: Unable to connect to server. Please check your connection and try again.');
        } else {
            showError('Error loading bookings: ' + error.message);
        }
    }
}

function displayBookings(bookings, filter) {
    console.log('=== Displaying bookings ===');
    console.log('Filter:', filter);
    console.log('Bookings to display:', bookings);
    
    const containerId = `${filter}-bookings`;
    const container = document.getElementById(containerId);
    
    if (!container) {
        console.error('Container not found:', containerId);
        return;
    }
    
    if (bookings.length === 0) {
        container.innerHTML = `
            <div class="col-12 text-center">
                <div class="alert alert-info">
                    <i class="bi bi-info-circle me-2"></i>
                    No ${filter} bookings found.
                </div>
            </div>
        `;
        return;
    }
    
    container.innerHTML = '';
    
    bookings.forEach(booking => {
        console.log('Processing booking:', booking);
        
        const bookingCard = document.createElement('div');
        bookingCard.className = 'col-lg-6 col-xl-4 mb-4';
        
        const statusClass = getStatusClass(booking.status);
        const statusText = getStatusText(booking.status);
        
        // Get venue name safely
        const venueName = booking.venue && booking.venue.name ? booking.venue.name : 'Unknown Venue';
        const venueLocation = booking.venue && booking.venue.location ? booking.venue.location : '';
        
        bookingCard.innerHTML = `
            <div class="booking-card">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-start mb-3">
                        <h5 class="card-title mb-0">${booking.title || 'Untitled Event'}</h5>
                        <span class="status-badge ${statusClass}">${statusText}</span>
                    </div>
                    
                    <div class="mb-3">
                        <small class="text-muted">
                            <i class="bi bi-building me-1"></i>
                            ${venueName}${venueLocation ? ` - ${venueLocation}` : ''}
                        </small>
                    </div>
                    
                    <div class="row mb-3">
                        <div class="col-6">
                            <small class="text-muted">
                                <i class="bi bi-calendar me-1"></i>
                                ${formatDate(booking.bookingDate)}
                            </small>
                        </div>
                        <div class="col-6">
                            <small class="text-muted">
                                <i class="bi bi-clock me-1"></i>
                                ${booking.startTime} - ${booking.endTime}
                            </small>
                        </div>
                    </div>
                    
                    ${booking.purpose ? `
                        <div class="mb-3">
                            <small class="text-muted">
                                <i class="bi bi-info-circle me-1"></i>
                                ${booking.purpose}
                            </small>
                        </div>
                    ` : ''}
                    
                    <div class="d-flex justify-content-between align-items-center">
                        <small class="text-muted">
                            ID: #${booking.id}
                        </small>
                        <div class="btn-group btn-group-sm">
                            <button class="btn btn-outline-primary btn-sm" onclick="viewDetails(${booking.id})">
                                <i class="bi bi-eye me-1"></i>Details
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
        
        container.appendChild(bookingCard);
    });
}

function filterBookings(filter) {
    console.log('=== Filtering bookings ===');
    console.log('Filter:', filter);
    console.log('All bookings:', allBookings);
    
    let filteredBookings = [];
    
    switch (filter) {
        case 'all':
            filteredBookings = allBookings;
            break;
        case 'confirmed':
            filteredBookings = allBookings.filter(booking => booking.status === 'CONFIRMED');
            break;
        case 'pending':
            filteredBookings = allBookings.filter(booking => booking.status === 'PENDING');
            break;
        case 'cancelled':
            filteredBookings = allBookings.filter(booking => booking.status === 'REJECTED');
            break;
        default:
            filteredBookings = allBookings;
    }
    
    console.log('Filtered bookings:', filteredBookings);
    displayBookings(filteredBookings, filter.replace('#', ''));
}

function getStatusClass(status) {
    switch (status) {
        case 'PENDING':
            return 'status-pending';
        case 'CONFIRMED':
            return 'status-confirmed';
        case 'REJECTED':
            return 'status-rejected';
        default:
            return 'status-pending';
    }
}

function getStatusText(status) {
    switch (status) {
        case 'PENDING':
            return 'Pending';
        case 'CONFIRMED':
            return 'Confirmed';
        case 'REJECTED':
            return 'Rejected';
        default:
            return status;
    }
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    });
}

function viewDetails(bookingId) {
    // For now, just show an alert. This can be expanded to show a modal with details
    const booking = allBookings.find(b => b.id === bookingId);
    if (booking) {
        alert(`Booking Details:\n\nVenue: ${booking.venue?.name}\nDate: ${formatDate(booking.bookingDate)}\nTime: ${booking.startTime} - ${booking.endTime}\nPurpose: ${booking.purpose || 'Not specified'}`);
    }
}

function showSuccess(message) {
    // Create success alert
    const alertDiv = document.createElement('div');
    alertDiv.className = 'alert alert-success alert-dismissible fade show position-fixed';
    alertDiv.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px;';
    alertDiv.innerHTML = `
        <i class="bi bi-check-circle me-2"></i>${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    document.body.appendChild(alertDiv);
    
    // Auto-remove after 5 seconds
    setTimeout(() => {
        if (alertDiv.parentNode) {
            alertDiv.remove();
        }
    }, 5000);
}

function showError(message) {
    // Create error alert
    const alertDiv = document.createElement('div');
    alertDiv.className = 'alert alert-danger alert-dismissible fade show position-fixed';
    alertDiv.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px;';
    alertDiv.innerHTML = `
        <i class="bi bi-exclamation-triangle me-2"></i>${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    document.body.appendChild(alertDiv);
    
    // Auto-remove after 5 seconds
    setTimeout(() => {
        if (alertDiv.parentNode) {
            alertDiv.remove();
        }
    }, 5000);
} 