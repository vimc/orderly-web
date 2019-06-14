import {options} from "./utils/reportsTable";
import $ from 'jquery';

require("datatables.net")(window, $);
require("datatables.net-dt")(window, $);
require('datatables.net-bs4')(window, $);
require("treetables")(window, $);

$(document).ready(function () {
    const isReviewer = typeof canReview !== "undefined";
    const $table = $('#reports-table');
    $table.treeTable(options(isReviewer, reports));

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
