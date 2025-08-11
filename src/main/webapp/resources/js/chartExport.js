/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */


function setReportValues(elementClass) {
    setTimeout(() => {
        let chart = document.querySelector('.' + elementClass + ' canvas');

        let base64 = chart.toDataURL('image/png').replace('data:image/png;base64,', '');

        document.querySelector("." + elementClass + "Hidden").value = base64;
    }, 1000);
}

function chartExtender() {
    var options = {
        plugins: [ChartDataLabels],
        options: {
            plugins: {
                datalabels: {
                    color: 'white',
                    font: {
                        size: 20
                    },
                    formatter: function (value, context) {
                        // Only show non-zero values
                        return value === 0 ? null : value;
                    }
                }
            }
        },
        data: {
            datasets: [{
                    datalabels: {
                        color: 'white'
                    }
                }]
        }
    };

    // Merge all options into the main chart config
    $.extend(true, this.cfg.config, options);
}