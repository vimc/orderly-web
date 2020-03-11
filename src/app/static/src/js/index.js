import {nameFilter, options, statusFilter} from "./utils/reportsTable";
import $ from 'jquery';

require("datatables.net")(window, $);
require("datatables.net-dt")(window, $);
require('datatables.net-bs4')(window, $);
require("treetables")(window, $);

export const initReportTable = (isReviewer, reports, customFields) => {

    const $table = $('#reports-table');

    $table.treeTable(options(isReviewer, reports, customFields));
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

    $.fn.dataTable.ext.search.push((settings, data) => {
        const displayNameReviewerIdx = customFields.length + 6;
        const displayName = isReviewer ? displayNameReviewerIdx : displayNameReviewerIdx-1;
        const value = $('#name-filter').val();
        return nameFilter(displayName, value, data);
    });

    $('#name-filter').on('keyup', () => {
        dt.draw();
    });

    $('[data-role=standard-filter]').on('keyup', function () {
        const col = parseInt($(this).data("col"));

        dt.column(col)
            .search(this.value)
            .draw();
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
};

$(document).ready(() => {
    const isReviewer = typeof canReview !== "undefined";
    initReportTable(isReviewer, reports, customFields)
});
