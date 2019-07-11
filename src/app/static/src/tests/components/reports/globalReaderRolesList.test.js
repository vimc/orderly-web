import {mount} from '@vue/test-utils';
import GlobalReadersList from "../../../js/components/reports/permissions/globalReportReadersRoleList.vue";
import {mockAxios} from "../../mockAxios";
import EditIcon from '../../../js/components/reports/permissions/editIcon.vue';
import RoleList from "../../../js/components/reports/permissions/roleList.vue";

describe("globalReaderRolesList", () => {

    beforeEach(() => {
        mockAxios.reset();
        mockAxios.onGet('http://app/user-groups/report-readers/')
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
            members: [
                {
                    email: "user2@example.com",
                    username: "user2",
                    display_name: "User Two",
                    can_remove: false
                }
            ]
        }
    ];

    it('renders title and roles', () => {

        const wrapper = mount(GlobalReadersList);
        wrapper.setData({
            readers: mockRoles
        });

        expect(wrapper.find('label').text()).toContain("Global read access");
        expect(wrapper.find('label').find("a").text()).toBe("Edit roles");
        expect(wrapper.find('label').findAll(EditIcon).length).toBe(1);

        expect(wrapper.find(RoleList).props().roles).toEqual(expect.arrayContaining(mockRoles));
        expect(wrapper.find(RoleList).props().canRemove).toBe(false);
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