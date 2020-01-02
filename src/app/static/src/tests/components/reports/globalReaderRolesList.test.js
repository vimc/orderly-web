import {mount} from '@vue/test-utils';
import GlobalReadersList from "../../../js/components/reports/globalReportReaderRolesList.vue";
import {mockAxios} from "../../mockAxios";
import RoleList from "../../../js/components/permissions/roleList.vue"
import Vue from "vue";

describe("globalReaderRolesList", () => {

    beforeEach(() => {
        mockAxios.reset();
        mockAxios.onGet('http://app/roles/report-readers/')
            .reply(200, {"data": mockRoles});
    });

    const mockRoles =  [
        {
            name: "Funders",
            members: [
                {
                    email: "user1@example.com",
                    username: "user1",
                    display_name: "User One",
                    can_remove: false
                }
            ]
        },
        {
            name: "Science",
            members: []
        }
    ];

    it('renders role list', async () => {

        const wrapper = mount(GlobalReadersList);
        wrapper.setData({
            roles: mockRoles
        });

        await Vue.nextTick();

        expect(wrapper.find(RoleList).props().roles).toEqual(expect.arrayContaining(mockRoles));
        expect(wrapper.find(RoleList).props().canRemoveRoles).toBe(false);
        expect(wrapper.find(RoleList).props().canRemoveMembers).toBe(false);

    });

    it('fetches readers on mount', (done) => {

        const wrapper = mount(GlobalReadersList);

        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(1);
            expect(wrapper.find(RoleList).props().roles).toEqual(expect.arrayContaining(mockRoles));
            done();
        });
    });

});