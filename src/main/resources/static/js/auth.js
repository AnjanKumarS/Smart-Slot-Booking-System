// Firebase Authentication JavaScript for Smart Slot Booking System

// Global variables
let currentBookingId = null;
let otpTimer = null;
let currentUser = null;

// API Base URL
const API_BASE = window.location.origin + '/api';

// Initialize authentication event listeners
document.addEventListener('DOMContentLoaded', function() {
    setupAuthEventListeners();
    setupFirebaseAuthStateListener();
});

function setupAuthEventListeners() {
    // Login form
    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', handleFirebaseLogin);
    }
    
    // Register form
    const registerForm = document.getElementById('register-form');
    if (registerForm) {
        registerForm.addEventListener('submit', handleFirebaseRegister);
    }
    
    // Google login button
    const googleLoginBtn = document.getElementById('google-login-btn');
    if (googleLoginBtn) {
        googleLoginBtn.addEventListener('click', loginWithGoogle);
    }
    
    // Google register button
    const googleRegisterBtn = document.getElementById('google-register-btn');
    if (googleRegisterBtn) {
        googleRegisterBtn.addEventListener('click', registerWithGoogle);
    }
    
    // OTP form
    const otpForm = document.getElementById('otp-form');
    if (otpForm) {
        otpForm.addEventListener('submit', handleOTPVerification);
    }
    
    // OTP input formatting
    const otpInput = document.getElementById('otp-input');
    if (otpInput) {
        otpInput.addEventListener('input', function(e) {
            // Only allow numbers
            this.value = this.value.replace(/[^0-9]/g, '');
            
            // Auto-submit when 6 digits are entered
            if (this.value.length === 6) {
                handleOTPVerification(e);
            }
        });
    }
}

// Firebase Authentication Functions

// Setup Firebase Auth State Listener
function setupFirebaseAuthStateListener() {
    if (window.firebaseAuth) {
        window.firebaseAuth.onAuthStateChanged(function(user) {
            if (user) {
                console.log('User is signed in:', user.email);
                currentUser = user;
                handleSuccessfulAuth(user);
            } else {
                console.log('User is signed out');
                currentUser = null;
                handleSignOut();
            }
        });
    }
}

// Firebase Login Handler
async function handleFirebaseLogin(e) {
    e.preventDefault();
    
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    
    if (!email || !password) {
        handleFirebaseError({ code: 'validation', message: 'Please enter both email and password' }, 'login');
        return;
    }
    
    try {
        setLoadingState('login-btn', true);
        
        // Firebase Email/Password Authentication
        const userCredential = await window.firebaseAuth.signInWithEmailAndPassword(email, password);
        const user = userCredential.user;
        
        // Get ID token for backend authentication
        const idToken = await user.getIdToken();
        
        // Send token to backend for validation and user creation
        await authenticateWithBackend(idToken);
        
        // Show success message in alert box
        const alert = document.getElementById('login-alert');
        if (alert) {
            alert.textContent = `Welcome back, ${user.email}!`;
            alert.className = 'alert alert-success';
            alert.classList.remove('d-none');
        }
        
        // Redirect to dashboard
        setTimeout(() => {
            window.location.href = '/';
        }, 1000);
        
    } catch (error) {
        console.error('Firebase login error:', error);
        handleFirebaseError(error, 'login');
    } finally {
        setLoadingState('login-btn', false);
    }
}

// Firebase Register Handler
async function handleFirebaseRegister(e) {
    e.preventDefault();
    
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirm-password').value;
    
    // Validation
    if (!email || !password || !confirmPassword) {
        handleFirebaseError({ code: 'validation', message: 'Please fill in all fields' }, 'register');
        return;
    }
    
    if (password !== confirmPassword) {
        handleFirebaseError({ code: 'validation', message: 'Passwords do not match' }, 'register');
        return;
    }
    
    // Check password strength
    const strength = checkPasswordStrength(password);
    if (strength.score < 3) {
        handleFirebaseError({ code: 'validation', message: 'Password is too weak. Please choose a stronger password.' }, 'register');
        return;
    }
    
    try {
        setLoadingState('register-btn', true);
        
        // Firebase User Creation
        const userCredential = await window.firebaseAuth.createUserWithEmailAndPassword(email, password);
        const user = userCredential.user;
        
        // Get ID token for backend authentication
        const idToken = await user.getIdToken();
        
        // Send token to backend for user creation
        await authenticateWithBackend(idToken);
        
        // Show success message in alert box
        const alert = document.getElementById('register-alert');
        if (alert) {
            alert.textContent = `Account created successfully! Welcome, ${user.email}!`;
            alert.className = 'alert alert-success';
            alert.classList.remove('d-none');
        }
        
        // Redirect to dashboard
        setTimeout(() => {
            window.location.href = '/';
        }, 1000);
        
    } catch (error) {
        console.error('Firebase registration error:', error);
        handleFirebaseError(error, 'register');
    } finally {
        setLoadingState('register-btn', false);
    }
}

// Google Authentication
async function loginWithGoogle() {
    try {
        setLoadingState('google-login-btn', true);
        
        const provider = new firebase.auth.GoogleAuthProvider();
        const userCredential = await window.firebaseAuth.signInWithPopup(provider);
        const user = userCredential.user;
        
        // Get ID token for backend authentication
        const idToken = await user.getIdToken();
        
        // Send token to backend for validation and user creation
        await authenticateWithBackend(idToken);
        
        // Show success message in alert box
        const alert = document.getElementById('login-alert');
        if (alert) {
            alert.textContent = `Welcome, ${user.displayName || user.email}!`;
            alert.className = 'alert alert-success';
            alert.classList.remove('d-none');
        }
        
        // Redirect to dashboard
        setTimeout(() => {
            window.location.href = '/';
        }, 1000);
        
    } catch (error) {
        console.error('Google login error:', error);
        handleFirebaseError(error, 'google');
    } finally {
        setLoadingState('google-login-btn', false);
    }
}

// Google Registration (same as login for new users)
async function registerWithGoogle() {
    try {
        setLoadingState('google-register-btn', true);
        
        const provider = new firebase.auth.GoogleAuthProvider();
        const userCredential = await window.firebaseAuth.signInWithPopup(provider);
        const user = userCredential.user;
        
        // Get ID token for backend authentication
        const idToken = await user.getIdToken();
        
        // Send token to backend for validation and user creation
        await authenticateWithBackend(idToken);
        
        // Show success message in alert box
        const alert = document.getElementById('register-alert');
        if (alert) {
            alert.textContent = `Account created successfully! Welcome, ${user.displayName || user.email}!`;
            alert.className = 'alert alert-success';
            alert.classList.remove('d-none');
        }
        
        // Redirect to dashboard
        setTimeout(() => {
            window.location.href = '/';
        }, 1000);
        
    } catch (error) {
        console.error('Google registration error:', error);
        handleFirebaseError(error, 'register');
    } finally {
        setLoadingState('google-register-btn', false);
    }
}

// Authenticate with Backend
async function authenticateWithBackend(idToken) {
    try {
        const response = await fetch(`${API_BASE}/auth/session`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ idToken: idToken })
        });
        
        if (!response.ok) {
            throw new Error(`Backend authentication failed: ${response.status}`);
        }
        
        const data = await response.json();
        
        // Store authentication data
        localStorage.setItem('authToken', idToken);
        localStorage.setItem('userData', JSON.stringify(data.user || {}));
        
        return data;
        
    } catch (error) {
        console.error('Backend authentication error:', error);
        throw error;
    }
}

// Handle Successful Authentication
function handleSuccessfulAuth(user) {
    // Store user data
    localStorage.setItem('userData', JSON.stringify({
        uid: user.uid,
        email: user.email,
        displayName: user.displayName,
        photoURL: user.photoURL
    }));
    
    // Update UI if needed
    updateUIForAuthenticatedUser();
}

// Handle Sign Out
function handleSignOut() {
    // Clear local storage
    localStorage.removeItem('authToken');
    localStorage.removeItem('userData');
    
    // Update UI if needed
    updateUIForAuthenticatedUser();
}

// Handle Firebase Errors
function handleFirebaseError(error, context) {
    let message = 'An error occurred. Please try again.';
    if (error && error.code) {
        switch (error.code) {
            case 'validation':
                message = error.message;
                break;
            case 'auth/user-not-found':
                message = 'No user found with this email.';
                break;
            case 'auth/wrong-password':
                message = 'Incorrect password.';
                break;
            case 'auth/email-already-in-use':
                message = 'This email is already registered.';
                break;
            case 'auth/invalid-email':
                message = 'Invalid email address.';
                break;
            case 'auth/weak-password':
                message = 'Password is too weak (min 6 characters).';
                break;
            case 'auth/popup-closed-by-user':
                message = 'Google sign-in was cancelled.';
                break;
            default:
                message = error.message || message;
        }
    }
    // Show error in alert box if present
    if (context === 'login') {
        const alert = document.getElementById('login-alert');
        if (alert) {
            alert.textContent = message;
            alert.classList.remove('d-none');
        } else {
            alert(message);
        }
    } else if (context === 'register') {
        const alert = document.getElementById('register-alert');
        if (alert) {
            alert.textContent = message;
            alert.classList.remove('d-none');
        } else {
            alert(message);
        }
    } else {
        alert(message);
    }
}

// Loading State Management
function setLoadingState(buttonId, isLoading) {
    const button = document.getElementById(buttonId);
    if (!button) return;
    
    if (isLoading) {
        // Store original text
        if (!button.dataset.originalText) {
            button.dataset.originalText = button.innerHTML;
        }
        button.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Loading...';
        button.disabled = true;
    } else {
        // Restore original text
        if (button.dataset.originalText) {
            button.innerHTML = button.dataset.originalText;
        }
        button.disabled = false;
    }
}

// Utility Functions

// Show Alert Function
function showAlert(message, type = 'info') {
    const alertContainer = document.getElementById('alert-container');
    if (!alertContainer) {
        // Fallback to regular alert if no container
        alert(message);
        return;
    }
    
    const alertId = 'alert-' + Date.now();
    const alertHtml = `
        <div id="${alertId}" class="alert alert-${type} alert-dismissible fade show" role="alert">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    `;
    
    alertContainer.innerHTML = alertHtml;
    
    // Auto-dismiss after 5 seconds
    setTimeout(() => {
        const alertElement = document.getElementById(alertId);
        if (alertElement) {
            const bsAlert = new bootstrap.Alert(alertElement);
            bsAlert.close();
        }
    }, 5000);
}

// Update UI for authenticated user
function updateUIForAuthenticatedUser() {
    // This function can be extended to update UI elements
    // based on authentication state
    console.log('UI updated for authenticated user');
}

// Check if user is authenticated
function isAuthenticated() {
    return !!localStorage.getItem('authToken');
}

// Get current user data
function getCurrentUser() {
    const userData = localStorage.getItem('userData');
    if (userData) {
        try {
            return JSON.parse(userData);
        } catch (error) {
            console.error('Error parsing user data:', error);
            return null;
        }
    }
    return null;
}

// Logout function
function logout() {
    if (window.firebaseAuth) {
        window.firebaseAuth.signOut().then(() => {
            console.log('User signed out successfully');
        }).catch((error) => {
            console.error('Sign out error:', error);
        });
    }
    
    // Clear local storage
    localStorage.removeItem('authToken');
    localStorage.removeItem('userData');
    
    // Redirect to home page
    window.location.href = '/';
}

// OTP functions (for booking verification)
function showOTPModal(bookingId, otp) {
    currentBookingId = bookingId;
    
    // For demo purposes, show the OTP in the modal
    // In production, this would be sent via email/SMS
    const otpModal = document.getElementById('otpModal');
    const otpInput = document.getElementById('otp-input');
    
    if (otpInput) {
        otpInput.value = '';
        otpInput.focus();
    }
    
    // Show the OTP in an alert for demo purposes
    showAlert(`Demo OTP: ${otp} (In production, this would be sent to your email)`, 'info');
    
    // Start countdown timer
    startOTPTimer(600); // 10 minutes
    
    const modal = new bootstrap.Modal(otpModal);
    modal.show();
}

async function handleOTPVerification(e) {
    e.preventDefault();
    
    const otp = document.getElementById('otp-input').value;
    
    if (!otp || otp.length !== 6) {
        showAlert('Please enter a valid 6-digit OTP', 'warning');
        return;
    }
    
    if (!currentBookingId) {
        showAlert('Invalid booking session', 'danger');
        return;
    }
    
    try {
        setLoadingState('otp-btn', true);
        
        const response = await fetch(`${API_BASE}/bookings/verify-otp`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            },
            body: JSON.stringify({
                booking_id: currentBookingId,
                otp: otp
            })
        });
        
        const data = await response.json();
        
        if (data.success) {
            // Close OTP modal
            const modal = bootstrap.Modal.getInstance(document.getElementById('otpModal'));
            if (modal) {
                modal.hide();
            }
            
            // Clear OTP timer
            if (otpTimer) {
                clearInterval(otpTimer);
                otpTimer = null;
            }
            
            // Reset booking form
            document.getElementById('booking-form').reset();
            
            showAlert('Booking confirmed successfully! Your request is pending approval.', 'success');
            
            // Navigate to user bookings
            navigateToSection('my-bookings');
            
            // Reset current booking ID
            currentBookingId = null;
        } else {
            showAlert(data.error || 'OTP verification failed', 'danger');
            
            // Clear OTP input for retry
            document.getElementById('otp-input').value = '';
        }
    } catch (error) {
        console.error('OTP verification error:', error);
        showAlert('OTP verification failed. Please try again.', 'danger');
    } finally {
        setLoadingState('otp-btn', false);
    }
}

function startOTPTimer(seconds) {
    const timerElement = document.getElementById('otp-timer');
    if (!timerElement) return;
    
    let remainingTime = seconds;
    
    otpTimer = setInterval(() => {
        const minutes = Math.floor(remainingTime / 60);
        const secs = remainingTime % 60;
        
        timerElement.textContent = `${minutes}:${secs.toString().padStart(2, '0')}`;
        
        if (remainingTime <= 0) {
            clearInterval(otpTimer);
            otpTimer = null;
            
            // Close OTP modal
            const modal = bootstrap.Modal.getInstance(document.getElementById('otpModal'));
            if (modal) {
                modal.hide();
            }
            
            showAlert('OTP expired. Please try booking again.', 'warning');
            currentBookingId = null;
        }
        
        remainingTime--;
    }, 1000);
}

// Token management
function getAuthToken() {
    return localStorage.getItem('authToken');
}

function hasRole(requiredRole) {
    const user = getCurrentUser();
    if (!user) return false;
    
    const userRole = user.role || 'USER';
    
    if (Array.isArray(requiredRole)) {
        return requiredRole.includes(userRole);
    }
    
    return userRole === requiredRole;
}

function requireAuth() {
    if (!isAuthenticated()) {
        window.location.href = '/login';
        return false;
    }
    return true;
}

function requireRole(requiredRole) {
    if (!requireAuth()) return false;
    
    if (!hasRole(requiredRole)) {
        showAlert('Access denied. Insufficient permissions.', 'danger');
        return false;
    }
    
    return true;
}

// Password strength checker
function checkPasswordStrength(password) {
    let strength = 0;
    const checks = {
        length: password.length >= 8,
        lowercase: /[a-z]/.test(password),
        uppercase: /[A-Z]/.test(password),
        numbers: /\d/.test(password),
        special: /[!@#$%^&*(),.?":{}|<>]/.test(password)
    };
    
    Object.values(checks).forEach(check => {
        if (check) strength++;
    });
    
    return {
        score: strength,
        checks: checks,
        level: strength < 3 ? 'weak' : strength < 5 ? 'medium' : 'strong'
    };
}

// Session management
function refreshAuthToken() {
    // In production, this would refresh the Firebase token
    const token = getAuthToken();
    if (!token) {
        logout();
        return false;
    }
    
    return true;
}

function setupTokenRefresh() {
    // Refresh token every 50 minutes (Firebase tokens expire after 1 hour)
    setInterval(() => {
        if (isAuthenticated()) {
            refreshAuthToken();
        }
    }, 50 * 60 * 1000);
}

// Initialize token refresh on page load
document.addEventListener('DOMContentLoaded', function() {
    setupTokenRefresh();
});

// Handle authentication errors globally
function handleAuthError(error) {
    console.error('Authentication error:', error);
    
    if (error.status === 401) {
        // Token expired or invalid
        logout();
        showAlert('Your session has expired. Please log in again.', 'warning');
    } else if (error.status === 403) {
        // Insufficient permissions
        showAlert('Access denied. You do not have permission to perform this action.', 'danger');
    } else {
        // Other authentication errors
        showAlert('Authentication error. Please try again.', 'danger');
    }
}

// Utility function to make authenticated API calls
async function authenticatedFetch(url, options = {}) {
    const token = getAuthToken();
    
    if (!token) {
        throw new Error('No authentication token available');
    }
    
    const defaultOptions = {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
            ...options.headers
        }
    };
    
    const mergedOptions = { ...defaultOptions, ...options };
    
    try {
        const response = await fetch(url, mergedOptions);
        
        if (response.status === 401) {
            handleAuthError({ status: 401 });
            throw new Error('Authentication failed');
        }
        
        if (response.status === 403) {
            handleAuthError({ status: 403 });
            throw new Error('Access denied');
        }
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        return response;
    } catch (error) {
        console.error('Network error:', error);
        showAlert('Network error. Please check your connection and try again.', 'danger');
        throw error;
    }
}

