import {options, statusFilter} from "./utils/reportsTable";
import $ from 'jquery';

require("datatables.net")(window, $);
require("datatables.net-dt")(window, $);
require('datatables.net-bs4')(window, $);
require("treetables")(window, $);

$(document).ready(function () {
    const isReviewer = typeof canReview !== "undefined";
    const $table = $('#reports-table');

    $("th input, th select").on("click", (e) => {
        e.stopPropagation();
    });

    $table.treeTable(options(isReviewer, reports));
    const dt = $table.DataTable();

    if (isReviewer) {
        $.fn.dataTable.ext.search.push((settings, data) => {
            const status = $('#status-filter').val();
            return statusFilter(status, data);
        });
    }

    $('#status-filter').change(() => {
        dt.draw();
    });

    $('[data-role=standard-filter]').on('keyup', function () {
        dt.search(this.value).draw();
    });

    $('#expand').on("click", () => {
        $table.data("treeTable")
            .expandAllRows()
            .redraw();
    });

    $('#collapse').on("click", () => {
        $table.data("treeTable")
            .collapseAllRows()
            .redraw();
    })
});
