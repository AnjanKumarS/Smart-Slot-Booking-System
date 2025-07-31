// Firebase Configuration for Smart Slot Booking System
// Replace these values with your actual Firebase project configuration

const firebaseConfig = {
    apiKey: "AIzaSyCpzIil0mnC0_Q67UPN3-1T4GCehLiDzis",
    authDomain: "smartslotbooking-bd296.firebaseapp.com",
    projectId: "smartslotbooking-bd296",
    storageBucket: "smartslotbooking-bd296.firebasestorage.app",
    messagingSenderId: "475179360878",
    appId: "1:475179360878:web:d00dc0be2eea1fcfd93c7f",
    measurementId: "G-RNG8MWH7SY"
};

// Initialize Firebase
firebase.initializeApp(firebaseConfig);

// Initialize Firebase Authentication
const auth = firebase.auth();

// Export for use in other files
window.firebaseAuth = auth; 