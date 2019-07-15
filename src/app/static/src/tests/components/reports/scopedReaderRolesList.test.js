import {mount} from '@vue/test-utils';
import ScopedReaderRoleList from "../../../js/components/reports/scopedReportReaderRolesList.vue";
import {mockAxios} from "../../mockAxios";
import RoleList from "../../../js/components/permissions/roleList.vue"
import AddPermission from "../../../js/components/permissions/addPermission.vue";

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

    it('renders role list', () => {

        const wrapper = getSut();
        wrapper.setData({
            currentRoles: mockRoles
        });

        expect(wrapper.find(RoleList).props().roles).toEqual(expect.arrayContaining(mockRoles));
        expect(wrapper.find(RoleList).props().canRemoveRoles).toBe(true);
        expect(wrapper.find(RoleList).props().canRemoveMembers).toBe(false);

    });

    it('renders add permission component', () => {

        const wrapper = getSut();

        wrapper.setData({
            currentRoles: mockRoles,
            allRoles: mockRoleNames
        });

        const expectedPermission = {
            name: "reports.read",
            scope_prefix: "report",
            scope_id: "report1"
        };

        expect(wrapper.find(AddPermission).props().type).toBe("role");
        expect(wrapper.find(AddPermission).props().permission).toStrictEqual(expectedPermission);
        expect(wrapper.find(AddPermission).props().availableUserGroups.length).toBe(2);
        expect(wrapper.find(AddPermission).props().availableUserGroups)
            .toEqual(expect.arrayContaining(["Tech", "Admin"]));

    });

    it('refreshes data when added event is emitted', (done) => {

        mockAxios.onPost(`http://app/user-groups/Tech/actions/associate-permission/`)
            .reply(200);

        mockAxios.onGet('http://app/roles/report-readers/report1/')
            .reply(200, {"data": mockRoles});

        const wrapper = getSut();

        wrapper.setData({allRoles: mockRoleNames});

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

    it('refreshes data when removed event is emitted', (done) => {

        mockAxios.onPost(`http://app/user-groups/Tech/actions/associate-permission/`)
            .reply(200);

        mockAxios.onGet('http://app/roles/report-readers/report1/')
            .reply(200, {"data": mockRoles});

        const wrapper = getSut();

        wrapper.setData({allRoles: mockRoleNames});

        wrapper.find(RoleList).vm.$emit("removed");

        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(3); //Initial fetch and after added reader
            done();
        });

    });

    it('fetches all and current roles on mount', (done) => {

        const wrapper = getSut();

        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(2);
            expect(wrapper.find(RoleList).props().roles).toEqual(expect.arrayContaining(mockRoles));
            expect(wrapper.find(AddPermission).props().availableUserGroups)
                .toEqual(expect.arrayContaining(["Tech", "Admin"]));

            done();
        });
    });

});