/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */


function scrollToSelectedRow() {
    // Add a longer delay to ensure table is fully updated
    setTimeout(() => {
        const table = PF('roleTable');
        const selectedRowIndexElement = document.getElementById('roleForm:selectedRowIndex');

        if (!selectedRowIndexElement || !selectedRowIndexElement.value) {
            return; // No selected row
        }

        const rowIndex = parseInt(selectedRowIndexElement.value);

        // Get rows per page from different possible sources
        let rowsPerPage = 10; // Default fallback from your XHTML

        if (table.cfg && table.cfg.rows) {
            rowsPerPage = table.cfg.rows;
        } else if (table.cfg && table.cfg.paginator && table.cfg.paginator.rows) {
            rowsPerPage = table.cfg.paginator.rows;
        } else if (table.getPaginator && table.getPaginator().getRows) {
            rowsPerPage = table.getPaginator().getRows();
        }

        const page = Math.floor(rowIndex / rowsPerPage);
        const currentPage = table.getPaginator().getCurrentPage();


        // Move to the correct page if we're not already there
        if (currentPage !== page) {
            console.log('Navigating to page:', page);
            table.getPaginator().setPage(page);
        }

        // After the page is set, scroll to the row
        setTimeout(() => {
            const row = document.querySelector('.scroll-target');
            if (row) {
                console.log('Scrolling to row');
                row.scrollIntoView({behavior: 'smooth', block: 'center'});
            } else {
                console.log('No scroll-target row found');
            }
        }, 500); // Increased delay for pagination to complete
    }, 200); // Increased initial delay for table update
}