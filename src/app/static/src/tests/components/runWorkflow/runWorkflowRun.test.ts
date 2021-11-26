import {mount, shallowMount} from "@vue/test-utils";
import runWorkflowRun from "../../../js/components/runWorkflow/runWorkflowRun.vue";
import {mockAxios} from "../../mockAxios";
import ErrorInfo from "../../../js/components/errorInfo.vue";
import Instances from "../../../js/components/runReport/instances.vue";
import Changelog from "../../../js/components/runReport/changeLog.vue";
import {mockEmptyRunWorkflowMetadata} from "../../mocks";

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

    const getWrapper = (propsData: any = {}) => {
        return mount(runWorkflowRun,
            {
                propsData: {
                    workflowMetadata: mockEmptyRunWorkflowMetadata(propsData),
                },
                data() {
                    return {
                        runMetadata: null,
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

    it("emits initial valid event when workflow name is blank", (done) => {
        const wrapper = getWrapper();
        setTimeout(() => {
            expect(wrapper.emitted("valid").length).toBe(1);
            expect(wrapper.emitted("valid")[0][0]).toBe(false);
            done();
        });
    });

    it("emits initial valid event when workflow name matches existing workflow", (done) => {
        const wrapper = getWrapper({
            workflowMetadata: mockEmptyRunWorkflowMetadata({name: "Interim report"})
        });
        setTimeout(() => {
            expect(wrapper.emitted("valid").length).toBe(1);
            expect(wrapper.emitted("valid")[0][0]).toBe(false);
            done();
        });
    });

    it("emits initial valid event when workflow name is valid", (done) => {
        const wrapper = getWrapper({
            workflowMetadata: mockEmptyRunWorkflowMetadata({name: "Interim report3"})
        });
        setTimeout(() => {
            expect(wrapper.emitted("valid").length).toBe(1);
            expect(wrapper.emitted("valid")[0][0]).toBe(true);
            done();
        });
    });

    it(`can render workflow-name elements, and emit update event on name change`, (done) => {
        const wrapper = getWrapper()

        const workflowName = wrapper.find("#workflow-name-div")
        expect(workflowName.text()).toBe("Name")
        const workflowNameInput = workflowName.find("input#run-workflow-name")
        expect(workflowNameInput.exists()).toBe(true)
        setTimeout(async () => {
            await workflowNameInput.setValue("New Workflow name")
            expect(workflowName.find("small").text())
                .toEqual("")
            expect(wrapper.emitted().update.length).toBe(3); // this follows initial changelog and instances updates
            expect(wrapper.emitted().update[2][0]).toStrictEqual({name: "New Workflow name"});

            expect(wrapper.emitted().valid.length).toBe(2);
            expect(wrapper.emitted().valid[1][0]).toBe(true);
            done();
        });
    })

    it(`emits valid false on workflow name change to empty string`, (done) => {
        const wrapper = getWrapper({
            workflowMetadata: {name: "interim report3"}
        });
        setTimeout(async () => {
            const workflowNameInput = wrapper.find("input#run-workflow-name");
            await workflowNameInput.setValue("");

            expect(wrapper.emitted().valid.length).toBe(2);
            expect(wrapper.emitted().valid[1][0]).toBe(false);
            done();
        });
    });

    it(`emits valid false on workflow name change to name of existing workflow`, (done) => {
        const wrapper = getWrapper({
            workflowMetadata: {name: "interim report3"}
        });
        setTimeout(async () => {
            const workflowNameInput = wrapper.find("input#run-workflow-name");
            await workflowNameInput.setValue("interim report");

            expect(wrapper.emitted().valid.length).toBe(2);
            expect(wrapper.emitted().valid[1][0]).toBe(false);
            done();
        });
    });

    it("can display workflow name from metadata", () => {
        const wrapper = getWrapper({
            workflowMetadata: {name: "Test Workflow"}
        });
        expect((wrapper.find("#run-workflow-name").element as HTMLInputElement).value).toBe("Test Workflow");
    });

    it(`shows error message if workflow name already exists`, async (done) => {
        const wrapper = getWrapper()
        setTimeout(async () => {
            const workflowName = wrapper.find("#workflow-name-div")
            expect(workflowName.text()).toBe("Name")
            const workflowNameInput = workflowName.find("input#run-workflow-name")
            expect(workflowNameInput.exists()).toBe(true)

            await workflowNameInput.setValue("interim report")
            expect(wrapper.emitted().update[wrapper.emitted().update.length-1][0])
                .toStrictEqual({name: "interim report"});
            expect(workflowName.find("small").text())
                .toEqual("Workflow name already exists, please rename your workflow.")
            done()
        })
    })

    it(`can render instances elements`, (done) => {
        const wrapper = getWrapper()

        setTimeout(() => {
            expect(wrapper.findComponent(Instances).emitted().selectedValues.length).toBe(1)
            expect(wrapper.findComponent(Instances).emitted().selectedValues[0][0]).toEqual({"annex": "one", "source": "prod"})

            const label = wrapper.findAll("#instances-div label")
            expect(label.length).toBe(1)
            expect(label.at(0).text()).toBe("Database \"source\"")

            const sourceOptions = wrapper.findAll("#source option");
            expect(sourceOptions.length).toBe(2);
            expect(sourceOptions.at(0).attributes().value).toBe("prod");
            expect(sourceOptions.at(0).text()).toBe("prod");
            expect(sourceOptions.at(1).attributes().value).toBe("uat");
            expect(sourceOptions.at(1).text()).toBe("uat");

            expect(wrapper.find("#annex").exists()).toBe(false); // only 1 option so don't show
            done();
        });
    })

    it(`emits update event on select instance`,  (done) => {
        const wrapper = getWrapper()

        setTimeout(async () => {
            const label = wrapper.findAll("#instances-div label")
            expect(label.length).toBe(1)
            expect(label.at(0).text()).toBe("Database \"source\"")

            //expect initial emit on instances + changelog mount
            expect(wrapper.emitted().update.length).toBe(2);
            expect(wrapper.emitted().update[0][0]).toStrictEqual({instances: {source: "prod", annex: "one"}});

            const selectedOption = wrapper.find("#source");
            await selectedOption.setValue("uat")
            expect(wrapper.emitted().update.length).toBe(3);
            expect(wrapper.emitted().update[2][0]).toStrictEqual({instances: {source: "uat", annex: "one"}});
            done();
        });
    })

    it("initialises instances from workflow metadata", (done) => {
        const wrapper = getWrapper({
            workflowMetadata: {
                instances: {source: "uat"}
            }
        });

        setTimeout(() => {
            expect(wrapper.findComponent(Instances).props("initialSelectedInstances")).toStrictEqual({source: "uat"});

            expect(wrapper.emitted().update.length).toBe(2);
            expect(wrapper.emitted().update[0][0]).toStrictEqual({instances: {source: "uat", annex: "one"}});
            done();
        });
    });

    it(`can render workflow-changelog message elements`, async (done) => {
        const wrapper = getWrapper()

        setTimeout(() => {
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

    it(`emits update on changelog message change`, (done) => {
        const wrapper = getWrapper()

        setTimeout(async () => {
            const changelog = wrapper.find("#changelog-message")
            expect(changelog.find("label").text()).toBe("Changelog Message")
            const textarea = changelog.find("textarea")
            expect(changelog.find("textarea").exists()).toBe(true)

            // expect initial emit of default selected type + selected instances
            expect(wrapper.emitted().update.length).toBe(2)
            expect(wrapper.emitted().update[1][0]).toStrictEqual({changelog: null})

            await textarea.setValue("test message input")
            expect(wrapper.emitted().update.length).toBe(3)
            expect(wrapper.emitted().update[2][0]).toStrictEqual({changelog: {message: "test message input", type: "internal"}})
            done()
        })
    });

    it(`changelog message change updates with existing changelog type`, (done) => {
        const wrapper = getWrapper({
            workflowMetadata: {
                changelog: {message: "", type: "public"}
            }
        });

        setTimeout(async () => {
            const changelog = wrapper.find("#changelog-message")
            expect(changelog.find("label").text()).toBe("Changelog Message")
            const textarea = changelog.find("textarea")
            expect(changelog.find("textarea").exists()).toBe(true)

            // expect initial emit of default selected instances
            expect(wrapper.emitted().update.length).toBe(1);

            await textarea.setValue("test message input")
            expect(wrapper.emitted().update.length).toBe(2);
            expect(wrapper.emitted().update[1][0]).toStrictEqual({changelog: {message: "test message input", type: "public"}})
            done()
        })
    });

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

    it(`emits update on changelog type change`, async (done) => {
        const wrapper = getWrapper();

        setTimeout(async() => {
            const changelogType = wrapper.find("#changelog-type")
            expect(wrapper.find("#changelog-type label").text()).toEqual("Changelog Type");

            await wrapper.find("#changelog-message").find("textarea").setValue("test message input")
            await changelogType.find("select").setValue(changelogTypes[1])
            expect(wrapper.emitted().update.length).toBe(4)
            expect(wrapper.emitted().update[3][0]).toStrictEqual({
                changelog: {
                    message: "test message input",
                    type: "public"
                }
            })
            done()
        })
    })

    it(`changelog type change updates with existing changelog message`, (done) => {
        const wrapper = getWrapper({
            workflowMetadata: {
                changelog: {message: "existing message", type: "internal"}
            }
        });

        setTimeout(async () => {
            // expect initial emit of default selected instances
            expect(wrapper.emitted().update.length).toBe(1);

            const changelogType = wrapper.find("#changelog-type")
            expect(wrapper.find("#changelog-type label").text()).toEqual("Changelog Type")
            const selectOptions = changelogType.find("select")
            await selectOptions.setValue(changelogTypes[1]);

            expect(wrapper.emitted().update.length).toBe(2);
            expect(wrapper.emitted().update[1][0]).toStrictEqual({changelog: {message: "existing message", type: "public"}})
            done()
        })
    });

    it(`initialises changelog type and value from workflow metadata`, (done) => {
        const wrapper = getWrapper({
            workflowMetadata: {
                changelog: {
                    message: "Workflow message",
                    type: "public"
                }
            }
        });
        setTimeout(() => {
            expect(wrapper.findComponent(Changelog).props("initialMessage")).toBe("Workflow message");
            expect(wrapper.findComponent(Changelog).props("initialType")).toBe("public");

            expect((wrapper.find("#changelogMessage").element as HTMLTextAreaElement).value).toBe("Workflow message");
            expect((wrapper.find("#changelogType").element as HTMLSelectElement).value).toBe("public");
            done();
        });
    });

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
