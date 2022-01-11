import {mockAxios} from "../../../mockAxios";
import {mount, Wrapper} from "@vue/test-utils";
import Vue from "vue";
import runWorkflowReport from "../../../../js/components/runWorkflow/runWorkflowReport.vue";
import {RunWorkflowMetadata} from "../../../../js/utils/types";
import {mockRunWorkflowMetadata, mockRunReportMetadata} from "../../../mocks";

const gitCommits = [
    {id: "abcdef", date_time: "Mon Jun 08, 12:01"},
    {id: "abc123", date_time: "Tue Jun 09, 13:11"}
];

const reports = [
    { name: "minimal", date: null },
    { name: "global", date: new Date() }
];

describe("runWorkflowReport validation", () => {
    beforeEach(() => {
        mockAxios.reset();

        mockAxios.onGet('http://app/report/run-metadata')
            .reply(200, {"data": mockRunReportMetadata()});

        mockAxios.onGet('http://app/git/branch/master/commits/')
            .reply(200, {"data": gitCommits});
        mockAxios.onGet('http://app/reports/runnable/?branch=master&commit=abcdef&show_all=true')
            .reply(200, {"data": reports});
    });

    const getWrapper = (workflowMetadata: Partial<RunWorkflowMetadata> = {}) => {
        const propsData = {
            workflowMetadata: mockRunWorkflowMetadata(workflowMetadata)
        };
        return mount(runWorkflowReport, {propsData});
    };

    const expectEmittedValid = (wrapper: Wrapper<any>) => {
        expect(wrapper.emitted("valid").length).toBe(1);
        expect(wrapper.emitted("valid")[0][0]).toBe(true);

        expect(wrapper.vm.$data.error).toBe("");
        expect(wrapper.vm.$data.defaultMessage).toBe("");
    };

    const updateComponentMetadataFromLastEmitted = (wrapper: Wrapper<any>) => {
        // simulate the workflow wizard managing the metadata object by feeding the last update patch back to
        // the component
        const oldMetadata = wrapper.vm.$data.workflowMetadata;
        const patches = wrapper.emitted("update");
        const newMetadata = {
            ...oldMetadata,
            ...patches[patches.length-1][0]
        };
        wrapper.setData({workflowMetadata: newMetadata});
    };

    it("emits valid true when initialised with valid workflow with no parameters", (done) => {
        mockAxios.onGet('http://app/report/minimal/config/parameters/')
            .reply(200, {data: []});
        mockAxios.onGet('http://app/report/global/config/parameters/')
            .reply(200, {data: []});

        const wrapper = getWrapper({
            reports: [
                {name: "minimal"},
                {name: "global"}
            ]
        });
        setTimeout(() => {
            expectEmittedValid(wrapper);
            done();
        });
    });

    it("emits valid true when initialised with valid workflow with parameters", (done) => {
        mockAxios.onGet('http://app/report/minimal/config/parameters/')
            .reply(200, {data: [{name: "nmin", value: ""}, {name: "nmax", value: ""}]});
        mockAxios.onGet('http://app/report/global/config/parameters/')
            .reply(200, {data: []});

        const wrapper = getWrapper({
            reports: [
                {name: "minimal", params: {"nmin": "7", "nmax": "4"}},
                {name: "global"}
            ]
        });
        setTimeout(() => {
            expectEmittedValid(wrapper);
            done();
        });
    });

    it("does not emit valid event when initialised with invalid workflow with parameters", (done) => {
        // This probably shouldn't ever happen since we'd expect to initialise with a valid workflow which has
        // already run, but might initialise with partially complete workflow in future
        const wrapper = getWrapper({
            reports: [
                {name: "minimal", params: {"nmin": "7", "nmax": null}},
                {name: "global"}
            ]
        });
        setTimeout(() => {
            expect(wrapper.emitted("valid")).toBeUndefined();
            done();
        });
    });

    it("emits valid true when workflow becomes valid after adding report with no parameters", (done) => {
        mockAxios.onGet('http://app/report/minimal/config/parameters/?commit=abcdef')
            .reply(200, {data: []});
        const wrapper = getWrapper({git_commit: "abcdef"});
        setTimeout(async () => {
            expect(wrapper.emitted("valid")).toBeUndefined();
            wrapper.setData({selectedReport: {name: "minimal"}});
            await Vue.nextTick();
            wrapper.find("#add-report-button").trigger("click");

            setTimeout(() => {
                expectEmittedValid(wrapper);
                done();
            });
        });
    });

    it("emits valid true with workflow becomes valid after adding report with parameters with default", (done) => {
        mockAxios.onGet('http://app/report/minimal/config/parameters/?commit=abcdef')
            .reply(200, {data: [{name: "p1", value: "v1"}, {name: "p2", value: "v2"}]});
        const wrapper = getWrapper({git_commit: "abcdef"});
        setTimeout(async () => {
            expect(wrapper.emitted("valid")).toBeUndefined();
            wrapper.setData({selectedReport: {name: "minimal"}});
            await Vue.nextTick();
            wrapper.find("#add-report-button").trigger("click");

            setTimeout(async () => {
                updateComponentMetadataFromLastEmitted(wrapper);
                await Vue.nextTick();
                expectEmittedValid(wrapper);
                done();
            });
        });
    });

    it("emits valid false when workflow becomes invalid after adding report with parameters without default", (done) => {
        mockAxios.onGet('http://app/report/global/config/parameters/?commit=abcdef')
            .reply(200, {data: []});
        mockAxios.onGet('http://app/report/minimal/config/parameters/?commit=abcdef')
            .reply(200, {data: [{name: "p1", value: null}, {name: "p2", value: "v2"}]});
        const wrapper = getWrapper({
            git_commit: "abcdef",
            reports: [{name: "global"}]
        });
        setTimeout(async () => {
            expectEmittedValid(wrapper);
            wrapper.setData({selectedReport: {name: "minimal"}});
            await Vue.nextTick();
            wrapper.find("#add-report-button").trigger("click");

            setTimeout(async () => {
                updateComponentMetadataFromLastEmitted(wrapper);
                await Vue.nextTick();
                expect(wrapper.emitted("valid").length).toBe(2);
                expect(wrapper.emitted("valid")[1][0]).toBe(false);
                done();
            });
        });
    });

    it("emits valid true when workflow becomes valid after set parameter value", (done) => {
        mockAxios.onGet('http://app/report/global/config/parameters/')
            .reply(200, {data: [{name: "p1", value: null}]});

        const wrapper = getWrapper({
            reports: [{name: "global", params: {p1: null}}]
        });
        setTimeout(async () => {
            expect(wrapper.emitted("valid")).toBeUndefined();

            wrapper.find("#param-control-0").setValue("1");
            await Vue.nextTick();
            expectEmittedValid(wrapper);
            done();
        });
    });

    it("emits valid false when workflow becomes invalid after unset parameter value", (done) => {
        mockAxios.onGet('http://app/report/global/config/parameters/')
            .reply(200, {data: [{name: "p1", value: null}]});

        const wrapper = getWrapper({
            reports: [{name: "global", params: {p1: "v1"}}]
        });
        setTimeout(async () => {
            expectEmittedValid(wrapper);

            wrapper.find("#param-control-0").setValue("");
            await Vue.nextTick();
            expect(wrapper.emitted("valid").length).toBe(2);
            expect(wrapper.emitted("valid")[1][0]).toBe(false);
            done();
        });
    });

    it("emits valid false when workflow becomes invalid when remove only report", (done) => {
        mockAxios.onGet('http://app/report/global/config/parameters/')
            .reply(200, {data: []});

        const wrapper = getWrapper({
            reports: [{name: "global"}]
        });
        setTimeout(async () => {
            expectEmittedValid(wrapper);

            wrapper.find(".remove-report-button").trigger("click");
            await Vue.nextTick();
            expect(wrapper.emitted("valid").length).toBe(2);
            expect(wrapper.emitted("valid")[1][0]).toBe(false);
            done();
        });
    });

    it("emits valid true when workflow becomes valid when remove invalid report", (done) => {
        mockAxios.onGet('http://app/report/global/config/parameters/')
            .reply(200, {data: []});
        mockAxios.onGet('http://app/report/minimal/config/parameters/')
            .reply(200, {data: [{name: "p1", value: null}]});

        const wrapper = getWrapper({
            reports: [
                {name: "global"},
                {name: "minimal", params: {p1: null}}
            ]
        });
        setTimeout(async () => {
            expect(wrapper.emitted("valid")).toBeUndefined();

            wrapper.findAll(".remove-report-button").at(1).trigger("click");

            await Vue.nextTick();
            expectEmittedValid(wrapper);
            done();
        });
    });

    it("emits valid true when commit change removes report which was invalid", (done) => {
        mockAxios.onGet('http://app/reports/runnable/?branch=master&commit=abc123&show_all=true')
            .reply(200, {"data": [{ name: "minimal", date: null }]});

        mockAxios.onGet('http://app/report/minimal/config/parameters/?commit=abc123')
            .reply(200, {data: []});

        const wrapper = getWrapper({
            reports: [
                {name: "global", params: {p1: null}},
                {name: "minimal"}
            ]
        });
        setTimeout(async () => {
            expect(wrapper.emitted("valid")).toBeUndefined();

            const commitOptions = wrapper.find("#git-commit").findAll("option");
            await commitOptions.at(1).setSelected();
            updateComponentMetadataFromLastEmitted(wrapper);

            setTimeout(() => {
                expectEmittedValid(wrapper);
                done();
            });
        });
    });

    it("emits valid true when commit change removes parameter which made report invalid", (done) => {
        //Responses for default commit
        mockAxios.onGet('http://app/report/minimal/config/parameters/?commit=abcdef')
            .reply(200, {data: []});

        mockAxios.onGet('http://app/report/global/config/parameters/?commit=abcdef')
            .reply(200, {data: [{name: "p1", value: null}]});

        //Responses for newly selected commit
        mockAxios.onGet('http://app/reports/runnable/?branch=master&commit=abc123&show_all=true')
            .reply(200, {"data": [
                { name: "minimal", date: null },
                { name: "global", date: null }
            ]});

        mockAxios.onGet('http://app/report/minimal/config/parameters/?commit=abc123')
            .reply(200, {data: []});

        mockAxios.onGet('http://app/report/global/config/parameters/?commit=abc123')
            .reply(200, {data: []});

        const wrapper = getWrapper({
            git_commit: "abcdef",
            reports: [
                {name: "global", params: {p1: null}},
                {name: "minimal"}
            ]
        });
        setTimeout(async () => {
            expect(wrapper.emitted("valid")).toBeUndefined();

            const commitOptions = wrapper.find("#git-commit").findAll("option");
            await commitOptions.at(1).setSelected();
            updateComponentMetadataFromLastEmitted(wrapper);

            setTimeout(() => {
                expectEmittedValid(wrapper);
                done();
            });
        });
    });

    it("emits valid false when commit change adds parameter which makes report invalid", (done) => {
        //Responses for default commit
        mockAxios.onGet('http://app/report/minimal/config/parameters/?commit=abcdef')
            .reply(200, {data: []});

        mockAxios.onGet('http://app/report/global/config/parameters/?commit=abcdef')
            .reply(200, {data: [{name: "p1", value: null}]});


        //Responses for newly selected commit
        mockAxios.onGet('http://app/reports/runnable/?branch=master&commit=abc123&show_all=true')
            .reply(200, {"data": [
                    { name: "minimal", date: null },
                    { name: "global", date: null }
                ]});

        mockAxios.onGet('http://app/report/minimal/config/parameters/?commit=abc123')
            .reply(200, {data: []});

        mockAxios.onGet('http://app/report/global/config/parameters/?commit=abc123')
            .reply(200, {data: [
                {name: "p1", value: null},
                {name: "p2", value: null}
             ]});

        const wrapper = getWrapper({
            git_commit: "abcdef",
            reports: [
                {name: "global", params: {p1: "v1"}},
                {name: "minimal"}
            ]
        });
        setTimeout(async () => {
            expectEmittedValid(wrapper);

            const commitOptions = wrapper.find("#git-commit").findAll("option");
            await commitOptions.at(1).setSelected();
            updateComponentMetadataFromLastEmitted(wrapper);

            setTimeout(async () => {
                updateComponentMetadataFromLastEmitted(wrapper);
                await Vue.nextTick();
                expect(wrapper.emitted("valid").length).toBe(2);
                expect(wrapper.emitted("valid")[1][0]).toBe(false);
                done();
            });
        });
    });
});
