const $ = window.$ = window.jQuery = require("jquery");
const dt = require('datatables.net');
require('datatables.net-bs4');

$(document).ready(function () {
    $('#reports').DataTable();
});