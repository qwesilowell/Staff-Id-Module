/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */


let currentlyExpanded = null;

function toggleCardExpansion(cardId) {
    const mainCard = document.querySelector(`[data-card-id="${cardId}"]`);
    const subCards = document.querySelectorAll(`[data-parent="${cardId}"]`);
    const isCurrentlyExpanded = mainCard.classList.contains('expanded');

    // Close currently expanded card if different from clicked one
    if (currentlyExpanded && currentlyExpanded !== cardId) {
        closeCard(currentlyExpanded);
    }

    if (isCurrentlyExpanded) {
        // Close this card
        closeCard(cardId);
        currentlyExpanded = null;
    } else {
        // Open this card
        openCard(cardId);
        currentlyExpanded = cardId;
    }
}

function openCard(cardId) {
    const mainCard = document.querySelector(`[data-card-id="${cardId}"]`);
    const subCards = document.querySelectorAll(`[data-parent="${cardId}"]`);

    mainCard.classList.add('expanded');

    // Show sub-cards with staggered animation
    subCards.forEach((subCard, index) => {
        setTimeout(() => {
            subCard.classList.add('show');
        }, index * 100);
    });

    // Smooth scroll to keep the expanded section in view
    setTimeout(() => {
        mainCard.scrollIntoView({
            behavior: 'smooth',
            block: 'start'
        });
    }, 300);
}

function closeCard(cardId) {
    const mainCard = document.querySelector(`[data-card-id="${cardId}"]`);
    const subCards = document.querySelectorAll(`[data-parent="${cardId}"]`);

    mainCard.classList.remove('expanded');

    // Hide sub-cards
    subCards.forEach((subCard) => {
        subCard.classList.remove('show');
    });
}

// Initialize page
document.addEventListener('DOMContentLoaded', function () {
    console.log('Expandable cards system initialized');
});

// Legacy function for compatibility
function toggleCardDropdown(header) {
    // This function is kept for backward compatibility
    // but the new system uses toggleCardExpansion instead
}