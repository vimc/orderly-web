import {mount, shallowMount} from '@vue/test-utils';
import ScopedReaderRoleList from "../../../js/components/reports/scopedReportReaderRolesList.vue";
import {mockAxios} from "../../mockAxios";
import RoleList from "../../../js/components/permissions/roleList.vue"
import AddReportReader from "../../../js/components/permissions/addReportReader.vue";
import ErrorInfo from "../../../js/components/errorInfo";
import Vue from "vue";

describe("scopedReaderRolesList", () => {

    beforeEach(() => {
        mockAxios.reset();
        mockAxios.onGet('http://app/roles/report-readers/report1/')
            .reply(200, {"data": mockRoles});

        mockAxios.onGet('http://app/typeahead/roles/')
            .reply(200, {"data": mockRoleNames});
    });

    const mockRoleNames = ["Funders", "Science", "Tech", "Admin"];

    const mockRoles = [
        {
            name: "Funders",
            members: [
                {
                    email: "user1@example.com",
                    username: "user1",
                    display_name: "User One"
                }
            ]
        },
        {
            name: "Science",
            members: []
        }
    ];

    function expectPostDataCorrect(action) {
        const postData = JSON.parse(mockAxios.history.post[0].data);
        expect(postData.name).toBe("reports.read");
        expect(postData.action).toBe(action);
        expect(postData.scope_prefix).toBe("report");
        expect(postData.scope_id).toBe("report1");
    }

    function getSut() {
        return mount(ScopedReaderRoleList, {
            propsData: {
                report: {
                    name: "report1"
                }
            }
        });
    }

    it('renders role list', async () => {

        const wrapper = getSut();
        wrapper.setData({
            currentRoles: mockRoles
        });

        await Vue.nextTick();

        expect(wrapper.find(RoleList).props().roles).toEqual(expect.arrayContaining(mockRoles));
        expect(wrapper.find(RoleList).props().canRemoveRoles).toBe(true);
        expect(wrapper.find(RoleList).props().canRemoveMembers).toBe(false);
        expect(wrapper.find(RoleList).props().permission).toStrictEqual({
            name: "reports.read",
            scope_id : "report1",
            scope_prefix: "report"
        });

    });

    it('renders errorInfo', async () => {
        const wrapper = shallowMount(ScopedReaderRoleList, {
            propsData: {
                report: {
                    name: "report1"
                }
            }
        });

        wrapper.setData({
            error: "test-error",
            defaultMessage: "test-default"
        });

        await Vue.nextTick();

        expect(wrapper.find(ErrorInfo).props().apiError).toBe("test-error");
        expect(wrapper.find(ErrorInfo).props().defaultMessage).toBe("test-default");
    });

    it('renders add report reader component', async () => {

        const wrapper = getSut();

        wrapper.setData({
            currentRoles: mockRoles,
            allRoles: mockRoleNames
        });

        await Vue.nextTick();

        expect(wrapper.find(AddReportReader).props().type).toBe("role");
        expect(wrapper.find(AddReportReader).props().reportName).toBe("report1");
        expect(wrapper.find(AddReportReader).props().availableUserGroups.length).toBe(2);
        expect(wrapper.find(AddReportReader).props().availableUserGroups)
            .toEqual(expect.arrayContaining(["Tech", "Admin"]));

    });

    it('refreshes data when added event is emitted', async (done) => {

        mockAxios.onPost(`http://app/roles/Tech/permissions/`)
            .reply(200);

        mockAxios.onGet('http://app/roles/report-readers/report1/')
            .reply(200, {"data": mockRoles});

        const wrapper = getSut();

        wrapper.setData({allRoles: mockRoleNames});

        await Vue.nextTick();

        wrapper.find("input").setValue('Tech');
        wrapper.find('button').trigger('click');

        setTimeout(() => {
            expect(wrapper.findAll('.text-danger').length).toBe(0);

            expect(mockAxios.history.post.length).toBe(1);
            expect(mockAxios.history.get.length).toBe(3); //Initial fetch and after added reader

            expectPostDataCorrect("add");

            done();
        });

    });

    it('fetches all and current roles on mount', (done) => {

        const wrapper = getSut();

        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(2);
            expect(wrapper.find(RoleList).props().roles).toEqual(expect.arrayContaining(mockRoles));
            expect(wrapper.find(AddReportReader).props().availableUserGroups)
                .toEqual(expect.arrayContaining(["Tech", "Admin"]));

            done();
        });
    });

    it('removes role and gets current roles when role list emits removed event', async (done) => {

        const url = 'http://app/roles/Funders/permissions/reports.read/?scopePrefix=report&scopeId=report1';
        mockAxios.onDelete(url)
            .reply(200);

        const wrapper = getSut();

        await Vue.nextTick();

        expect(mockAxios.history.get.length).toBe(2);

        wrapper.find(RoleList).vm.$emit("removed", "Funders");

        setTimeout(() => {
            expect(mockAxios.history.delete.length).toBe(1);
            expect(mockAxios.history.delete[0].url).toBe(url);

            //Check roles were refreshed
            expect(mockAxios.history.get.length).toBe(3);
            expect(mockAxios.history.get[2].url).toBe("http://app/roles/report-readers/report1/");
            done();
        })
    });

    it('sets error if removing role fails', (done) => {
        const url = 'http://app/roles/Funders/permissions/reports.read/?scopePrefix=report&scopeId=report1';
        mockAxios.onDelete(url)
            .reply(500, "TEST ERROR");

        const wrapper = getSut();

        wrapper.find(RoleList).vm.$emit("removed", "Funders");

        setTimeout(() => {

            expect(wrapper.vm.$data.error.response.data).toBe("TEST ERROR");
            expect(wrapper.vm.$data.defaultMessage).toBe("could not remove Funders");
            done();
        });

    });
});