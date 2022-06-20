import { shallowMount } from "@vue/test-utils";
import { WorkflowSummaryResponse } from "../../../../js/utils/types";
import workflowSummaryReports from "../../../../js/components/runWorkflow/workflowSummary/workflowSummaryReports.vue";
import runWorkflowParameters from "../../../../js/components/runWorkflow/runWorkflowParameters.vue";

describe(`workflowSummaryReports`, () => {

    const workflowSummary: WorkflowSummaryResponse = {
        ref: "commit123",
        missing_dependencies: {},
        reports: [
            {
                name: "testReport",
                param_list: [{ name: "disease", value: "Measles" }],
                default_param_list: [{ name: "nmin", value: "123" }],
            },
            {
                name: "testReport2",
                param_list: [],
                default_param_list: [{ name: "nmin2", value: "234" }, { name: "disease", value: "HepC" }]
            },
            {
                name: "testReport2",
                param_list: [{ name: "nmin2", value: "345" }, { name: "disease", value: "Malaria" }],
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
                directives: { "tooltip": mockTooltip }
            })
    }

    it(`it can render tooltip text for reports that run single and multiple times`, () => {
        const sameReports = [
            { name: "testReport", param_list: [], default_param_list: [] },
            { name: "testReport", param_list: [], default_param_list: [] },
            { name: "testReport2", param_list: [], default_param_list: [] }
        ];
        const wrapper = getWrapper({ reports: sameReports });

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

    it(`it can render parameter components`, () => {
        const wrapper = getWrapper(workflowSummary);

        const parametersHeading = wrapper.find(".report-params span");
        expect(parametersHeading.text()).toBe("Parameters");

        const reportRows = wrapper.findAllComponents(runWorkflowParameters);
        expect(reportRows.length).toBe(3);
        expect(reportRows.at(0).props("reportIndex")).toBe(0);
        expect(reportRows.at(0).props("report")).toBe(workflowSummary.reports[0]);
        expect(reportRows.at(1).props("reportIndex")).toBe(1);
        expect(reportRows.at(1).props("report")).toBe(workflowSummary.reports[1]);
        expect(reportRows.at(2).props("reportIndex")).toBe(2);
        expect(reportRows.at(2).props("report")).toBe(workflowSummary.reports[2]);
    });

    it(`it can render depends on dependencies and missing dependencies`, () => {
        const wrapper = getWrapper(workflowSummary2);

        const reports = wrapper.findAll(".single-workflow-summary-area");
        expect(reports.length).toBe(3);

        const dependencies = wrapper.findAll(".dependencies");
        expect(dependencies.length).toBe(2);
        expect(reports.at(0).findAll(".dependencies").length).toEqual(0);

        const dependsOn1 = reports.at(1).find(".dependsOn");
        expect(dependsOn1.exists()).toBe(false);
        const missingDependency1 = reports.at(1).find(".missingDependency");
        expect(missingDependency1.find("span").text()).toEqual("Missing dependency");
        expect(missingDependency1.find("p").text()).toEqual("other");

        const dependsOn2 = reports.at(2).find(".dependsOn");
        expect(dependsOn2.find("span").text()).toEqual("Depends on");
        expect(dependsOn2.find("p").text()).toEqual("use_dependency");
        const missingDependency2 = reports.at(2).find(".missingDependency");
        expect(missingDependency2.exists()).toBe(false);
    });

});
