document.addEventListener('DOMContentLoaded', function() {
    // Toast setup
    let toastContainer = document.getElementById('toast-container');
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.id = 'toast-container';
        toastContainer.style.position = 'fixed';
        toastContainer.style.top = '1rem';
        toastContainer.style.right = '1rem';
        toastContainer.style.zIndex = 9999;
        document.body.appendChild(toastContainer);
    }
    function showToast(message, success = true) {
        const toast = document.createElement('div');
        toast.className = `toast align-items-center text-bg-${success ? 'success' : 'danger'} border-0 show mb-2`;
        toast.role = 'alert';
        toast.innerHTML = `<div class="d-flex"><div class="toast-body">${message}</div><button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button></div>`;
        toastContainer.appendChild(toast);
        setTimeout(() => toast.remove(), 3000);
    }
    // Approve button handler
    document.querySelectorAll('.approve-btn').forEach(btn => {
        btn.addEventListener('click', async function() {
            const bookingId = this.getAttribute('data-id');
            const row = document.getElementById('booking-row-' + bookingId);
            try {
                const resp = await fetch(`/api/bookings/${bookingId}/approve`, { method: 'POST' });
                const data = await resp.json();
                if (data.success) {
                    showToast('Booking approved!', true);
                    if (row) row.remove();
                } else {
                    showToast(data.error || 'Approval failed', false);
                }
            } catch (e) {
                showToast('Approval failed', false);
            }
        });
    });
    // Reject button handler
    document.querySelectorAll('.reject-btn').forEach(btn => {
        btn.addEventListener('click', async function() {
            const bookingId = this.getAttribute('data-id');
            const row = document.getElementById('booking-row-' + bookingId);
            try {
                const resp = await fetch(`/api/bookings/${bookingId}/reject`, { method: 'POST' });
                const data = await resp.json();
                if (data.success) {
                    showToast('Booking rejected!', true);
                    if (row) row.remove();
                } else {
                    showToast(data.error || 'Rejection failed', false);
                }
            } catch (e) {
                showToast('Rejection failed', false);
            }
        });
    });
}); 