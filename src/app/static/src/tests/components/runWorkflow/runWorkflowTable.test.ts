import { shallowMount } from "@vue/test-utils";
import runWorkflowTable from '../../../js/components/runWorkflow/runWorkflowTable.vue'
import runWorkflowParameters from '../../../js/components/runWorkflow/runWorkflowParameters.vue'

const workflowStatus1 = {
    "reports": [
        {
            "key": "preterrestrial_andeancockoftherock",
            "name": "report one a",
            "status": "error",
            "date": "2021-06-16T09:51:16Z"
        },
        {
            "key": "hygienic_mammoth",
            "name": "report two a",
            "status": "success",
            "version": "20210510-100458-8f1a9624",
            "date": "2021-06-16T09:51:16Z"
        },
        {
            "key": "blue_bird",
            "name": "report three a",
            "status": "running",
            "date": null
        },
        {
            "key": "non_hygienic_mammoth",
            "name": "report four a",
            "status": "impossible",
            "date": "2021-06-16T09:51:16Z"
        },
    ]
}

const workflowSummary = {
    ref: "commit123",
    missing_dependencies: {},
    reports: [
        {
            name: "report one a",
            param_list: [{ name: "disease", value: "Measles" }],
            default_param_list: [{ name: "nmin", value: "123" }],
        },
        {
            name: "report one b",
            param_list: [],
            default_param_list: [{ name: "nmin2", value: "234" }, { name: "disease", value: "HepC" }]
        },
        {
            name: "report one c",
            param_list: [{ name: "nmin2", value: "345" }, { name: "disease", value: "Malaria" }],
            default_param_list: []
        },
        {
            name: "report one d",
            param_list: [{ name: "nmin2", value: "345" }, { name: "disease", value: "Malaria" }],
            default_param_list: []
        }
    ]
}

describe(`runWorkflowTable`, () => {

    const getWrapper = (workflowRunStatus = workflowStatus1, workflowSummary = null) => {
        return shallowMount(runWorkflowTable, { propsData: { workflowRunStatus, workflowSummary } })
    }

    it(`can render reports table`, (done) => {
        const wrapper = getWrapper()

        setTimeout(() => {
            expect(wrapper.find("table").exists()).toBe(true)
            expect(wrapper.findAll("tr").length).toBe(5)

            const headers = wrapper.findAll("th")
            expect(headers.length).toBe(3)
            expect(headers.at(0).text()).toBe("Report")
            expect(headers.at(1).text()).toBe("Status")
            expect(headers.at(2).text()).toBe("Logs")

            const reportLinks = wrapper.findAll("td > a.report-version-link")
            expect(reportLinks.length).toBe(1)

            const completedReportLink = reportLinks.at(0)
            expect(completedReportLink.text()).toBe("report two a")
            expect(completedReportLink.attributes("href")).toBe("http://app/report/report two a/20210510-100458-8f1a9624/")

            const errorStatus = wrapper.findAll("tr > td:nth-child(2)").at(0)
            expect(errorStatus.text()).toBe("Failed")
            expect(errorStatus.classes()).toContain("text-danger")

            const successStatus = wrapper.findAll("tr > td:nth-child(2)").at(1)
            expect(successStatus.text()).toBe("Complete")

            const runningStatus = wrapper.findAll("tr > td:nth-child(2)").at(2)
            expect(runningStatus.text()).toBe("Running")
            expect(runningStatus.classes()).toContain("text-secondary")

            const dependencyErrorStatus = wrapper.findAll("tr > td:nth-child(2)").at(3)
            expect(dependencyErrorStatus.text()).toBe("Dependency failed")
            expect(dependencyErrorStatus.classes()).toContain("text-danger")

            const dateColumns = wrapper.findAll("tr > td:nth-child(3)")
            expect(dateColumns.at(0).text()).toBe("Wed Jun 16 2021, 09:51")

            const logCell = wrapper.findAll("tr > td:nth-child(4)").at(0);
            const link = logCell.find("a.report-log-link");
            expect(link.text()).toBe("View log");
            expect(link.attributes("href")).toBe("#");

            done();
        })
    });

    it(`renders View Log links only for started reports`, (done) => {
        const allStatusWorkflow = {
            "status": "running",
            "reports": [
                {
                    "key": "happy_tiger",
                    "name": "report1",
                    "status": "queued",
                    "date": null
                },
                {
                    "key": "sad_marmot",
                    "name": "report2",
                    "status": "deferred",
                    "date": null
                },
                {
                    "key": "pensive_goldfinch",
                    "name": "report3",
                    "status": "impossible",
                    "date": null
                },
                {
                    "key": "sarcastic_beetle",
                    "name": "report4",
                    "status": "missing",
                    "date": null
                },
                {
                    "key": "charismatic_baboon",
                    "name": "report5",
                    "status": "success",
                    "version": "20210510-100458-8f1a9624",
                    "date": "2021-06-16T09:51:16Z"
                },
                {
                    "key": "compassionate_piranha",
                    "name": "report6",
                    "status": "error",
                    "date": "2021-06-16T09:51:16Z"
                },
                {
                    "key": "stubborn_zebra",
                    "name": "report7",
                    "status": "running",
                    "date": "2021-06-16T09:51:16Z"
                },
                {
                    "key": "grumpy_centipede",
                    "name": "report8",
                    "status": "interrupted",
                    "date": "2021-06-16T09:51:16Z"
                },
                {
                    "key": "philosophical_mussel",
                    "name": "report9",
                    "status": "orphan",
                    "date": "2021-06-16T09:51:16Z"
                },
            ]
        };
        const wrapper = getWrapper(allStatusWorkflow);

        setTimeout(() => {
            const reportRows = wrapper.findAll("table tr");
            expect(reportRows.length).toBe(10);

            const statusSelector = "td:nth-child(2)";
            const viewLogSelector = "a.report-log-link";

            expect(reportRows.at(1).find(statusSelector).text()).toBe("Queued");
            expect(reportRows.at(1).find(viewLogSelector).exists()).toBe(false);
            expect(reportRows.at(2).find(statusSelector).text()).toBe("Waiting for dependency");
            expect(reportRows.at(2).find(viewLogSelector).exists()).toBe(false);
            expect(reportRows.at(3).find(statusSelector).text()).toBe("Dependency failed");
            expect(reportRows.at(3).find(viewLogSelector).exists()).toBe(false);
            expect(reportRows.at(4).find(statusSelector).text()).toBe("Failed"); // missing
            expect(reportRows.at(4).find(viewLogSelector).exists()).toBe(false);

            expect(reportRows.at(5).find(statusSelector).text()).toBe("Complete");
            expect(reportRows.at(5).find(viewLogSelector).text()).toBe("View log");
            expect(reportRows.at(6).find(statusSelector).text()).toBe("Failed"); // error
            expect(reportRows.at(6).find(viewLogSelector).text()).toBe("View log");
            expect(reportRows.at(7).find(statusSelector).text()).toBe("Running");
            expect(reportRows.at(7).find(viewLogSelector).text()).toBe("View log");
            expect(reportRows.at(8).find(statusSelector).text()).toBe("Failed"); // interrupted
            expect(reportRows.at(8).find(viewLogSelector).text()).toBe("View log");
            expect(reportRows.at(9).find(statusSelector).text()).toBe("Failed"); // orphan
            expect(reportRows.at(9).find(viewLogSelector).text()).toBe("View log");

            done();
        });
    });

    it(`emits report log dialog report key on click View log link`, (done) => {
        const wrapper = getWrapper()

        setTimeout(async () => {
            const link = wrapper.findAll("tr").at(1).find("a.report-log-link");
            await link.trigger("click");

            expect(wrapper.emitted()["show-report-log"].length).toBe(1)
            expect(wrapper.emitted()["show-report-log"][0][0]).toEqual("preterrestrial_andeancockoftherock")

            done();
        })
    });

    it(`can render parameters column`, (done) => {
        const wrapper = getWrapper(workflowStatus1, workflowSummary)

        setTimeout(() => {
            expect(wrapper.find("table").exists()).toBe(true)
            expect(wrapper.findAll("tr").length).toBe(5)

            const headers = wrapper.findAll("th")
            expect(headers.length).toBe(4)
            expect(headers.at(1).text()).toBe("Parameters")

            const parameterComponents = wrapper.findAllComponents(runWorkflowParameters)
            expect(parameterComponents.length).toBe(4)
            expect(parameterComponents.at(0).props("index")).toBe(0);
            expect(parameterComponents.at(0).props("report")).toBe(workflowSummary.reports[0]);
            expect(parameterComponents.at(1).props("index")).toBe(1);
            expect(parameterComponents.at(1).props("report")).toBe(workflowSummary.reports[1]);
            expect(parameterComponents.at(2).props("index")).toBe(2);
            expect(parameterComponents.at(2).props("report")).toBe(workflowSummary.reports[2]);
            expect(parameterComponents.at(3).props("index")).toBe(3);
            expect(parameterComponents.at(3).props("report")).toBe(workflowSummary.reports[3]);
            done();
        })
    })
})
