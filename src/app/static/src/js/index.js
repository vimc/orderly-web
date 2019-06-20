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
        // we don't watch the column sort event triggered when filtering
        e.stopPropagation();
    });

    $table.treeTable(options(isReviewer, reports));
    const dt = $table.DataTable();

    if (isReviewer) {
        $.fn.dataTable.ext.search.push((settings, data) => {
            const status = $('#status-filter').val();
            return statusFilter(status, data);
        });

        $('#status-filter').change(() => {
            dt.draw();
        });
    }

    $('[data-role=standard-filter]').on('keyup', function () {
        const col = parseInt($(this).data("col"));
        const value = this.value;

        if (col === 1) {
            // for the name column search this and the invisible display name column
            const displayName = isReviewer ? 6 : 5;
            dt.columns([1, displayName])
                // need an extra call to data() here because of column visibility
                // https://stackoverflow.com/a/49812374/2624366
                .data()
                .search(value)
                .draw();
        }
        else {
            dt.column(col)
                .search(value)
                .draw();
        }

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
