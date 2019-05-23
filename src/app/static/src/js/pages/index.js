const $ = window.$ = window.jQuery = require("jquery");
require('datatables.net');
require('datatables.net-dt');
require('datatables.net-bs4');
require("@reside-ic/tree-table");

$(document).ready(function () {

    function buildIdCell(data, type, full) {
        if (!data) return '';
        if (full["tt_parent"] === 0) {
            return '';
        }
        return `<a href="/reports/${full['name']}/${data}/">${data}</a>`;
    }

    function buildNameCell(data, type, full) {
        if (!data) return '';
        if (full["tt_parent"] > 0) {
            return '';
        }
        const versionText = full.num_versions > 1 ? "versions" : "versions";
        return `<span>${data}</span><br/>
<span class="text-muted">${full.num_versions} ${versionText}: </span>
<a href="/reports/${full['name']}/${full.id}/">view latest</a>`;
    }

    function buildStatusCell(data) {
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
        "collapsed": true,
        "columns": [
            {
                "data": "name",
                "render": buildNameCell
            },
            {
                "data": "id",
                "render": buildIdCell
            },
            {
                "data": "published",
                "render": buildStatusCell
            },
            {
                "data": "author"
            },
            {
                "data": "requester"
            }
        ],
        "order": [
            [1, 'asc']
        ],
        "lengthMenu": [10, 25, 50, 75, 100],
        "pageLength": 50
    });

});