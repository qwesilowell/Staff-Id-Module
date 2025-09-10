/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */

Chart.register(ChartDataLabels);

function chartExtender() {
    var options = {
        plugins: {
            datalabels: {
                display: true,
                color: 'white',
                font: {
                    weight: 'bold',
                    size: 17
                },
                formatter: function (value, context) {
                    // Show value or percentage
                    return value;
                },
                anchor: 'end',
                align: 'top'
            }
        }
    };

    //merge all options into the main chart options
    $.extend(true, this.cfg.config, options);
}

// Pie chart extender with percentage labels
function pieChartExtender() {
    var options = {
        plugins: {
            datalabels: {
                display: true,
                color: 'white',
                font: {
                    weight: 'bold',
                    size: 14
                },
                formatter: function (value, context) {
                    // Calculate percentage
                    var total = context.dataset.data.reduce((a, b) => a + b, 0);
                    var percentage = ((value / total) * 100).toFixed(1);
                    return percentage + '%';
                },
                anchor: 'center',
                align: 'center'
            }
        }
    };

    $.extend(true, this.cfg.config, options);
}

// Bar chart extender with value labels
function barChartExtender() {
    var options = {
        plugins: {
            datalabels: {
                display: true,
                color: 'white',
                font: {
                    weight: 'bold',
                    size: 11
                },
                formatter: function (value, context) {
                    return value.toLocaleString();
                },
                anchor: 'end',
                align: 'top'
            }
        }
    };

    $.extend(true, this.cfg.config, options);
}

 