import {api} from "./api";

function buildBasicCell(data, type, full) {
    if (full["tt_parent"] === 0) {
        return '';
    }
    return data
}

function buildVersionBadge(data, full) {
    if (data === full["latest_version"]) {
        return '<span class="badge-info badge float-right">latest</span>'
    }
    else {
        return '<span class="badge-light badge float-right">out-dated</span>'
    }
}

function buildIdCell(data, type, full) {
    if (full["tt_parent"] === 0) {
        return '';
    }
    return `<a href="${api.baseUrl}/report/${full["name"]}/${data}/">
                <div>
                <span>${full["date"]}</span>
                ${buildVersionBadge(data, full)}
                </div>
                <div class="small">(${data})</div>
            </a>`;
}

function buildNameCell(data, type, full) {
    if (full["tt_parent"] > 0) {
        return '';
    }
    const versionText = full["num_versions"] > 1 ? "versions" : "version";
    return `<div>
                <span>${full["display_name"]}</span><br/>
                <span class="text-muted">${full["num_versions"]} ${versionText}: </span>                
                <a href="${api.baseUrl}/report/${full['name']}/${full["latest_version"]}/">view latest</a>
            </div>`;
}

function buildStatusCell(data, type, full) {

    if (full["tt_parent"] === 0) {
        return '';
    }

    if (data) {
        return '<span class="badge-published badge float-left">published</span>'
    }
    else {
        return '<span class="badge-internal badge float-left">internal</span>'
    }
}

function buildTagsCell(data) {
    const result = [];
    for (const tag of data) {
        result.push(`<span class="badge-primary badge float-left mr-1">${tag}</span>`)
    }
    return result.join('');
}

export const options = (isReviewer, reports, customFields) => {

    let cols = [
        {
            "data": "name",
            "render": buildNameCell
        },
        {
            "data": "id",
            "render": buildIdCell
        }];
    if (isReviewer) {
        cols.push({
            "data": "published",
            "render": buildStatusCell
        })
    }

    cols.push({
        "data": "tags",
        "render": buildTagsCell,
        "orderable": false
    });

    cols.push({
        "data": "parameter_values",
        "render": buildBasicCell,
        "orderable": false
    });

    for (const customField of customFields) {
        cols.push({
            "data": customField,
            "render": buildBasicCell,
            "orderable": false
        })
    }

    cols = cols.concat([
        {
            "data": "display_name",
            "visible": false,
            "searchable": true
        }]);



    return {
        "dom": '<"top">rt<"bottom"lp><"clear">',
        "bSortCellsTop": true,
        "data": reports,
        "collapsed": true,
        "columns": cols,
        "order": [
            [2, 'desc']
        ],
        "lengthMenu": [10, 25, 50, 75, 100],
        "pageLength": 50,
        "autoWidth": false
    }
};

export const statusFilter = (selectedStatus, data) => {
    switch (selectedStatus) {
        case "published":
            const reTrue = new RegExp("true");
            return reTrue.test(data[3]);
        case "internal":
            const reFalse = new RegExp("false");
            return reFalse.test(data[3]);
        case "all":
        default:
            return true
    }
};

export const nameFilter = (displayNameCol, value, data) => {
    const re = new RegExp(value, "i");
    return re.test(data[1]) || re.test(data[displayNameCol]);
};
