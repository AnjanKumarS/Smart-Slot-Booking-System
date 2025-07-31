// Booking JavaScript for Smart Slot Booking System

// Global variables
let venues = [];
let currentUser = null;

// API Base URL
const API_BASE = '/api';

// Initialize booking page
document.addEventListener('DOMContentLoaded', function() {
    initializeBooking();
});

function initializeBooking() {
    // Load venues
    loadVenues();
    
    // Set up event listeners
    setupEventListeners();
    
    // Initialize date inputs
    initializeDateInputs();
    
    // Auto-fill venue if selected from dashboard
    autoFillSelectedVenue();
    
    // Check if venue is pre-selected and trigger change event
    setTimeout(() => {
        const venueSelect = document.getElementById('venue');
        if (venueSelect && venueSelect.value) {
            const event = new Event('change');
            venueSelect.dispatchEvent(event);
        }
    }, 500);
}

function setupEventListeners() {
    // Booking form
    const bookingForm = document.getElementById('bookingForm');
    if (bookingForm) {
        bookingForm.addEventListener('submit', handleBookingSubmit);
    }
    
    // Venue selection change
    const venueSelect = document.getElementById('venue');
    if (venueSelect) {
        venueSelect.addEventListener('change', handleVenueChange);
    }
    
    // Date selection change
    const dateInput = document.getElementById('date');
    if (dateInput) {
        dateInput.addEventListener('change', handleDateChange);
    }
}

function loadVenues() {
    fetch(`${API_BASE}/venues`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                venues = data.venues;
                populateVenueSelect(venues);
            } else {
                showError('Failed to load venues: ' + (data.error || 'Unknown error'));
            }
        })
        .catch(error => {
            console.error('Error loading venues:', error);
            showError('Error loading venues: ' + error.message);
        });
}

function populateVenueSelect(venueList) {
    const venueSelect = document.getElementById('venue');
    if (!venueSelect) return;
    
    // Clear existing options
    venueSelect.innerHTML = '<option value="">Select a venue...</option>';
    
    venueList.forEach(venue => {
        const option = document.createElement('option');
        option.value = venue.id;
        option.textContent = `${venue.name} - ${venue.location} (Capacity: ${venue.capacity})`;
        venueSelect.appendChild(option);
    });
}

function handleVenueChange() {
    const venueSelect = document.getElementById('venue');
    const dateInput = document.getElementById('date');
    
    if (venueSelect.value && dateInput.value) {
        loadAvailableSlots(venueSelect.value, dateInput.value);
    }
}

function handleDateChange() {
    const venueSelect = document.getElementById('venue');
    const dateInput = document.getElementById('date');
    
    if (venueSelect.value && dateInput.value) {
        loadAvailableSlots(venueSelect.value, dateInput.value);
    }
}

function loadAvailableSlots(venueId, date) {
    fetch(`${API_BASE}/bookings/available-slots?venueId=${venueId}&date=${date}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                displayAvailableSlots(data.availableSlots);
            } else {
                console.error('Failed to load available slots:', data.error);
            }
        })
        .catch(error => {
            console.error('Error loading available slots:', error);
        });
}

function displayAvailableSlots(availableSlots) {
    const slotsContainer = document.getElementById('available-slots');
    const containerDiv = document.getElementById('available-slots-container');
    
    if (!slotsContainer || !containerDiv) return;
    
    if (availableSlots.length === 0) {
        slotsContainer.innerHTML = '<div class="alert alert-warning">No available slots for this date.</div>';
        containerDiv.style.display = 'block';
        return;
    }
    
    let slotsHtml = '';
    availableSlots.forEach(slot => {
        slotsHtml += `
            <div class="time-slot" onclick="selectTimeSlot('${slot.startTime}', '${slot.endTime}')">
                <div class="slot-time">
                    <i class="bi bi-clock me-2"></i>
                    ${slot.startTime} - ${slot.endTime}
                </div>
                <div class="slot-status">
                    <i class="bi bi-check-circle"></i>
                    Available
                </div>
            </div>
        `;
    });
    
    slotsContainer.innerHTML = slotsHtml;
    containerDiv.style.display = 'block';
}

function selectTimeSlot(startTime, endTime) {
    // Set the time values
    document.getElementById('start-time').value = startTime;
    document.getElementById('end-time').value = endTime;
    
    // Remove selected class from all time slots
    document.querySelectorAll('.time-slot').forEach(slot => {
        slot.classList.remove('selected');
    });
    
    // Add selected class to the clicked slot
    event.target.closest('.time-slot').classList.add('selected');
    
    // Show success feedback
    showSuccess(`Time slot selected: ${startTime} - ${endTime}`);
}

function initializeDateInputs() {
    const today = new Date().toISOString().split('T')[0];
    const dateInput = document.getElementById('date');
    if (dateInput) {
        dateInput.min = today;
    }
}

function autoFillSelectedVenue() {
    // Check URL parameters first
    const urlParams = new URLSearchParams(window.location.search);
    const venueIdFromUrl = urlParams.get('venueId');
    const venueNameFromUrl = urlParams.get('venueName');
    
    // Check localStorage as fallback
    const selectedVenueId = venueIdFromUrl || localStorage.getItem('selectedVenueId');
    const selectedVenueName = venueNameFromUrl || localStorage.getItem('selectedVenueName');
    
    console.log('Auto-fill venue check:', { venueIdFromUrl, venueNameFromUrl, selectedVenueId, selectedVenueName });
    
    if (selectedVenueId && selectedVenueName) {
        // Try to set the venue immediately if the select is already populated
        const venueSelect = document.getElementById('venue');
        if (venueSelect && venueSelect.options.length > 1) {
            venueSelect.value = selectedVenueId;
            console.log('Venue auto-filled successfully:', selectedVenueId, selectedVenueName);
            
            // Clear localStorage after setting
            localStorage.removeItem('selectedVenueId');
            localStorage.removeItem('selectedVenueName');
            
            // Trigger change event to load available slots
            const event = new Event('change');
            venueSelect.dispatchEvent(event);
        } else {
            // If venues aren't loaded yet, wait and retry
            const checkInterval = setInterval(() => {
                const venueSelect = document.getElementById('venue');
                if (venueSelect && venueSelect.options.length > 1) {
                    venueSelect.value = selectedVenueId;
                    // Clear localStorage after setting
                    localStorage.removeItem('selectedVenueId');
                    localStorage.removeItem('selectedVenueName');
                    
                    // Trigger change event to load available slots
                    const event = new Event('change');
                    venueSelect.dispatchEvent(event);
                    
                    clearInterval(checkInterval);
                }
            }, 100);
            
            // Stop checking after 5 seconds
            setTimeout(() => {
                clearInterval(checkInterval);
            }, 5000);
        }
    }
}

async function handleBookingSubmit(e) {
    e.preventDefault();
    
    const submitBtn = document.getElementById('submitBtn');
    const originalText = submitBtn.innerHTML;
    
    // Show loading state
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<div class="spinner"></div><span>Processing...</span>';
    
    const formData = new FormData(e.target);
    const bookingData = {
        venueId: formData.get('venue'),
        date: formData.get('date'),
        startTime: formData.get('start-time'),
        endTime: formData.get('end-time'),
        title: formData.get('title'),
        purpose: formData.get('purpose')
    };
    
    // Validation
    if (!bookingData.venueId || !bookingData.date || !bookingData.startTime || !bookingData.endTime || !bookingData.title) {
        showError('Please fill in all required fields');
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalText;
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE}/bookings`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(bookingData)
        });
        
        const data = await response.json();
        
        if (data.success) {
            showSuccess(data.message || 'Booking submitted successfully!');
            
            // Redirect based on booking status
            if (data.redirectUrl) {
                setTimeout(() => {
                    window.location.href = data.redirectUrl;
                }, 2000);
            }
        } else {
            // Handle specific error types
            if (data.conflictType) {
                showError(`Booking conflict: ${data.error}. Please choose a different time slot.`);
            } else {
                showError(data.error || 'Failed to submit booking');
            }
        }
    } catch (error) {
        console.error('Error submitting booking:', error);
        showError('Error submitting booking: ' + error.message);
    } finally {
        // Reset button state
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalText;
    }
}

function showSuccess(message) {
    const alertDiv = document.createElement('div');
    alertDiv.className = 'alert alert-success alert-dismissible fade show position-fixed';
    alertDiv.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px;';
    alertDiv.innerHTML = `
        <i class="bi bi-check-circle me-2"></i>${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    document.body.appendChild(alertDiv);
    
    setTimeout(() => {
        if (alertDiv.parentNode) {
            alertDiv.remove();
        }
    }, 5000);
}

function showError(message) {
    const alertDiv = document.createElement('div');
    alertDiv.className = 'alert alert-danger alert-dismissible fade show position-fixed';
    alertDiv.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px;';
    alertDiv.innerHTML = `
        <i class="bi bi-exclamation-triangle me-2"></i>${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    document.body.appendChild(alertDiv);
    
    setTimeout(() => {
        if (alertDiv.parentNode) {
            alertDiv.remove();
        }
    }, 5000);
}

