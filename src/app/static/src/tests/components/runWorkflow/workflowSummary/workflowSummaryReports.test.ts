import {shallowMount} from "@vue/test-utils";
import {WorkflowSummaryResponse} from "../../../../js/utils/types";
import workflowSummaryReports from "../../../../js/components/runWorkflow/workflowSummary/workflowSummaryReports.vue";

describe(`workflowSummaryReports`, () => {

    const workflowSummary: WorkflowSummaryResponse = {
        ref: "commit123",
        missing_dependencies: {},
        reports: [
            {
                name: "testReport",
                param_list: [{name: "disease", value: "Measles"}],
                default_param_list: [{name: "nmin", value: "123"}],
            },
            {
                name: "testReport2",
                param_list: [],
                default_param_list: [{name: "nmin2", value: "234"}, {name: "disease", value: "HepC"}]
            },
            {
                name: "testReport2",
                param_list: [{name: "nmin2", value: "345"}, {name: "disease", value: "Malaria"}],
                default_param_list: []
            }
        ]
    }

    const workflowSummary2: WorkflowSummaryResponse = {
        ref: "commit123",
        missing_dependencies: {
            no_dependency: [],
            use_dependency: ["other"],
            use_dependency2: []
        },
        reports: [
            {
                name: "no_dependency",
                depends_on: null,
                param_list: [],
                default_param_list: [],
            },
            {
                name: "use_dependency",
                depends_on: null,
                param_list: [],
                default_param_list: [],
            },
            {
                name: "use_dependency2",
                depends_on: ["use_dependency"],
                param_list: [],
                default_param_list: []
            },
        ]
    }

    const mockTooltip = jest.fn();

    const getWrapper = (summary: Partial<WorkflowSummaryResponse> = {}) => {
        return shallowMount(workflowSummaryReports,
            {
                propsData: {
                    workflowSummary: summary,
                    gitCommit: "commit123"
                },
                directives: {"tooltip": mockTooltip}
            })
    }

    it(`it can render tooltip text for reports that run single and multiple times`, () => {
        const sameReports = [
            {name: "testReport", param_list: [], default_param_list: []},
            {name: "testReport", param_list: [], default_param_list: []},
            {name: "testReport2", param_list: [], default_param_list: []}
        ];
        const wrapper = getWrapper({reports: sameReports});

        expect(wrapper.find("#workflow-summary").exists()).toBe(true)
        const reports = wrapper.findAll("#report-name-icon")
        expect(reports.length).toBe(3)

        expect(reports.at(0).find("h5").text()).toBe("testReport")
        expect(mockTooltip.mock.calls[0][1].value).toEqual("testReport runs 2 times")

        expect(reports.at(2).find("h5").text()).toBe("testReport2")
        expect(mockTooltip.mock.calls[2][1].value).toEqual("testReport2 runs 1 time")

    });

    it(`it can render report name and info icon`, () => {
        const wrapper = getWrapper(workflowSummary);

        expect(wrapper.find("#workflow-summary").exists()).toBe(true)
        const reports = wrapper.findAll("#report-name-icon")
        expect(reports.length).toBe(3)

        expect(reports.at(0).find("h5").text()).toBe("testReport")
        expect(reports.at(1).find("h5").text()).toBe("testReport2")
        expect(reports.at(2).find("h5").text()).toBe("testReport2")

        const indexes = [0, 1, 2];
        indexes.forEach(idx => {
            expect(reports.at(idx).find("info-icon-stub").exists()).toBeTruthy()
            expect(reports.at(idx).find("info-icon-stub").attributes()).toEqual({
                class: "custom-class",
                size: "1.2x",
                stroke: "grey"
            })
        });

    });

    it(`it can render placeholder text when no parameters to display`, () => {
        const wrapper = getWrapper({reports: [{name: "newReport", param_list: []}]});
        const params = wrapper.findAll(".non-default-param")
        expect(params.length).toBe(0)
        expect(wrapper.find(".report-params p").text()).toBe("There are no parameters")
    });

    it(`it can render non-default parameters`, () => {
        const wrapper = getWrapper(workflowSummary);

        const parametersHeading = wrapper.find(".report-params span");
        expect(parametersHeading.text()).toBe("Parameters");

        const reportRows = wrapper.findAll(".report-params");

        //report 1
        let params = reportRows.at(0).findAll(".non-default-param");
        expect(params.length).toBe(1);
        expect(params.at(0).text()).toBe("disease: Measles");

        //report 2
        params = reportRows.at(1).findAll(".non-default-param");
        expect(params.length).toBe(0);

        //report 3
        params = reportRows.at(2).findAll(".non-default-param");
        expect(params.length).toBe(2);
        expect(params.at(0).text()).toBe("nmin2: 345");
        expect(params.at(1).text()).toBe("disease: Malaria");

    });

    it(`it can render default parameters`, () => {
        const wrapper = getWrapper(workflowSummary);

        //report 1
        const defaultParams1 = wrapper.findAll("#default-params-0 p");
        expect(defaultParams1.length).toBe(1);
        expect(defaultParams1.at(0).text()).toEqual("nmin: 123");

        //report 2
        const defaultParams2 = wrapper.findAll("#default-params-1 p");
        expect(defaultParams2.at(0).text()).toEqual("nmin2: 234");
        expect(defaultParams2.at(1).text()).toEqual("disease: HepC");

        //report 3 - no defaults
        const defaultParams3 = wrapper.find("default-params-2");
        expect(defaultParams3.exists()).toBe(false);
    });

    it("shows expand default parameters link only for reports with default parameters", () => {
        const wrapper = getWrapper(workflowSummary);

        expect(wrapper.find("#default-params-0 b-link-stub.show-defaults .when-closed").text()).toBe("Show");
        expect(wrapper.find("#default-params-0 b-link-stub.show-defaults .when-open").text()).toBe("Hide");

        expect(wrapper.find("#default-params-1 b-link-stub.show-defaults .when-closed").text()).toBe("Show");
        expect(wrapper.find("#default-params-1 b-link-stub.show-defaults .when-open").text()).toBe("Hide");

        expect(wrapper.find("#default-params-2 b-link-stub.show-defaults").exists()).toBe(false);

    });

    it(`it can render depends on dependencies and missing dependencies`, () => {
        const wrapper = getWrapper(workflowSummary2);

        const dependencies = wrapper.findAll(".dependencies");
        expect(dependencies.length).toBe(2);

        const dependsOn = wrapper.findAll(".dependsOn");
        expect(dependsOn.length).toBe(1);
        expect(dependsOn.at(0).find("span").text()).toEqual("Depends on");
        expect(dependsOn.at(0).find("p").text()).toEqual("use_dependency");

        const missingDependency = wrapper.findAll(".missingDependency");
        expect(missingDependency.length).toBe(1);
        expect(missingDependency.at(0).find("span").text()).toEqual("Missing dependency");
        expect(missingDependency.at(0).find("p").text()).toEqual("other");
    });

});
