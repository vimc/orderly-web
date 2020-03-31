import {initReportTable} from "../js";

const $ = require('jquery');

describe("index page as report reviewer", () => {

    document.body.innerHTML = '<table id="reports-table" class="table dt-responsive display table-striped">\n' +
        '        <thead>\n' +
        '        <tr>\n' +
        '            <th>\n' +
        '                <label for="name-filter">Name</label>\n' +
        '            </th>\n' +
        '            <th>\n' +
        '                <label for="version-filter">Version</label>\n' +
        '            </th>\n' +
        '            <th><label for="status-filter">Status</label>\n' +
        '            </th>\n' +
        '            <th>\n' +
        '            <th>' +
        '               <label for="tags-filter">Tags</label>\n' +
        '            </th>\n' +
        '            <th>' +
        '               <label for="parameter-values-filter">Parameter Values</label>\n' +
        '            </th>\n' +
        '                <label for="author-filter">Author</label>\n' +
        '            </th>\n' +
        '            <th>\n' +
        '                <label for="requester-filter">Requester</label>\n' +
        '            </th>\n' +
        '        </tr>\n' +
        '        <tr>\n' +
        '            <th>\n' +
        '                <input class="form-control" type="text" id="name-filter"/>\n' +
        '            </th>\n' +
        '            <th>\n' +
        '                <input class="form-control" type="text" id="version-filter"\n' +
        '                       data-role="standard-filter"\n' +
        '                       data-col="2"/>\n' +
        '            </th>\n' +
        '            <th>\n' +
        '                 <select id="status-filter" class="form-control">\n' +
        '                     <option value="all">\n' +
        '                         All\n' +
        '                     </option>\n' +
        '                     <option value="published">\n' +
        '                         Published\n' +
        '                     </option>\n' +
        '                     <option value="internal">\n' +
        '                         Internal\n' +
        '                     </option>\n' +
        '                 </select>\n' +
        '            </th>\n' +
        '            <th>\n' +
        '                <select class="form-control" multiple="multiple" id="tags-filter"\n' +
        '                       data-col="3"><option value="test-tag">test-tag</option></select>\n' +
        '            </th>\n' +
        '            <th>\n' +
        '                <input class="form-control" type="text" id="parameter-values-filter"\n' +
        '                       data-role="standard-filter"\n' +
        '                       data-col="5"/>\n' +
        '            </th>\n' +
        '            <th>\n' +
        '                <input class="form-control" type="text" id="author-filter"\n' +
        '                       data-role="standard-filter"\n' +
        '                       data-col="6"/>\n' +
        '            </th>\n' +
        '            <th>\n' +
        '                <input class="form-control" type="text" id="requester-filter"\n' +
        '                       data-role="standard-filter"\n' +
        '                       data-col="7"/>\n' +
        '            </th>\n' +
        '        </tr>\n' +
        '        </thead>\n' +
        '    </table>';

    const reports = [{
        tt_key: 1,
        tt_parent: 0,
        name: "r1",
        display_name: "r1 display",
        id: "20181112-152443-4de0c811",
        date: "Mon Nov 12 2018",
        latest_version: "20181112-152443-4de0c811",
        author: "author",
        requester: "requester",
        published: true,
        parameter_values: "p1=v1",
        tags: ["tag1"]
    }];

    initReportTable(true, reports, ["author", "requester"]);

    it("wires up name filter", () => {

        const $table = $('#reports-table');
        const $filter = $('#name-filter');
        expect($($table.find("tbody tr td")[1]).find("span")[0].innerHTML).toBe("r1 display");

        $filter.val("author");
        $filter.keyup();

        expect($table.find("tbody tr td")[0].innerHTML).toBe("No matching records found");

        $filter.val("r1");
        $filter.keyup();

        expect($($table.find("tbody tr td")[1]).find("span")[0].innerHTML).toBe("r1 display");

        $filter.val("display");
        $filter.keyup();

        expect($($table.find("tbody tr td")[1]).find("span")[0].innerHTML).toBe("r1 display");
    });

    it("wires up version filter", () => {

        const $table = $('#reports-table');
        const $filter = $('#version-filter');
        expect($($table.find("tbody tr td")[1]).find("span")[0].innerHTML).toBe("r1 display");

        $filter.val("2019");
        $filter.keyup();

        expect($table.find("tbody tr td")[0].innerHTML).toBe("No matching records found");

        $filter.val("2018");
        $filter.keyup();

        expect($($table.find("tbody tr td")[1]).find("span")[0].innerHTML).toBe("r1 display");

    });

    it("wires up tag filter", () => {

        const $table = $('#reports-table');
        const $filter = $('#tags-filter');
        expect($($table.find("tbody tr td")[1]).find("span")[0].innerHTML).toBe("r1 display");

        $filter.val("r1");
        $filter.keyup();

        expect($table.find("tbody tr td")[0].innerHTML).toBe("No matching records found");

        $filter.val("tag1");
        $filter.keyup();

        expect($($table.find("tbody tr td")[1]).find("span")[0].innerHTML).toBe("r1 display");
    });

    it("wires up parameter values filter", () => {

        const $table = $('#reports-table');
        const $filter = $('#parameter-values-filter');
        expect($($table.find("tbody tr td")[1]).find("span")[0].innerHTML).toBe("r1 display");

        $filter.val("r1");
        $filter.keyup();

        expect($table.find("tbody tr td")[0].innerHTML).toBe("No matching records found");

        $filter.val("p1");
        $filter.keyup();

        expect($($table.find("tbody tr td")[1]).find("span")[0].innerHTML).toBe("r1 display");
    });


    it("wires up author filter", () => {

        const $table = $('#reports-table');
        const $filter = $('#author-filter');
        expect($($table.find("tbody tr td")[1]).find("span")[0].innerHTML).toBe("r1 display");

        $filter.val("r1");
        $filter.keyup();

        expect($table.find("tbody tr td")[0].innerHTML).toBe("No matching records found");

        $filter.val("author");
        $filter.keyup();

        expect($($table.find("tbody tr td")[1]).find("span")[0].innerHTML).toBe("r1 display");
    });

    it("wires up requester filter", () => {

        const $table = $('#reports-table');
        const $filter = $('#requester-filter');
        expect($($table.find("tbody tr td")[1]).find("span")[0].innerHTML).toBe("r1 display");

        $filter.val("r1");
        $filter.keyup();

        expect($table.find("tbody tr td")[0].innerHTML).toBe("No matching records found");

        $filter.val("requester");
        $filter.keyup();

        expect($($table.find("tbody tr td")[1]).find("span")[0].innerHTML).toBe("r1 display");
    });

    it("wires up status filter", () => {

        expect($.fn.dataTable.ext.search.length).toBe(2);
        const $table = $('#reports-table');
        const $filter = $('#status-filter');
        expect($($table.find("tbody tr td")[1]).find("span")[0].innerHTML).toBe("r1 display");

        $filter.val("internal").change();

        expect($table.find("tbody tr td")[0].innerHTML).toBe("No matching records found");

        $filter.val("all").change();

        expect($($table.find("tbody tr td")[1]).find("span")[0].innerHTML).toBe("r1 display");
    });

});