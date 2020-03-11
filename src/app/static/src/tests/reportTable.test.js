import {nameFilter, options, statusFilter} from "../js/utils/reportsTable"

const $ = require('jquery');

describe("reportsTable", () => {

    const customFields = ["author", "requester"]

    it("has expected columns when user is reviewer", () => {
        const cols = options(true, [], customFields).columns;
        expect(cols.length).toBe(8);
        expect(cols.map(c => c.data)).toStrictEqual(["name", "id", "published", "tags", "parameter_values", "author", "requester", "display_name"])
    });

    it("has expected columns when user is not a reviewer", () => {
        const cols = options(false, [], customFields).columns;
        expect(cols.length).toBe(7);
        expect(cols.map(c => c.data)).toStrictEqual(["name", "id", "tags", "parameter_values", "author", "requester", "display_name"])
    });

    it("has invisible display name col", () => {
        const cols = options(false, [], customFields).columns;
        expect(cols[6].visible).toBe(false);
    });

    it("is ordered by version desc", () => {
        const opts = options(false, [], customFields);
        expect(opts.order).toStrictEqual([[2, "desc"]]);
        expect(opts.columns[1].data).toBe("id");
    });

    it("has expected dom structure", () => {
        const dom = options(true, [], customFields).dom;
        expect(dom).toBe('<"top">rt<"bottom"lp><"clear">',);
    });

    it("has expected options", () => {
        const opts = options(true, [], customFields);
        expect(opts.pageLength).toBe(50);
        expect(opts.autoWidth).toBe(false);
    });

    describe("name cell", () => {

        it("contains display name for parent", () => {
            const nameCol = options(false, [], customFields).columns[0];
            const $result = $(nameCol.render("r1", null, {display_name: "report name", tt_parent: 0}));
            expect($($result.find("span")[0]).text()).toBe("report name");
        });

        it("contains link to latest version for parent", () => {
            const nameCol = options(false, [], customFields).columns[0];
            const $result = $(nameCol.render("r1", null, {
                display_name: "report name", latest_version: "v1",
                name: "r1", tt_parent: 0
            }));

            const $link = $result.find("a");
            expect($link.attr("href")).toBe("http://app/report/r1/v1/");
            expect($link.text()).toBe("view latest");
        });

        it("contains number of versions for parent", () => {
            const nameCol = options(false, [], customFields).columns[0];
            let $result = $(nameCol.render("r1", null, {display_name: "report name", num_versions: 1, tt_parent: 0}));
            expect($result.find("span.text-muted").text()).toBe("1 version: ");

            $result = $(nameCol.render("r1", null, {display_name: "report name", num_versions: 2, tt_parent: 0}));
            expect($result.find("span.text-muted").text()).toBe("2 versions: ");
        });

        it("is empty for child", () => {
            const nameCol = options(false, [], customFields).columns[0];
            const $result = $(nameCol.render("name", null, {tt_parent: 1, display_name: "report name"}));
            expect($result.text()).toBe("")
        });
    });

    describe("version cell", () => {

        it("is empty for parent rows", () => {
            const versionColDefinition = options(false, [], customFields).columns[1];
            const result = versionColDefinition.render("some-id", null, {tt_parent: 0});
            expect(result).toBe("");
        });

        it("contains link to report version page for child", () => {
            const versionColDefinition = options(false, [], customFields).columns[1];
            const $result = $(versionColDefinition.render("some-id", null, {
                tt_parent: 1,
                name: "r1",
                date: "human friendly date"
            }));

            expect($result.attr("href")).toBe("http://app/report/r1/some-id/");
            expect($result.find("span:not(.badge)").text()).toBeIgnoringWhitespace("human friendly date");
            expect($result.find("div.small").text()).toBeIgnoringWhitespace("(some-id)");
        });

        it("contains latest badge if version is latest", () => {
            const versionColDefinition = options(false, [], customFields).columns[1];
            const $result = $(versionColDefinition.render("v1", null, {
                tt_parent: 1,
                name: "r1",
                date: "human friendly date",
                latest_version: "v1"
            }));

            const $badge = $result.find(".badge");
            expect($badge.text()).toBe("latest");
            expect($badge.attr("class")).toBe("badge-info badge float-right");
        });


        it("contains out-dated badge if version is out-dated", () => {
            const versionColDefinition = options(false, [], customFields).columns[1];
            const $result = $(versionColDefinition.render("v1", null, {
                tt_parent: 1,
                name: "r1",
                date: "human friendly date",
                latest_version: "v2"
            }));

            const $badge = $result.find(".badge");
            expect($badge.text()).toBe("out-dated");
            expect($badge.attr("class")).toBe("badge-light badge float-right");
        });

    });

    describe("status cell", () => {

        it("is empty for parent rows", () => {
            const statusCol = options(true, [], customFields).columns[2];
            const result = statusCol.render("some-id", null, {tt_parent: 0});
            expect(result).toBe("");
        });

        it("renders published badge", () => {
            const statusCol = options(true, [], customFields).columns[2];
            const $result = $(statusCol.render(true, null, {tt_parent: 1}));
            expect($result.text()).toBe("published");
            expect($result.attr("class")).toBe("badge-published badge float-left");
        });

        it("renders internal badge", () => {
            const statusCol = options(true, [], customFields).columns[2];
            const $result = $(statusCol.render(false, null, {tt_parent: 1}));
            expect($result.text()).toBe("internal");
            expect($result.attr("class")).toBe("badge-internal badge float-left");
        });

    });

    describe("tags cell", () => {

        it("is not orderable", () => {
            const tagsCol = options(false, [], customFields).columns[2];
            expect(tagsCol.orderable).toBe(false);
        });

        it("show tags for parent rows", () => {
            const tagsCol = options(false, [], customFields).columns[2];
            expect(tagsCol.data).toBe("tags");

            const result = tagsCol.render(["tag1"], null, {tt_parent: 0});
            expect(result).toBe("<span class=\"badge badge-tag float-left mr-1 mb-1\">tag1</span>");
        });

        it("shows tags for child row", () => {
            const tagsCol = options(false, [], customFields).columns[2];
            expect(tagsCol.data).toBe("tags");

            const result = tagsCol.render(["tag1", "tag2"], null, {tt_parent: 1});
            expect(result).toBe("<span class=\"badge badge-tag float-left mr-1 mb-1\">tag1</span>" +
                                "<span class=\"badge badge-tag float-left mr-1 mb-1\">tag2</span>");
        });
    });

    describe("parameter values cell", () => {

        it("is not orderable", () => {
            const paramValsCol = options(false, [], customFields).columns[3];
            expect(paramValsCol.orderable).toBe(false);
        });

        it("is empty for parent rows", () => {
            const paramValsCol = options(false, [], customFields).columns[3];
            expect(paramValsCol.data).toBe("parameter_values");

            const result = paramValsCol.render("parameter_values", null, {tt_parent: 0});
            expect(result).toBe("");
        });

        it("shows parameter values for child row", () => {
            const paramValsCol = options(false, [], customFields).columns[3];
            expect(paramValsCol.data).toBe("parameter_values");

            const result = paramValsCol.render("parameter_values", null, {tt_parent: 1});
            expect(result).toBe("parameter_values");
        });
    });

    describe("author cell", () => {

        it("is not orderable", () => {
            const requesterCol = options(false, [], customFields).columns[4];
            expect(requesterCol.orderable).toBe(false);
        });

        it("is empty for parent rows", () => {
            const authorCol = options(false, [], customFields).columns[4];
            expect(authorCol.data).toBe("author");

            const result = authorCol.render("author", null, {tt_parent: 0});
            expect(result).toBe("");
        });

        it("shows author for child row", () => {
            const authorCol = options(false, [], customFields).columns[4];
            expect(authorCol.data).toBe("author");

            const result = authorCol.render("author", null, {tt_parent: 1});
            expect(result).toBe("author");
        });
    });

    describe("requester cell", () => {

        it("is not orderable", () => {
            const requesterCol = options(false, [], customFields).columns[5];
            expect(requesterCol.orderable).toBe(false);
        });

        it("is empty for parent rows", () => {
            const requesterCol = options(false, [], customFields).columns[5];
            expect(requesterCol.data).toBe("requester");

            const result = requesterCol.render("requester", null, {tt_parent: 0});
            expect(result).toBe("");
        });

        it("shows author for child row", () => {
            const requesterCol = options(false, [], customFields).columns[5];
            expect(requesterCol.data).toBe("requester");

            const result = requesterCol.render("requester", null, {tt_parent: 1});
            expect(result).toBe("requester");
        });
    });

    describe("filtering", () => {

        const internal = [null, "r1", "v1", false, "p1=v1", "author", "requester", "displaya"];
        const published = [null, "r2", "v1", true, "p1=v2", "author", "requester", "displayb"];
        const parentBoth = [null, "r2,r1", "v1,v1", "true,false", null,  "author", "requester", "displaya,displayb"];
        const parentInternalOnly = [null, "", "", "false,false", null, "", "", ""];
        const parentPublishedOnly = [null, "", "", "true,true", null, "", "", ""];

        it("can return reports with any status", () => {
            expect(statusFilter("all", internal)).toBe(true);
            expect(statusFilter("all", published)).toBe(true);
            expect(statusFilter("all", parentBoth)).toBe(true);
            expect(statusFilter("all", parentPublishedOnly)).toBe(true);
            expect(statusFilter("all", parentInternalOnly)).toBe(true);
        });

        it("can return published reports", () => {
            expect(statusFilter("published", internal)).toBe(false);
            expect(statusFilter("published", published)).toBe(true);
            expect(statusFilter("published", parentBoth)).toBe(true);
            expect(statusFilter("published", parentPublishedOnly)).toBe(true);
            expect(statusFilter("published", parentInternalOnly)).toBe(false);
        });

        it("can return internal reports", () => {
            expect(statusFilter("internal", internal)).toBe(true);
            expect(statusFilter("internal", published)).toBe(false);
            expect(statusFilter("internal", parentBoth)).toBe(true);
            expect(statusFilter("internal", parentPublishedOnly)).toBe(false);
            expect(statusFilter("internal", parentInternalOnly)).toBe(true);
        });

        it("can filter by name", () => {
            expect(nameFilter(7, "2", internal)).toBe(false);
            expect(nameFilter(7, "2", published)).toBe(true);
            expect(nameFilter(7, "2", parentBoth)).toBe(true);
        });

        it("can filter by display name", () => {
            expect(nameFilter(7, "aya", internal)).toBe(true);
            expect(nameFilter(7, "aya", published)).toBe(false);
            expect(nameFilter(7, "aya", parentBoth)).toBe(true);
        });
    });

});