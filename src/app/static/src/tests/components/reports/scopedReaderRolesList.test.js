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

        expect(wrapper.findComponent(RoleList).props().roles).toEqual(expect.arrayContaining(mockRoles));
        expect(wrapper.findComponent(RoleList).props().canRemoveRoles).toBe(true);
        expect(wrapper.findComponent(RoleList).props().canRemoveMembers).toBe(false);
        expect(wrapper.findComponent(RoleList).props().permission).toStrictEqual({
            name: "reports.read",
            scope_id: "report1",
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

        expect(wrapper.findComponent(ErrorInfo).props().apiError).toBe("test-error");
        expect(wrapper.findComponent(ErrorInfo).props().defaultMessage).toBe("test-default");
    });

    it('renders add report reader component', async () => {

        const wrapper = getSut();

        wrapper.setData({
            currentRoles: mockRoles,
            allRoles: mockRoleNames
        });

        await Vue.nextTick();

        expect(wrapper.findComponent(AddReportReader).props().type).toBe("role");
        expect(wrapper.findComponent(AddReportReader).props().reportName).toBe("report1");
        expect(wrapper.findComponent(AddReportReader).props().availableUserGroups.length).toBe(2);
        expect(wrapper.findComponent(AddReportReader).props().availableUserGroups)
            .toEqual(expect.arrayContaining(["Tech", "Admin"]));

    });

    it('refreshes data when added event is emitted', async () => {

        mockAxios.onPost(`http://app/roles/Tech/permissions/`)
            .reply(200);

        mockAxios.onGet('http://app/roles/report-readers/report1/')
            .reply(200, {"data": mockRoles});

        const wrapper = getSut();

        wrapper.setData({allRoles: mockRoleNames});

        await Vue.nextTick();

        await wrapper.find("input").setValue('Tech');
        await wrapper.find('button').trigger('click');

        await Vue.nextTick();

        expect(wrapper.findAll('.text-danger').length).toBe(0);

        expect(mockAxios.history.post.length).toBe(1);
        expect(mockAxios.history.get.length).toBe(3); //Initial fetch and after added reader

        expectPostDataCorrect("add");
    });

    it('fetches all and current roles on mount', (done) => {

        const wrapper = getSut();

        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(2);
            expect(wrapper.findComponent(RoleList).props().roles).toEqual(expect.arrayContaining(mockRoles));
            expect(wrapper.findComponent(AddReportReader).props().availableUserGroups)
                .toEqual(expect.arrayContaining(["Tech", "Admin"]));

            done();
        });
    });

    it('removes role and gets current roles when role list emits removed event', async () => {

        const url = 'http://app/roles/Funders/permissions/reports.read/?scopePrefix=report&scopeId=report1';
        mockAxios.onDelete(url)
            .reply(200);

        const wrapper = getSut();

        await Vue.nextTick();

        expect(mockAxios.history.get.length).toBe(2);

        await wrapper.findComponent(RoleList).vm.$emit("removed", "Funders");

        await Vue.nextTick();

        expect(mockAxios.history.delete.length).toBe(1);
        expect(mockAxios.history.delete[0].url).toBe(url);

        //Check roles were refreshed
        expect(mockAxios.history.get.length).toBe(3);
        expect(mockAxios.history.get[2].url).toBe("http://app/roles/report-readers/report1/");
    });

    it('sets error if removing role fails', (done) => {
        const url = 'http://app/roles/Funders/permissions/reports.read/?scopePrefix=report&scopeId=report1';
        mockAxios.onDelete(url)
            .reply(500, "TEST ERROR");

        const wrapper = getSut();

        wrapper.findComponent(RoleList).vm.$emit("removed", "Funders");

        setTimeout(() => {

            expect(wrapper.vm.$data.error.response.data).toBe("TEST ERROR");
            expect(wrapper.vm.$data.defaultMessage).toBe("could not remove Funders");
            done();
        });

    });
});