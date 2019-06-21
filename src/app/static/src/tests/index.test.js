const $ = require('jquery');

describe("index page", () => {

    document.body.innerHTML = '<table id="reports-table" class="table dt-responsive display table-striped">\n' +
        '        <thead>\n' +
        '        <tr>\n' +
        '            <th>\n' +
        '                <label for="name-filter">Name</label>\n' +
        '\n' +
        '            </th>\n' +
        '            <th>\n' +
        '                <label for="version-filter">Version</label>\n' +
        '\n' +
        '            </th>\n' +
        '            <#if isReviewer>\n' +
        '                <th><label for="status-filter">Status</label>\n' +
        '\n' +
        '                </th>\n' +
        '            </#if>\n' +
        '            <th>\n' +
        '                <label for="author-filter">Author</label>\n' +
        '\n' +
        '            </th>\n' +
        '            <th>\n' +
        '                <label for="requester-filter">Requester</label>\n' +
        '\n' +
        '            </th>\n' +
        '        </tr>\n' +
        '        <tr>\n' +
        '            <th>\n' +
        '                <input class="form-control" type="text" id="name-filter"/>\n' +
        '            </th>\n' +
        '            <th>\n' +
        '\n' +
        '                <input class="form-control" type="text" id="version-filter"\n' +
        '                       data-role="standard-filter"\n' +
        '                       data-col="2"/>\n' +
        '            </th>\n' +
        '            <#if isReviewer>\n' +
        '                <th>\n' +
        '                    <select id="status-filter" class="form-control">\n' +
        '                        <option value="all">\n' +
        '                            All\n' +
        '                        </option>\n' +
        '                        <option value="published">\n' +
        '                            Published\n' +
        '                        </option>\n' +
        '                        <option value="internal">\n' +
        '                            Internal\n' +
        '                        </option>\n' +
        '                    </select>\n' +
        '                </th>\n' +
        '            </#if>\n' +
        '            <th>\n' +
        '                <input class="form-control" type="text" id="author-filter"\n' +
        '                       data-role="standard-filter"\n' +
        '                       data-col=""/>\n' +
        '            </th>\n' +
        '            <th>\n' +
        '                <input class="form-control" type="text" id="requester-filter"\n' +
        '                       data-role="standard-filter"\n' +
        '                       data-col="5"/>\n' +
        '            </th>\n' +
        '        </tr>\n' +
        '        </thead>\n' +
        '    </table>';

    require("../js/index");

    it("wires up standard filters", () => {

    });

});