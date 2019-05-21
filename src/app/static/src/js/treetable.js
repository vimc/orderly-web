const $ = window.$ = window.jQuery = require("jquery");
require('datatables.net');
require('datatables.net-dt');
require('datatables.net-bs4');
require('jquery-mockjax')(jQuery, window);

$.mockjax({
    url: 'hierarchy.json',
    responseTime: 30,
    responseText: {"data": reports}
});

function compareObjectDesc(a, b) {
    if (a.key !== b.key) {
        return ((a.value < b.value) ? 1 : ((a.value > b.value) ? -1 : 0));
    } else if (typeof a.child === 'undefined' && typeof b.child === 'undefined') {
        return ((a.value < b.value) ? 1 : ((a.value > b.value) ? -1 : 0));
    } else if (typeof a.child !== 'undefined' && typeof b.child !== 'undefined') {
        return compareObjectDesc(a.child, b.child);
    } else {
        return typeof a.child !== 'undefined' ? 1 : -1;
    }
}

function compareObjectAsc(a, b) {
    if (a.key !== b.key) {
        return ((a.value < b.value) ? -1 : ((a.value > b.value) ? 1 : 0));
    } else if (typeof a.child === 'undefined' && typeof b.child === 'undefined') {
        return ((a.value < b.value) ? -1 : ((a.value > b.value) ? 1 : 0));
    } else if (typeof a.child !== 'undefined' && typeof b.child !== 'undefined') {
        return compareObjectAsc(a.child, b.child);
    } else {
        return typeof a.child !== 'undefined' ? 1 : -1;
    }
}

jQuery.fn.dataTableExt.oSort['custom-asc'] = function (a, b) {
    return compareObjectAsc(a, b);
};

jQuery.fn.dataTableExt.oSort['custom-desc'] = function (a, b) {
    return compareObjectDesc(a, b);
};

$(document).ready(function () {

    const dt = $('#reports-table').DataTable({
        "ajax": "hierarchy.json",
        "columns": [{
            "class": "details-control",
            "orderable": false,
            "data": null,
            "defaultContent": "<div class='expander'></div>",
            "width": 50,
            "createdCell": function (td, cellData, rowData, row, col) {
                if (rowData.level > 0) {
                    td.className = td.className + ' level-' + rowData.level;
                }
            }
        }, {
            "data": "level",
            "visible": false
        }, {
            "data": "key",
            "visible": false
        }, {
            "data": "parent",
            "visible": false
        }, {
            "data": "name",
            "type": "custom",
            "render": function (data, type, full, meta) {
                switch (type) {
                    case "display":
                        return data;
                    case "filter":
                        return data;
                    case "sort":
                        return buildOrderObject(dt, full['key'], 'name').child;
                }
                return data ? data : "";
            }
        }, {
            "data": "id",
            "type": "custom",
            "render": function (data, type, full, meta) {

                switch (type) {
                    case "display":
                        return data;
                    case "filter":
                        return data;
                    case "sort":
                        return buildOrderObject(dt, full['key'], 'id').child;
                }
                return data ? data : "";
            },
        }, {
            "data": "published",
            "type": "custom",
            "render": function (data, type, full, meta) {

                switch (type) {
                    case "display":
                        return data;
                    case "filter":
                        return data;
                    case "sort":
                        return buildOrderObject(dt, full['key'], 'published').child;
                }
                return data ? data : "";
            }
        }],
        "order": [
            [4, 'asc']
        ],
        "lengthMenu": [ 10, 25, 50, 75, 100 ],
        "pageLength": 50
    });

    function buildOrderObject(dt, key, column) {
        var rowData = dt.row(key - 1).data();
        if (typeof rowData === 'undefined') {
            return {};
        } else {
            var object = buildOrderObject(dt, rowData['parent'], column);
            var a = object;
            while (typeof a.child !== 'undefined') {
                a = a.child;
            }
            a.child = {};
            a.child.key = rowData['key'];
            a.child.value = rowData[column];
            return object;
        }
    }

    $('#reports-table').on('init.dt', function () {
        dt.columns([3]).search('^(0)$', true, false).draw();
    });

    var displayed = new Set([]);
    $('#reports-table tbody').on('click', 'tr td:first-child', function () {
        var tr = $(this).closest('tr');
        var row = dt.row(tr);
        var key = row.data().key;
        if (displayed.has(key)) {
            displayed.delete(key);
            tr.removeClass('open');
        } else {
            displayed.add(key);
            tr.addClass('open');
        }
        var regex = "^(0";
        displayed.forEach(function (value) {
            regex = regex + "|" + value;
        });
        regex = regex + ")$";
        dt.columns([3]).search(regex, true, false).draw();
    });
});