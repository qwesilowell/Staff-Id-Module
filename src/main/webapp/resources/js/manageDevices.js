/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */


function setPositionUI(position) {
    // Visual feedback for position toggle
    document.querySelectorAll('.position-toggle .position').forEach(opt => {
        opt.classList.remove('active');
    });
    document.querySelector(`.position-option.${position}`).classList.add('active');
}

function resetPositionUI() {
    // Clear active class from all position options
    document.querySelectorAll('.position-toggle .position').forEach(opt => {
        opt.classList.remove('active');
    });
}

// Animation on page load
document.addEventListener('DOMContentLoaded', function () {
    const cards = document.querySelectorAll('.device-card');
    cards.forEach((card, index) => {
        card.style.animationDelay = `${index * 0.05}s`;
        card.style.animation = 'fadeInUp 0.4s ease forwards';
    });
});

// Add CSS animation
const style = document.createElement('style');
style.textContent = `
           @keyframes fadeInUp {
               from {
                   opacity: 0;
                   transform: translateY(15px);
               }
               to {
                   opacity: 1;
                   transform: translateY(0);
               }
           }
  .content-area.expanded {
                            max-height: none;
                        }
                        
                        .devices-section {
                        }
                        
                        .devices-grid {
                            overflow-y: auto;
                            padding-right: 10px;
                        }
                        
           .search-filter-section {
               background: var(--card-bg, #fff);
               border-radius: 12px;
               padding: 20px;
               margin-bottom: 20px;
               box-shadow: 0 2px 8px rgba(0,0,0,0.1);
           }
                        
           .search-filter-grid {
               display: grid;
               grid-template-columns: 2fr 1fr 1fr auto;
               gap: 20px;
               align-items: end;
           }
                        
           .search-input {
               position: relative;
           }
                        
           .search-input::before {
               content: '🔍';
               position: absolute;
               left: 12px;
               top: 50%;
               transform: translateY(-50%);
               color: #666;
           }
                        
           .filter-results-info {
               display: flex;
               justify-content: space-between;
               align-items: center;
               margin-bottom: 15px;
               padding: 0 5px;
           }
                        
           .results-count {
               color: #666;
               font-size: 14px;
           }
                        
           .active-filters {
               color: #007bff;
               font-size: 14px;
               font-weight: 500;
           }
                        
           @media (max-width: 768px) {
               .search-filter-grid {
                   grid-template-columns: 1fr;
                   gap: 15px;
               }
                            
               .filter-results-info {
                   flex-direction: column;
                   align-items: flex-start;
                   gap: 5px;
               }
           }
       `;
document.head.appendChild(style);

function filterDevices(type) {
    // Remove active class from all cards
    document.querySelectorAll('.stat-card').forEach(card => {
        card.classList.remove('active');
    });

    // Add active class to clicked card
    event.currentTarget.classList.add('active');

    // Update filter indicator
    const filterText = document.querySelector('.filter-text');
    const filterLabels = {
        'all': 'All Devices',
        'entry': 'Entry Devices',
        'exit': 'Exit Devices',
        'unassigned': 'Unassigned Devices'
    };

    if (filterText) {
        filterText.innerHTML = `<i class="fas fa-filter"></i> Showing: ${filterLabels[type]}`;
    }

    // Here you would call your JSF method
    console.log('Filtering by:', type);
}
