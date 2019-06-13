import {options} from "./utils/reportsTable";
import $ from 'jquery';

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
