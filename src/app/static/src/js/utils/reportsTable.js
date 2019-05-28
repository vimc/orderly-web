function buildIdCell(data, type, full) {
    if (!data) return '';
    if (full["tt_parent"] === 0) {
        return '';
    }
    return `<a href="/reports/${full['name']}/${data}/">
<div>
${full.date}
</div>
<div class="small">(${data})</div></a>`;
}

function buildNameCell(data, type, full) {
    if (!data) return '';
    if (full["tt_parent"] > 0) {
        return '';
    }
    const versionText = full.num_versions > 1 ? "versions" : "version";
    return `<span>${data}</span><br/>
<span class="text-muted">${full.num_versions} ${versionText}: </span>
<a href="/reports/${full['name']}/${full.id}/">view latest</a>`;
}

function buildStatusCell(data) {
    if (data == null) {
        return '';
    }
    if (data) {
        return '<span class="badge-published badge float-left">published</span>'
    }
    else {
        return '<span class="badge-internal badge float-left">internal</span>'
    }
}

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

const isReviewer = typeof isReviewer !== "undefined";

export const options = {
    "dom": '<"top"f>rt<"bottom"lp><"clear">',
    "data": reports,
    "collapsed": true,
    "columns": cols,
    "order": [
        [1, 'asc']
    ],
    "lengthMenu": [10, 25, 50, 75, 100],
    "pageLength": 50
};
