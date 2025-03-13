function toggleSidebar() {
    document.getElementById("sidebar").classList.toggle("active");
}

function toggleDropdown() {
    let dropdown = document.getElementById("dropdownContent");
    dropdown.style.display = dropdown.style.display === "block" ? "none" : "block";
}

// Close the dropdown when clicking outside
document.addEventListener("click", function (event) {
    var dropdown = document.getElementById("dropdownContent");
    var profile = document.querySelector(".profile-dropdown");

    if (!profile.contains(event.target)) {
        dropdown.style.display = "none";
    }
});
