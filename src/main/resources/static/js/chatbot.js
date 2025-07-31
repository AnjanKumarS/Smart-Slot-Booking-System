// Chatbot JavaScript for Smart Slot Booking System

// Global variables
let currentUser = null;
let chatHistory = [];

// API Base URL
const API_BASE = '/api';

// Initialize chatbot
document.addEventListener('DOMContentLoaded', function() {
    initializeChatbot();
});

// Clear chat when user logs in/out
function clearChat() {
    const messagesContainer = document.getElementById('chat-messages');
    if (messagesContainer) {
        messagesContainer.innerHTML = '';
    }
    chatHistory = [];
    localStorage.removeItem('chatHistory');
}

// Check if user has changed (login/logout)
function checkUserChange() {
    const currentToken = localStorage.getItem('authToken');
    const storedToken = sessionStorage.getItem('lastAuthToken');
    
    if (currentToken !== storedToken) {
        // User has logged in or out
        clearChat();
        sessionStorage.setItem('lastAuthToken', currentToken);
    }
}

function initializeChatbot() {
    // Check authentication
    checkAuthStatus();
    
    // Check for user changes
    checkUserChange();
    
    // Set up event listeners
    setupEventListeners();
    
    // Show welcome message
    showWelcomeMessage();
}

function setupEventListeners() {
    // Chat form
    const chatForm = document.getElementById('chat-form');
    if (chatForm) {
        chatForm.addEventListener('submit', handleMessageSubmit);
    }
    
    // Message input
    const messageInput = document.getElementById('message-input');
    if (messageInput) {
        messageInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                handleMessageSubmit(e);
            }
        });
    }
    
    // Listen for logout events
    window.addEventListener('storage', function(e) {
        if (e.key === 'authToken' && !e.newValue) {
            // User logged out
            clearChat();
        }
    });
    
    // Listen for logout button clicks
    document.addEventListener('click', function(e) {
        if (e.target && e.target.textContent.toLowerCase().includes('logout')) {
            clearChat();
        }
    });
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

function showWelcomeMessage() {
    const welcomeMessage = `
        Hello! I'm your Smart Slot Assistant. I can help you with:
        
        • Booking venues
        • Checking booking status
        • Managing your account
        • General questions about the system
        
        How can I help you today?
    `;
    
    addBotMessage(welcomeMessage);
}

async function handleMessageSubmit(e) {
    e.preventDefault();
    
    const messageInput = document.getElementById('message-input');
    const message = messageInput.value.trim();
    
    if (!message) return;
    
    // Add user message
    addUserMessage(message);
    
    // Clear input
    messageInput.value = '';
    
    // Show typing indicator
    showTypingIndicator();
    
    try {
        // Send message to backend
        const response = await fetch(`${API_BASE}/chatbot`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            },
            body: JSON.stringify({
                message: message,
                userId: currentUser?.uid
            })
        });
        
        const data = await response.json();
        
        // Hide typing indicator
        hideTypingIndicator();
        
        if (data.success) {
            addBotMessage(data.response);
        } else {
            // Fallback to local responses if API fails
            const fallbackResponse = await getFallbackResponse(message);
            addBotMessage(fallbackResponse);
        }
        
    } catch (error) {
        console.error('Error sending message:', error);
        hideTypingIndicator();
        
        // Fallback to local responses
        const fallbackResponse = await getFallbackResponse(message);
        addBotMessage(fallbackResponse);
    }
}

function addUserMessage(message) {
    const messagesContainer = document.getElementById('chat-messages');
    const messageDiv = document.createElement('div');
    messageDiv.className = 'message user';
    messageDiv.innerHTML = `
        <div class="message-content">
            ${escapeHtml(message)}
        </div>
    `;
    
    messagesContainer.appendChild(messageDiv);
    scrollToBottom();
}

function addBotMessage(message) {
    const messagesContainer = document.getElementById('chat-messages');
    const messageDiv = document.createElement('div');
    messageDiv.className = 'message bot';
    messageDiv.innerHTML = `
        <div class="message-content">
            ${formatBotMessage(message)}
        </div>
    `;
    
    messagesContainer.appendChild(messageDiv);
    scrollToBottom();
}

function formatBotMessage(message) {
    // Convert line breaks to <br> tags
    return message.replace(/\n/g, '<br>');
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function showTypingIndicator() {
    const indicator = document.getElementById('typing-indicator');
    if (indicator) {
        indicator.style.display = 'block';
        scrollToBottom();
    }
}

function hideTypingIndicator() {
    const indicator = document.getElementById('typing-indicator');
    if (indicator) {
        indicator.style.display = 'none';
    }
}

function scrollToBottom() {
    const messagesContainer = document.getElementById('chat-messages');
    if (messagesContainer) {
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }
}

async function getFallbackResponse(message) {
    const lowerMessage = message.toLowerCase();
    
    // Booking related responses
    if (lowerMessage.includes('book') || lowerMessage.includes('booking')) {
        return `To book a venue, you can:
        
1. Go to the Dashboard and click "Book a Venue"
2. Select a venue from the available options
3. Choose your date and time
4. Fill in the event details
5. Submit your booking request

Your booking will be reviewed and you'll receive a confirmation once approved.`;
    }
    
    // Venue related responses
    if (lowerMessage.includes('venue') || lowerMessage.includes('venues')) {
        try {
            const response = await fetch('/api/venues');
            const venues = await response.json();
            
            if (venues && venues.length > 0) {
                let venueList = `Here are our available venues:\n\n`;
                venues.forEach(venue => {
                    venueList += `🏢 **${venue.name}**\n`;
                    venueList += `   • Capacity: ${venue.capacity} people\n`;
                    venueList += `   • Status: Available\n\n`;
                });
                venueList += `You can view more details and book any venue from the Dashboard.`;
                return venueList;
            }
        } catch (error) {
            console.error('Error fetching venues:', error);
        }
        
        return `We have various venues available including:
        
• Conference Rooms (50 people)
• Auditorium (200 people)
• Meeting Rooms (20 people)
• Seminar Halls (100 people)

Each venue has different capacities and amenities. You can view all available venues on the Dashboard.`;
    }
    
    // My bookings related responses
    if (lowerMessage.includes('my booking') || lowerMessage.includes('my bookings') || lowerMessage.includes('booked')) {
        try {
            const response = await fetch('/api/bookings', {
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`
                }
            });
            const bookings = await response.json();
            
            if (bookings && bookings.length > 0) {
                let bookingList = `Here are your current bookings:\n\n`;
                bookings.slice(0, 5).forEach(booking => {
                    const status = booking.status || 'PENDING';
                    const statusIcon = status === 'CONFIRMED' ? '✅' : status === 'REJECTED' ? '❌' : '⏳';
                    bookingList += `${statusIcon} **${booking.venueName || 'Venue'}**\n`;
                    bookingList += `   • Date: ${new Date(booking.date).toLocaleDateString()}\n`;
                    bookingList += `   • Time: ${booking.startTime} - ${booking.endTime}\n`;
                    bookingList += `   • Status: ${status}\n\n`;
                });
                
                if (bookings.length > 5) {
                    bookingList += `... and ${bookings.length - 5} more bookings.\n\n`;
                }
                
                bookingList += `View all your bookings in the "My Bookings" section.`;
                return bookingList;
            } else {
                return `You don't have any bookings yet. Would you like to book a venue? You can start by visiting the Dashboard and clicking "Book a Venue".`;
            }
        } catch (error) {
            console.error('Error fetching bookings:', error);
        }
        
        return `To check your booking status:
        
1. Go to "My Bookings" in the navigation
2. You'll see all your bookings with their current status
3. Bookings can be: Pending, Confirmed, or Rejected

You can also cancel pending bookings from the same page.`;
    }
    
    // Status related responses
    if (lowerMessage.includes('status') || lowerMessage.includes('check')) {
        return `To check your booking status:
        
1. Go to "My Bookings" in the navigation
2. You'll see all your bookings with their current status
3. Bookings can be: Pending, Confirmed, or Rejected

You can also cancel pending bookings from the same page.`;
    }
    
    // Help related responses
    if (lowerMessage.includes('help') || lowerMessage.includes('support')) {
        return `I'm here to help! You can ask me about:
        
• How to book venues
• Checking booking status
• Available venues and their details
• Your current bookings
• Account management
• General system questions

Try asking me:
- "What venues are available?"
- "Show me my bookings"
- "How do I book a venue?"
- "What's the status of my bookings?"

If you need more specific help, please let me know what you're looking for.`;
    }
    
    // Default response
    return `I understand you're asking about "${message}". 

For the most accurate information, I recommend:
• Checking the Dashboard for venue availability
• Using "My Bookings" to see your current bookings
• Contacting support if you need immediate assistance

Is there something specific about booking venues that I can help you with?`;
}

// Global function for quick action buttons
window.sendQuickMessage = function(message) {
    const messageInput = document.getElementById('message-input');
    if (messageInput) {
        messageInput.value = message;
        const event = new Event('submit', { bubbles: true });
        document.getElementById('chat-form').dispatchEvent(event);
    }
};

