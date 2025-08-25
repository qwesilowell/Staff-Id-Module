function scrollTableToRightSmooth() {
    var container = document.querySelector('.table-container');
    if (container) {
// Try scrolling to a specific large value for testing
        container.scrollTo({
            left: 2000, // Fixed large value for testing
            behavior: 'smooth'
        });
    }
}
