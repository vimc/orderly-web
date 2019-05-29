import {options} from "../js/utils/reportsTable"
const $ = require('jquery');

describe("reportsTable", () => {

    it("has expected columns when user is reviewer", () => {
        const cols = options(true, []).columns;
        expect(cols.length).toBe(5);
        expect(cols.map(c => c.data)).toStrictEqual(["name", "id", "published", "author", "requester"])
    });

    it("has expected columns when user is not a reviewer", () => {
        const cols = options(false, []).columns;
        expect(cols.length).toBe(4);
        expect(cols.map(c => c.data)).toStrictEqual(["name", "id", "author", "requester"])
    });

    describe("name cell", () => {

        it("is empty if data is null", () => {
            const nameCol = options(false, []).columns[0];
            const $result = $(nameCol.render(null));
            expect($result.text()).toBe("")
        });

        it("contains display name for parent", () => {
            const nameCol = options(false, []).columns[0];
            const $result = $(nameCol.render("r1", null, {display_name: "report name", tt_parent: 0}));
            expect($($result.find("span")[0]).text()).toBe("report name");
        });

        it("contains link to latest version for parent", () => {
            const nameCol = options(false, []).columns[0];
            const $result = $(nameCol.render("r1", null, {
                display_name: "report name", latest_version: "v1",
                name: "r1", tt_parent: 0
            }));

            const $link = $result.find("a");
            expect($link.attr("href")).toBe("/reports/r1/v1/");
            expect($link.text()).toBe("view latest");
        });

        it("contains number of versions for parent", () => {
            const nameCol = options(false, []).columns[0];
            let $result = $(nameCol.render("r1", null, {display_name: "report name", num_versions: 1, tt_parent: 0}));
            expect($result.find("span.text-muted").text()).toBe("1 version: ");

            $result = $(nameCol.render("r1", null, {display_name: "report name", num_versions: 2, tt_parent: 0}));
            expect($result.find("span.text-muted").text()).toBe("2 versions: ");
        });

        it("is empty for child", () => {
            const nameCol = options(false, []).columns[0];
            const $result = $(nameCol.render("name", null, {tt_parent: 1, display_name: "report name"}));
            expect($result.text()).toBe("")
        });
    });

    describe("version cell", () => {

        it("is empty if data is null", () => {
            const versionColDefinition = options(false, []).columns[1];
            const $result = $(versionColDefinition.render(null));
            expect($result.text()).toBe("")
        });

        it("is empty for parent rows", () => {
            const versionColDefinition = options(false, []).columns[1];
            const result = versionColDefinition.render("some-id", null, {tt_parent: 0});
            expect(result).toBe("");
        });

        it("contains link to report version page for child", () => {
            const versionColDefinition = options(false, []).columns[1];
            const $result = $(versionColDefinition.render("some-id", null, {
                tt_parent: 1,
                name: "r1",
                date: "human friendly date"
            }));

            expect($result.attr("href")).toBe("/reports/r1/some-id/");
            expect($($result.find("div")[0]).text()).toBeIgnoringWhitespace("human friendly date");
            expect($($result.find("div.small")[0]).text()).toBeIgnoringWhitespace("(some-id)");
        });
    });

    describe("status cell", () => {

        it("is empty if data is null", () => {
            const statusCol = options(true, []).columns[2];
            const $result = $(statusCol.render(null));
            expect($result.text()).toBe("")
        });

        it("is empty for parent rows", () => {
            const statusCol = options(true, []).columns[2];
            const result = statusCol.render("some-id", null, {tt_parent: 0});
            expect(result).toBe("");
        });

        it("renders published badge", () => {
            const statusCol = options(true, []).columns[2];
            const $result = $(statusCol.render(true, null, {tt_parent: 1}));
            expect($result.text()).toBe("published");
            expect($result.attr("class")).toBe("badge-published badge float-left");
        });

        it("renders internal badge", () => {
            const statusCol = options(true, []).columns[2];
            const $result = $(statusCol.render(false, null, {tt_parent: 1}));
            expect($result.text()).toBe("internal");
            expect($result.attr("class")).toBe("badge-internal badge float-left");
        });

    });


});