import {options} from "./utils/reportsTable";

const $ = window.$ = window.jQuery = require("jquery");
require('datatables.net');
require('datatables.net-dt');
require('datatables.net-bs4');
require("@reside-ic/tree-table");

$(document).ready(function () {
    $('#reports-table').treeTable(options);
});
