import {mockAxios} from "../../mockAxios";
import {shallowMount} from "@vue/test-utils";
import ReportDependencies from "../../../js/components/reports/reportDependencies";
import ReportDependencyList from "../../../js/components/reports/reportDependencyList";
import ErrorInfo from "../../../js/components/errorInfo";

describe("reportDependencies", () => {
    beforeEach(() => {
        mockAxios.reset();
    });

    const url = 'http://app/report/test-name/dependencies/';

    const getWrapper = function() {
        return shallowMount(ReportDependencies, {
            propsData: {
                report: {
                    name: "test-name",
                    id: "test-version"
                }
            }
        });
    };

    it("fetches and renders dependencies", async (done) => {
        const testDeps = {
          direction: "upstream",
          dependency_tree: {
              name: "test-name",
              version: "test-version",
              dependencies: [
                  {
                      name: "dep-name-1",
                      version: "dep-version-1",
                      dependencies: []
                  },
                  {
                      name: "dep-name-2",
                      version: "dep-version-2",
                      dependencies: []
                   }
              ]
          }
        };
        mockAxios.onGet(url)
            .reply(200, {"data": testDeps});

        const wrapper = getWrapper();
        setTimeout(() => {
            expect(mockAxios.history.get[0].params).toStrictEqual({id: "test-version", direction: "upstream"});

            expect(wrapper.find("hr").exists()).toBe(true);
            expect(wrapper.find("h4").text()).toBe("Dependencies");
            const listComponent = wrapper.findComponent(ReportDependencyList)
            expect(listComponent.props("dependencyList")).toStrictEqual(testDeps.dependency_tree.dependencies);
            expect(wrapper.findComponent(ErrorInfo).exists()).toBe(false);
            done();
        });

    });

    it("renders nothing if no child dependencies", async (done) => {
        const testDeps = {
            direction: "upstream",
            dependency_tree: {
                name: "test-name",
                version: "test-version",
                out_of_date: false,
                dependencies: []
            }
        };
        mockAxios.onGet(url)
            .reply(200, {"data": testDeps});

        const wrapper = getWrapper();
        setTimeout(() => {
            expect(wrapper.find("*").exists()).toBe(false);
            done();
        });
    });

    it("renders error", async (done) => {
        mockAxios.onGet(url)
            .reply(500, "test-error");

        const wrapper = getWrapper();
        setTimeout(() => {
            const errorInfo = wrapper.findComponent(ErrorInfo);
            expect(errorInfo.props("apiError").response.data).toBe("test-error");
            expect(errorInfo.props("defaultMessage")).toBe("Could not load report dependencies");
            expect(wrapper.find("hr").exists()).toBe(true);
            expect(wrapper.find("h4").exists()).toBe(false);
            expect(wrapper.findComponent(ReportDependencyList).exists()).toBe(false);
            done();
        });
    });
});
