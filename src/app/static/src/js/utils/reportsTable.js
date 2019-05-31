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
    return `<a href="/reports/${full["name"]}/${data}/">
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
                <a href="/reports/${full['name']}/${full["latest_version"]}/">view latest</a>
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

export const options = (isReviewer, reports) => {

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
    cols = cols.concat([
        {
            "data": "author",
            "render": buildBasicCell
        },
        {
            "data": "requester",
            "render": buildBasicCell
        }]);

    return {
        "dom": '<"top"f>rt<"bottom"lp><"clear">',
        "data": reports,
        "collapsed": true,
        "columns": cols,
        "order": [
            [2, 'desc']
        ],
        "lengthMenu": [10, 25, 50, 75, 100],
        "pageLength": 50
    }
};
