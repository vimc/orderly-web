const $ = window.$ = window.jQuery = require("jquery");
require('datatables.net');
require('datatables.net-dt');
require('datatables.net-bs4');
import treeTable from '../treeTable'

treeTable(jQuery);

$(document).ready(function () {

    function buildIdCell(data, type, full) {
        if (!data) return '';
        if (full["parent"] === 0){
            return '';
        }
        return `<a href="/reports/${full['name']}/${data}/">${data}</a>`;
    }

    function buildStatusCell(data, type, full) {
        if (data == null) {
            return '';
        }
        if (data) {
            return '<span class="badge-published badge float-left">published</span>'
        }
        else {
            return '<span class="badge-internal badge float-left">internal</span>'
        }
    }

    const dt = $('#reports-table').treeTable({
        "data": reports,
        "columns": [{
            "data": "name"
        }, {
            "data": "id",
            "render": buildIdCell
        }, {
            "data": "published",
            "render": buildStatusCell
        }],
        "order": [
            [4, 'asc']
        ],
        "lengthMenu": [10, 25, 50, 75, 100],
        "pageLength": 50
    });

});