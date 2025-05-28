
function toggleSidebar() {

    const sidebar = document.getElementById('sidebar');

    sidebar.classList.toggle('collapsed');
    sidebar.classList.toggle('active');

}
document.addEventListener('DOMContentLoaded', function () {
    const icon = document.getElementById('liveMonitoringIcon');
    // Check if the icon element exists and if the current page's path matches the target URL
    if (icon && window.location.pathname === '/app/Access/liveMonitoring') {
        // Add the 'fa-fade' class to start the animation
        icon.classList.add('fa-fade');
    }
});
