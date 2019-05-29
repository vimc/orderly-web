function buildIdCell(data, type, full) {
    if (! data || full["tt_parent"] === 0) {
        return '';
    }
    return `<a href="/reports/${full["name"]}/${data}/">
<div>
${full["date"]}
</div>
<div class="small">(${data})</div></a>`;
}

function buildNameCell(data, type, full) {
    if (!data || full["tt_parent"] > 0) {
        return '';
    }
    const versionText = full["num_versions"] > 1 ? "versions" : "version";
    return `<div><span>${full["display_name"]}</span><br/>
<span class="text-muted">${full["num_versions"]} ${versionText}: </span>
<a href="/reports/${full['name']}/${full["latest_version"]}/">view latest</a></div>`;
}

function buildStatusCell(data, type, full) {

    if (data == null || full["tt_parent"] === 0) {
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
    cols = cols.concat([{
        "data": "author"
    },
        {
            "data": "requester"
        }]);

    return {
        "dom": '<"top"f>rt<"bottom"lp><"clear">',
        "data": reports,
        "collapsed": true,
        "columns": cols,
        "order": [
            [1, 'asc']
        ],
        "lengthMenu": [10, 25, 50, 75, 100],
        "pageLength": 50
    }
};
