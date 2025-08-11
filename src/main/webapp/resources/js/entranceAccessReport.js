/* 
 * Scroll Indicators for DataTable
 * Provides visual indicators for horizontal scrolling in PrimeFaces DataTables
 */

function initScrollIndicators() {
    var scrollableBody = document.querySelector('#resultsTable .ui-datatable-scrollable-body');
    var leftIndicator = document.getElementById('scrollLeft');
    var rightIndicator = document.getElementById('scrollRight');

    if (!scrollableBody || !leftIndicator || !rightIndicator) {
        return;
    }

    function updateIndicators() {
        var scrollLeft = scrollableBody.scrollLeft;
        var scrollWidth = scrollableBody.scrollWidth;
        var clientWidth = scrollableBody.clientWidth;

        // Show/hide left indicator
        if (scrollLeft > 0) {
            leftIndicator.style.display = 'block';
        } else {
            leftIndicator.style.display = 'none';
        }

        // Show/hide right indicator
        if (scrollLeft + clientWidth < scrollWidth - 1) {
            rightIndicator.style.display = 'block';
        } else {
            rightIndicator.style.display = 'none';
        }
    }

    // Initial check
    updateIndicators();

    // Listen for scroll events
    scrollableBody.addEventListener('scroll', updateIndicators);

    // Click handlers for indicators
    leftIndicator.addEventListener('click', function () {
        scrollableBody.scrollLeft -= 200;
    });

    rightIndicator.addEventListener('click', function () {
        scrollableBody.scrollLeft += 200;
    });
}

// Initialize when page loads
document.addEventListener('DOMContentLoaded', function () {
    setTimeout(initScrollIndicators, 100);
});

// Re-initialize after AJAX updates
if (typeof PrimeFaces !== 'undefined') {
    PrimeFaces.ajax.addOnCompleteCallback(function () {
        setTimeout(initScrollIndicators, 100);
    });
}