import {options} from "./utils/reportsTable";

const $ = window.$ = window.jQuery = require("jquery");
require('datatables.net');
require('datatables.net-dt');
require('datatables.net-bs4');
require("@reside-ic/tree-table");

$(document).ready(function () {
    const isReviewer = typeof canReview !== "undefined";
    const $table = $('#reports-table');
    $table.treeTable(options(isReviewer, reports));

    $('#expand').on("click", () => {
        $table.data("treeTable")
            .expandAllRows()
            .redraw()    ;
    });

    $('#collapse').on("click", () => {
        $table.data("treeTable")
            .collapseAllRows()
            .redraw()    ;
    })
});
