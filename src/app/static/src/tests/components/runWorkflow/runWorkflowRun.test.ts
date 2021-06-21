import {mount, shallowMount} from "@vue/test-utils";
import runWorkflowRun from "../../../js/components/runWorkflow/runWorkflowRun.vue";
import {mockAxios} from "../../mockAxios";
import ErrorInfo from "../../../js/components/errorInfo.vue";

describe(`runWorkflowRun`, () => {

    const changelogTypes = ["internal", "public"]
    const source = ["prod", "uat"]
    const runMetadata = {
        git_branches: [],
        metadata: {
            changelog_types: changelogTypes,
            git_supported: false,
            instances_supported: true,
            instances: {
                source: source,
                annex: ["one"]
            }
        }
    }

    const workflowSummaryMetadata = [
        {name: "interim report", date: "2021-05-19T16:28:24Z", email: "test@example.com", key: "fake"},
        {name: "interim report2", date: "2021-06-19T16:28:24Z", email: "test@example.com2", key: "fake2"}
    ]

    beforeEach(() => {
        mockAxios.reset();
        mockAxios.onGet('http://app/report/run-metadata')
            .reply(200, {"data": runMetadata});
        mockAxios.onGet('http://app/workflows')
            .reply(200, {"data": workflowSummaryMetadata});
    })

    const getWrapper = () => {
        return mount(runWorkflowRun,
            {
                propsData: {workflowMetadata: {}},
                data() {
                    return {
                        selectedInstances: {},
                        workflowName: "",
                        runMetadata: null,
                        changeLogTypeValue: "",
                        changeLogMessageValue: "",
                        workflows: [],
                        workflowNameError: ""
                    }
                }
            }
        )
    }

    it(`it renders workflow run page correctly`, () => {
        const wrapper = getWrapper()
        expect(wrapper.find("#run-header").text()).toBe("Run workflow")
    })

    it(`can render workflow-name elements`, (done) => {
        const wrapper = getWrapper()
        setTimeout(async () => {
            const workflowName = wrapper.find("#workflow-name-div")
            expect(workflowName.text()).toBe("Name")
            const workflowNameInput = workflowName.find("input#run-workflow-name")
            expect(workflowNameInput.exists()).toBe(true)

            await workflowNameInput.setValue("New Workflow name")
            const workflowNameValue = workflowNameInput.element as HTMLInputElement
            expect(workflowNameValue.value).toEqual("New Workflow name")
            expect(workflowName.find("small").text())
                .toEqual("")
            done()
        })
    })

    it(`shows error message if workflow name already exists`, async (done) => {
        const wrapper = getWrapper()
        setTimeout(async () => {
            const workflowName = wrapper.find("#workflow-name-div")
            expect(workflowName.text()).toBe("Name")
            const workflowNameInput = workflowName.find("input#run-workflow-name")
            expect(workflowNameInput.exists()).toBe(true)

            await workflowNameInput.setValue("interim report")
            const workflowNameValue = workflowNameInput.element as HTMLInputElement
            expect(workflowNameValue.value).toEqual("interim report")
            expect(workflowName.find("small").text())
                .toEqual("Workflow name already exists, please rename your workflow.")
            done()
        })
    })

    it(`can render workflow-source elements`, async (done) => {
        const wrapper = getWrapper()

        setTimeout(async () => {
            const label = wrapper.findAll("#workflow-instances-div label")
            expect(label.length).toBe(1)
            expect(label.at(0).text()).toBe("Database \"source\"")

            const sourceOptions = wrapper.findAll("#source option");
            expect(sourceOptions.length).toBe(2);
            expect(sourceOptions.at(0).attributes().value).toBe("prod");
            expect(sourceOptions.at(0).text()).toBe("prod");
            expect(sourceOptions.at(1).attributes().value).toBe("uat");
            expect(sourceOptions.at(1).text()).toBe("uat");

            expect(wrapper.find("#annex").exists()).toBe(false); // only 1 option so don't show
            done()
        })
    })

    it(`can select workflow-source`, async (done) => {
        const wrapper = getWrapper()

        setTimeout(async () => {
            const label = wrapper.findAll("#workflow-instances-div label")
            expect(label.length).toBe(1)
            expect(label.at(0).text()).toBe("Database \"source\"")

            expect(wrapper.vm.$data.selectedInstances).toStrictEqual({source: "prod", annex: "one"});

            const selectedOption = wrapper.find("#source");
            await selectedOption.setValue("uat")
            expect(wrapper.vm.$data.selectedInstances).toStrictEqual({source: "uat", annex: "one"});
            const selectedOptionValue = selectedOption.element as HTMLSelectElement
            expect(selectedOptionValue.value).toEqual(source[1])
            done()
        })
    })

    it(`can render workflow-changelog message elements`, async (done) => {
        const wrapper = getWrapper()

        setTimeout(async () => {
            expect(mockAxios.history.get.length).toBe(2);
            expect(mockAxios.history.get[0].url).toBe("http://app/report/run-metadata");
            expect(mockAxios.history.get[1].url).toBe("http://app/workflows");
            expect(wrapper.vm.$data.error).toStrictEqual("")
            expect(wrapper.vm.$data.defaultMessage).toStrictEqual("")
            expect(wrapper.vm.$data.workflows).toStrictEqual(workflowSummaryMetadata)

            const changelog = wrapper.find("#changelog-container")
            const labels = changelog.findAll("label")
            expect(labels.at(0).text()).toBe("Changelog Message")
            expect(labels.at(1).text()).toBe("Changelog Type")
            expect(changelog.find("textarea").exists()).toBe(true)
            expect(changelog.find("select").exists()).toBe(true)
            done()
        })
    })

    it(`can set workflow-changelog message`, async (done) => {
        const wrapper = getWrapper()

        setTimeout(async () => {
            const changelog = wrapper.find("#changelog-message")
            expect(changelog.find("label").text()).toBe("Changelog Message")
            const textarea = changelog.find("textarea")
            expect(changelog.find("textarea").exists()).toBe(true)

            await textarea.setValue("test message input")
            const textAreaValue = textarea.element as HTMLTextAreaElement
            expect(textAreaValue.value).toBe("test message input")
            expect(wrapper.vm.$data.changeLogMessageValue).toEqual("test message input")
            done()
        })
    })

    it(`can render workflow-changelog-type`, async (done) => {
        const wrapper = getWrapper()

        setTimeout(async () => {
            expect(mockAxios.history.get.length).toBe(2);
            expect(mockAxios.history.get[0].url).toBe("http://app/report/run-metadata");
            expect(mockAxios.history.get[1].url).toBe("http://app/workflows");
            expect(wrapper.vm.$data.error).toStrictEqual("")
            expect(wrapper.vm.$data.defaultMessage).toStrictEqual("")
            expect(wrapper.vm.$data.workflows).toStrictEqual(workflowSummaryMetadata)

            const changelogType = wrapper.find("#changelog-type")
            expect(wrapper.find("#changelog-type label").text()).toEqual("Changelog Type")
            const options = changelogType.find("select").findAll("option")
            expect(options.at(0).text()).toBe(changelogTypes[0]);
            expect(options.at(1).text()).toBe(changelogTypes[1]);
            done()
        })
    })

    it(`can select workflow-changelog-type`, async (done) => {
        const wrapper = getWrapper()

        setTimeout(async () => {
            const changelogType = wrapper.find("#changelog-type")
            expect(wrapper.find("#changelog-type label").text()).toEqual("Changelog Type")
            const selectOptions = changelogType.find("select")
            selectOptions.setValue(changelogTypes[1])
            const optionsValue = selectOptions.element as HTMLSelectElement
            expect(optionsValue.value).toEqual(changelogTypes[1])
            expect(wrapper.vm.$data.selectedInstances).toEqual({"annex": "one", "source": "prod"})
            done()
        })
    })

    it(`can render workflow-tags elements`, async () => {
        const wrapper = getWrapper()
        await wrapper.setData({switchV2FeatureComponentOn: true})
        const tags = wrapper.find("#workflow-tags-div")
        expect(tags.text()).toBe("Report version tags")
        expect(tags.find("input#run-workflow-report-version-tags").exists()).toBe(true)
    })

    it(`can render workflow-completion elements`, async () => {
        const wrapper = getWrapper()
        await wrapper.setData({switchV2FeatureComponentOn: true})
        const completion = wrapper.find("#workflow-completion-div")
        expect(completion.find("label").text()).toBe("Only commit reports on workflow completion")
        expect(completion.find("#run-workflow-ticked").exists()).toBe(true)
        expect(completion.find("#run-workflow-ticked p").text()).toBe("ticked")
    })

    it(`it can set and render props correctly`, async (done) => {
        const workflowMeta = {placeholder: "test placeholder"}
        const wrapper = getWrapper()
        await wrapper.setProps({workflowMetadata: workflowMeta})
        setTimeout(() => {
            expect(wrapper.vm.$props.workflowMetadata).toBe(workflowMeta)
            done()
        })
    })

    it("show error message if error getting workflows", (done) => {
        mockAxios.onGet('http://app/workflows')
            .reply(500, "TEST ERROR");

        const wrapper = shallowMount(runWorkflowRun, {
            propsData: {
                workflowMetadata: {}
            }
        });

        setTimeout(() => {
            expect(wrapper.find(ErrorInfo).props("apiError").response.data).toBe("TEST ERROR");
            expect(wrapper.find(ErrorInfo).props("defaultMessage")).toBe("An error occurred while retrieving previously run workflows");
            done();
        })
    });

    it("show error message if error getting run-metadata", (done) => {
        mockAxios.onGet('http://app/report/run-metadata')
            .reply(500, "TEST ERROR");

        const wrapper = shallowMount(runWorkflowRun, {
            propsData: {
                workflowMetadata: {}
            }
        });

        setTimeout(() => {
            expect(wrapper.find(ErrorInfo).props("apiError").response.data).toBe("TEST ERROR");
            expect(wrapper.find(ErrorInfo).props("defaultMessage")).toBe("An error occurred while retrieving data");
            done();
        })
    });
})