const $ = require('jquery');
const dt = require('datatables.net')(window, $);
require('datatables.net-bs4')(window, $);

$(document).ready(function () {
    $('#reports').DataTable();
});