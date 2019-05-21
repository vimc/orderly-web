const $ = window.$ = window.jQuery = require("jquery");
const dt = require('datatables.net');
require('datatables-treegrid');
require('datatables.net-dt');
require('datatables.net-bs4');

jQuery.extend(jQuery.fn.dataTableExt.oSort, {
    "id-sort-asc": function (a, b) {
        const parentA = $(a).data("name");
        const parentB = $(b).data("name");

        a = $(a).text();
        b = $(b).text();

        const parentSort = parentA < parentB ? -1 : parentB > parentA ? 1 : 0;

        if (parentSort === 0) {
            if (!a) return -1;
            if (!b) return 1;
            return ((a < b) ? -1 : ((a > b) ? 1 : 0));
        }
        else return parentSort
    },
    "id-sort-desc": function (a, b) {
        const parentA = $(a).data("name");
        const parentB = $(b).data("name");

        a = $(a).text();
        b = $(b).text();

        const parentSort = parentA < parentB ? 1 : parentB > parentA ? -1 : 0;

        if (parentSort === 0) {
            if (!a) return -1;
            if (!b) return 1;
            return ((a < b) ? 1 : ((a > b) ? -1 : 0));
        }
        else return parentSort
    }
});

const rowData = function (item) {
    return `<span data-name="${item.name}">${item.id ? item.id : ''}</span>`
};

$(document).ready(function () {
    const table = $('#reports-table').DataTable({
        data: reports,
        "orderFixed": [1, 'asc'],
        "columnDefs": [{
            "targets": 0,
            "orderable": false
        }],
        "columns": [
            {
                title: '',
                target: 0,
                className: 'treegrid-control',
                data: function (item) {
                    if (item.children) {
                        return '<span class="expander">•</span>';
                    }
                    return '';
                }
            },
            {"data": "name"},
            {"data": rowData, "sType": "id-sort"},
            {"data": "published"},
            {"data": "author"},
            {"data": "requester"}
        ],
        'treeGrid': {
            'left': 10,
            'expandIcon': '<span class="expander">•</span>',
            'collapseIcon': '<span class="expander open">•</span>'
        },
        order: [[1, 'asc']]
    });
});
