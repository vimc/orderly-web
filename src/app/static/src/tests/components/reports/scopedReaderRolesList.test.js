import {mount} from '@vue/test-utils';
import ScopedReadersList from "../../../js/components/reports/permissions/scopedReportReadersRoleList.vue";
import {mockAxios} from "../../mockAxios";
import RoleList from "../../../js/components/reports/permissions/roleList.vue";

describe("scopedReaderRolesList", () => {

    beforeEach(() => {
        mockAxios.reset();
        mockAxios.onGet('http://app/user-groups/report-readers/r1/')
            .reply(200, {"data": mockRoles});
    });

    const mockRoles = [
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
        }
    ];

    it('renders roles', () => {

        const wrapper = mount(ScopedReadersList, {
            propsData: {
                report: {name: "r1"}
            }
        });

        wrapper.setData({
            readers: mockRoles
        });

        expect(wrapper.find(RoleList).props().roles).toEqual(expect.arrayContaining(mockRoles));
        expect(wrapper.find(RoleList).props().canRemove).toBe(true);

    });

    it('fetches readers on mount', (done) => {

        const wrapper = mount(ScopedReadersList, {
            propsData: {
                report: {name: "r1"}
            }
        });

        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(1);
            expect(wrapper.find(RoleList).props().roles).toEqual(expect.arrayContaining(mockRoles));
            done();
        });
    });

});