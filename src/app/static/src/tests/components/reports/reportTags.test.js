import {shallowMount} from "@vue/test-utils";
import {mockAxios} from "../../mockAxios";
import ReportTags from "../../../js/components/reports/reportTags.vue";
import TagList from "../../../js/components/reports/tagList.vue";
import ErrorInfo from "../../../js/components/errorInfo.vue";
import Vue from "vue";

describe("reportTags", () => {
    const propsData = {
        report : {
            name: "r1",
            id: "v1"
        },
        canEdit: false
    };

    const mockTags = {
        version_tags: ["version"],
        report_tags: ["report"],
        orderly_tags: ["orderly"]
    };

    beforeEach(() => {
        mockAxios.reset();
        mockAxios.onGet('http://app/report/r1/version/v1/tags/')
            .reply(200, {"data": mockTags});
    });

    it('fetches tags on mount', async (done) => {
        const wrapper = shallowMount(ReportTags, {propsData: propsData});

        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(1);
            expect(wrapper.vm.$data.tags).toStrictEqual(mockTags);
            done();
        });
    });

    it('displays tags sorted and deduped', async () => {
        const wrapper = shallowMount(ReportTags, {propsData: propsData});
        wrapper.setData({
            tags: {
                version_tags: ["b"],
                report_tags: ["c", "a"],
                orderly_tags: ["a", "b"]
            }
        });

        await Vue.nextTick();

        const tags = wrapper.findAll("span");
        expect(tags.length).toBe(3);
        expect(tags.at(0).text()).toBe("a");
        expect(tags.at(1).text()).toBe("b");
        expect(tags.at(2).text()).toBe("c");
    });

    it('displays nothing if no tags are present and cannot edit', async() => {
        const wrapper = shallowMount(ReportTags, {propsData});
        wrapper.setData({
            tags: {
                version_tags: [],
                report_tags: [],
                orderly_tags: []
            }
        });

        await Vue.nextTick();

        const div = wrapper.findAll("#tags");
        expect(div.length).toBe(0);
    });

    it('displays component if no tags are present and can edit', async() => {
        const wrapper = shallowMount(ReportTags, {propsData: {...propsData, canEdit: true}});
        wrapper.setData({
            tags: {
                version_tags: [],
                report_tags: [],
                orderly_tags: []
            }
        });

        await Vue.nextTick();

        const div = wrapper.findAll("#tags");
        expect(div.length).toBe(1);
    });

    it('displays Edit tags link if can edit', () => {
        const wrapper = shallowMount(ReportTags, {propsData: {...propsData, canEdit: true}});
        expect(wrapper.find("a").text()).toBe("Edit tags");
    });

    it('does not display Edit tags link if cannot edit', async () => {
        const wrapper = shallowMount(ReportTags, {propsData});
        wrapper.setData({
            tags: {
                version_tags: ["tag"],
                report_tags: [],
                orderly_tags: []
            }
        });

        await Vue.nextTick();

        expect(wrapper.findAll("#tags").length).toBe(1);
        expect(wrapper.findAll("a").length).toBe(0);
    });

    it('does not display modal by default', () => {
        const wrapper = shallowMount(ReportTags, {propsData: {...propsData, canEdit: true}});
        const modal = wrapper.find("#edit-tags");
        expect(modal.classes().indexOf("modal-hide")).toBeGreaterThan(-1);
    });

    it('renders modal as expected when Edit Tags is clicked', async () => {
        const wrapper = shallowMount(ReportTags, {propsData: {...propsData, canEdit: true}});
        wrapper.setData({
            tags: {
                version_tags: ["vtag", "vtag2"],
                report_tags: ["rtag"],
                orderly_tags: ["otag"]
            }
        });
        wrapper.find("a").trigger("click");
        await Vue.nextTick();
        const modal = wrapper.find("#edit-tags");
        expect(modal.classes().indexOf("modal-show")).toBeGreaterThan(-1);

        const tagLists = wrapper.findAllComponents(TagList);
        expect(tagLists.length).toBe(3);
        const versionTags = tagLists.at(0);
        expect(versionTags.props().header).toBe("Report Version Tags");
        expect(versionTags.props().description).toBe("These tags only apply to this version");
        expect(versionTags.props().editable).toBe(true);
        expect(versionTags.props().value).toStrictEqual(["vtag", "vtag2"]);

        const reportTags = tagLists.at(1);
        expect(reportTags.props().header).toBe("Report Tags");
        expect(reportTags.props().description).toBe("Warning: Editing these tags will change them for all versions of this report");
        expect(reportTags.props().editable).toBe(true);
        expect(reportTags.props().value).toStrictEqual(["rtag"]);

        const orderlyTags = tagLists.at(2);
        expect(orderlyTags.props().header).toBe("Orderly Tags");
        expect(orderlyTags.props().description).toBe("These are set in Orderly and cannot be changed");
        expect(orderlyTags.props().editable).toBe(false);
        expect(orderlyTags.props().value).toStrictEqual(["otag"]);

        expect(wrapper.find('#cancel-edit-btn').text()).toBe("Cancel");
        expect(wrapper.find("#save-tags-btn").text()).toBe("Save changes");
    });

    it("clicking Cancel button hides modal", async () => {
        const wrapper = shallowMount(ReportTags, {propsData: {...propsData, canEdit: true}});
        wrapper.find("a").trigger("click");
        await Vue.nextTick();

        wrapper.find("#cancel-edit-btn").trigger("click");
        await Vue.nextTick();
        const modal = wrapper.find("#edit-tags");
        expect(modal.classes().indexOf("modal-hide")).toBeGreaterThan(-1);
        expect(modal.classes().indexOf("modal-show")).toBe(-1);

        //should not have made any further api calls
        expect(mockAxios.history.get.length).toBe(1);
    });

    it("updates editedVersionTags when tagList emits event", () => {
        const wrapper = shallowMount(ReportTags, {propsData: {...propsData, canEdit: true}});
        wrapper.setData({tags: mockTags});

        const versionTags = wrapper.find("#version-tags");
        const newTags = ["newtag1", "newtag2"];
        versionTags.vm.$emit("input", newTags);

        expect(wrapper.vm.$data.editedVersionTags).toStrictEqual(newTags);
    });

    it("updates editedReportTags when tagList emits event", () => {
        const wrapper = shallowMount(ReportTags, {propsData: {...propsData, canEdit: true}});
        wrapper.setData({tags: mockTags});

        const reportTags = wrapper.find("#report-tags");
        const newTags = ["newtag1", "newtag2"];
        reportTags.vm.$emit("input", newTags);

        expect(wrapper.vm.$data.editedReportTags).toStrictEqual(newTags);
    });

    it("clicking saveChanges saves and refreshes tags", async (done) => {
        const wrapper = shallowMount(ReportTags, {propsData: {...propsData, canEdit: true}});
        wrapper.find("a").trigger("click");

        setTimeout(() => {
            mockAxios.onPost('http://app/report/r1/version/v1/update-tags/')
                .reply(200);

            const newTags = {
                version_tags: ["new_version_tag"],
                report_tags: ["new_report_tag"],
                orderly_tags: ["orderly_tag"]
            };
            mockAxios.onGet('http://app/report/r1/version/v1/tags/')
                .reply(200, {"data": newTags});

            wrapper.vm.$data.editedVersionTags = ["new_version_tag"];
            wrapper.vm.$data.editedReportTags = ["new_report_tag"];

            wrapper.find("#save-tags-btn").trigger("click");
            setTimeout(() => {
                expect(mockAxios.history.post.length).toBe(1);
                expect(mockAxios.history.post[0].url).toBe('http://app/report/r1/version/v1/update-tags/');
                expect(JSON.parse(mockAxios.history.post[0].data)).toStrictEqual({
                    version_tags: ["new_version_tag"],
                    report_tags: ["new_report_tag"]
                });

                expect(mockAxios.history.get.length).toBe(2);
                expect(wrapper.vm.$data.tags).toStrictEqual(newTags);

                expect(wrapper.find("#edit-tags").classes().indexOf("modal-hide")).toBeGreaterThan(-1);

                done();
            });
        });

    });

    it("error on save displays error message", async (done) => {
        mockAxios.onPost('http://app/report/r1/version/v1/update-tags/')
            .reply(500, "TEST ERROR");

        const wrapper = shallowMount(ReportTags, {propsData: {...propsData, canEdit: true}});
        wrapper.vm.saveTags();
        setTimeout(() => {
            expect(wrapper.vm.$data.defaultMessage).toBe("An error occurred updating tags");
            expect(wrapper.vm.$data.error.response.data).toBe("TEST ERROR");

            expect(wrapper.findComponent(ErrorInfo).props().defaultMessage).toBe("An error occurred updating tags");
            expect(wrapper.findComponent(ErrorInfo).props().apiError.response.data).toBe("TEST ERROR");

            done();
        });
    });

    it("error clears on next save", () => {
        mockAxios.onPost('http://app/report/r1/version/v1/update-tags/')
            .reply(200);
        const wrapper = shallowMount(ReportTags, {propsData: {...propsData, canEdit: true}});
        wrapper.setData({
            error: "TEST ERROR",
            defaultMessage: "TEST DEFAULT"
        });

        wrapper.vm.saveTags();
        expect(wrapper.vm.error).toBe("");
        expect(wrapper.vm.defaultMessage).toBe("");

    });

    it("error on get tags displays error message", async (done) => {
        mockAxios.onGet('http://app/report/r1/version/v1/tags/')
            .reply(500, "TEST ERROR");

        const wrapper = shallowMount(ReportTags, {propsData: {...propsData, canEdit: true}});
        wrapper.vm.refreshTags();
        setTimeout(() => {
            expect(wrapper.vm.$data.defaultMessage).toBe("An error occurred fetching tags");
            expect(wrapper.vm.$data.error.response.data).toBe("TEST ERROR");

            expect(wrapper.findComponent(ErrorInfo).props().defaultMessage).toBe("An error occurred fetching tags");
            expect(wrapper.findComponent(ErrorInfo).props().apiError.response.data).toBe("TEST ERROR");

            done();
        });
    });

    it("error clears on next refresh", () => {
        const wrapper = shallowMount(ReportTags, {propsData: {...propsData, canEdit: true}});
        wrapper.setData({
            error: "TEST ERROR",
            defaultMessage: "TEST DEFAULT"
        });

        wrapper.vm.refreshTags();
        expect(wrapper.vm.error).toBe("");
        expect(wrapper.vm.defaultMessage).toBe("");
    });
});