// Dashboard JavaScript for Smart Slot Booking System

// Global variables
let venues = [];
let currentUser = null;

// API Base URL
const API_BASE = '/api';

// Initialize dashboard
document.addEventListener('DOMContentLoaded', function() {
    initializeDashboard();
});

function initializeDashboard() {
    // Check authentication
    checkAuthStatus();
    
    // Load venues
    loadVenues();
    
    // Set up event listeners
    setupEventListeners();
}

function setupEventListeners() {
    // Smooth scrolling for anchor links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
    
    // Reset venues button (for testing)
    const resetBtn = document.getElementById('reset-venues-btn');
    if (resetBtn) {
        resetBtn.addEventListener('click', resetVenues);
        // Show button for testing (remove in production)
        resetBtn.style.display = 'inline-block';
    }
}

function checkAuthStatus() {
    const token = localStorage.getItem('authToken');
    const userData = localStorage.getItem('userData');
    
    if (!token || !userData) {
        // Redirect to login if not authenticated
        window.location.href = '/login';
        return;
    }
    
    try {
        currentUser = JSON.parse(userData);
    } catch (error) {
        console.error('Error parsing user data:', error);
        window.location.href = '/login';
    }
}

async function loadVenues() {
    try {
        console.log('=== Dashboard: Loading venues ===');
        console.log('Dashboard: Making fetch request to /api/venues');
        
        const response = await fetch(`${API_BASE}/venues`);
        console.log('Dashboard: Response status:', response.status);
        console.log('Dashboard: Response ok:', response.ok);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status} - ${response.statusText}`);
        }
        
        const data = await response.json();
        console.log('Dashboard: Response data:', data);
        
        if (data.success) {
            venues = data.venues;
            console.log('Dashboard: Venues loaded successfully:', venues);
            displayVenues(venues);
        } else {
            console.error('Dashboard: Failed to load venues:', data.error);
            showError('Failed to load venues: ' + (data.error || 'Unknown error'));
        }
    } catch (error) {
        console.error('=== Dashboard: ERROR loading venues ===');
        console.error('Dashboard: Error details:', error);
        console.error('Dashboard: Error message:', error.message);
        console.error('Dashboard: Error stack:', error.stack);
        
        // Show more specific error message
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            showError('Network error: Unable to connect to server. Please check your connection and try again.');
        } else {
            showError('Error loading venues: ' + error.message);
        }
    }
}

function displayVenues(venueList) {
    const venuesGrid = document.getElementById('venues-grid');
    if (!venuesGrid) {
        console.error('Venues grid element not found');
        return;
    }
    
    console.log('Displaying venues:', venueList);
    
    if (!venueList || venueList.length === 0) {
        venuesGrid.innerHTML = `
            <div class="col-12 text-center">
                <div class="alert alert-info">
                    <i class="bi bi-info-circle me-2"></i>
                    No venues available at the moment.
                </div>
            </div>
        `;
        return;
    }
    
    venuesGrid.innerHTML = '';
    
    venueList.forEach(venue => {
        console.log('Creating venue card for:', venue);
        const venueCard = document.createElement('div');
        venueCard.className = 'venue-card';
        
        // Generate venue features from amenities
        const amenities = venue.amenities || [];
        const features = amenities.slice(0, 3).map(amenity => 
            `<span class="venue-feature">${amenity}</span>`
        ).join('');
        
        venueCard.innerHTML = `
            <div class="venue-status available">Available</div>
            <div class="venue-image" style="background-image: url('${getVenueImage(venue.name)}'); background-size: cover; background-position: center;">
                <div class="venue-image-overlay">
                    <i class="bi bi-building"></i>
                </div>
            </div>
            <div class="venue-details">
                <h3 class="venue-name">${venue.name}</h3>
                <p class="venue-capacity">
                    <i class="bi bi-people"></i>
                    Capacity: ${venue.capacity || 'N/A'} people
                </p>
                <div class="venue-features">
                    ${features}
                </div>
                                            <a href="/booking?venueId=${venue.id}&venueName=${encodeURIComponent(venue.name)}" class="book-venue-btn" onclick="setSelectedVenue(${venue.id}, '${venue.name}')">
                                <i class="bi bi-calendar-plus"></i>
                                Book Now
                            </a>
            </div>
        `;
        
        venuesGrid.appendChild(venueCard);
    });
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

async function resetVenues() {
    try {
        const resetBtn = document.getElementById('reset-venues-btn');
        if (resetBtn) {
            resetBtn.disabled = true;
            resetBtn.innerHTML = '<i class="bi bi-arrow-clockwise spin"></i> Resetting...';
        }
        
        const response = await fetch(`${API_BASE}/venues/reset-demo-public`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        const data = await response.json();
        
        if (data.success) {
            showSuccess('Successfully reset and created ' + data.venues.length + ' venues!');
            // Reload venues
            await loadVenues();
        } else {
            showError('Failed to reset venues: ' + (data.error || 'Unknown error'));
        }
    } catch (error) {
        console.error('Error resetting venues:', error);
        showError('Error resetting venues: ' + error.message);
    } finally {
        const resetBtn = document.getElementById('reset-venues-btn');
        if (resetBtn) {
            resetBtn.disabled = false;
            resetBtn.innerHTML = '<i class="bi bi-arrow-clockwise"></i> Reset Venues';
        }
    }
} 

// Function to set selected venue in localStorage
function setSelectedVenue(venueId, venueName) {
    console.log('Setting selected venue:', venueId, venueName);
    localStorage.setItem('selectedVenueId', venueId);
    localStorage.setItem('selectedVenueName', venueName);
}

// Function to get venue-specific images
function getVenueImage(venueName) {
    const venueImages = {
        'CS Auditorium': 'https://images.unsplash.com/photo-1497366216548-37526070297c?w=400&h=200&fit=crop',
        'ISE Seminar Hall': 'https://images.unsplash.com/photo-1577412647305-991150c7d163?w=400&h=200&fit=crop',
        'EC Conference Room': 'https://images.unsplash.com/photo-1517502884422-41eaead166d4?w=400&h=200&fit=crop',
        'Main Auditorium': 'https://images.unsplash.com/photo-1497366216548-37526070297c?w=400&h=200&fit=crop',
        'ME Lab Hall': 'https://images.unsplash.com/photo-1581091226825-a6a2a5aee158?w=400&h=200&fit=crop',
        'Civil Seminar Hall': 'https://images.unsplash.com/photo-1577412647305-991150c7d163?w=400&h=200&fit=crop',
        'Library Conference Room': 'https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=400&h=200&fit=crop',
        'Biotechnology Lab': 'https://images.unsplash.com/photo-1581091226825-a6a2a5aee158?w=400&h=200&fit=crop',
        'Architecture Studio': 'https://images.unsplash.com/photo-1517502884422-41eaead166d4?w=400&h=200&fit=crop',
        'Chemistry Seminar Hall': 'https://images.unsplash.com/photo-1581091226825-a6a2a5aee158?w=400&h=200&fit=crop',
        'Physics Lecture Hall': 'https://images.unsplash.com/photo-1577412647305-991150c7d163?w=400&h=200&fit=crop',
        'Mathematics Conference Room': 'https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=400&h=200&fit=crop',
        'Computer Lab A': 'https://images.unsplash.com/photo-1517502884422-41eaead166d4?w=400&h=200&fit=crop',
        'Innovation Center': 'https://images.unsplash.com/photo-1497366216548-37526070297c?w=400&h=200&fit=crop',
        'Sports Complex Hall': 'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=400&h=200&fit=crop'
    };
    
    return venueImages[venueName] || 'https://images.unsplash.com/photo-1497366216548-37526070297c?w=400&h=200&fit=crop';
} 