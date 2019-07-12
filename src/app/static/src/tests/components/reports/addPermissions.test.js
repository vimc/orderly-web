import {mount} from '@vue/test-utils';
import ReportReader from "../../../js/components/reports/permissions/reportReader.vue"
import ReportReadersList from "../../../js/components/reports/permissions/scopedReportReadersList.vue";
import AddPermissions from "../../../js/components/reports/permissions/addPermissions.vue";
import {mockAxios} from "../../mockAxios";

describe("reportReadersList", () => {

    const userEmails = [
        "another.user@example.com",
        "test.user@example.com"
    ];

    beforeEach(() => {
        mockAxios.reset();
        mockAxios.onGet('http://app/users/')
            .reply(200, {"data": userEmails});
    });

    const reportReaders = [
        {
            email: "user1@example.com",
            username: "user1",
            display_name: "User One"

        },
        {
            email: "user2@example.com",
            username: "user2",
            display_name: "User Two"
        }
    ];

    it('adds user and emits added event', (done) => {

        mockAxios.onPost(`http://app/user-groups/test.user%40example.com/actions/associate-permission/`)
            .reply(200);

        const wrapper = mount(AddPermissions, {
            propsData: {
                report: {
                    name: "report1"
                },
                placeholder: "email",
                availableItems: userEmails
            }
        });

        wrapper.find("input").setValue('test.user@example.com');
        wrapper.find('button').trigger('click');

        setTimeout(() => {

            expect(wrapper.findAll('.text-danger').length).toBe(0);
            expect(mockAxios.history.post.length).toBe(1);
            expect(wrapper.emitted().added).toBeDefined();

            const postData = JSON.parse(mockAxios.history.post[0].data);
            expect(postData.name).toBe("reports.read");
            expect(postData.action).toBe("add");
            expect(postData.scope_prefix).toBe("report");
            expect(postData.scope_id).toBe("report1");

            done();
        });

    });

    it('add reader shows error and does not refresh reader list', (done) => {
        mockAxios.onPost(`http://app/user-groups/test.user%40example.com/actions/associate-permission/`)
            .reply(500);

        mockAxios.onGet('http://app/users/report-readers/report1/')
            .reply(200, {"data": reportReaders});

        const wrapper = mount(AddPermissions, {
            propsData: {
                report: {
                    name: "report1"
                },
                placeholder: "email",
                availableItems: userEmails
            }
        });

        wrapper.find('input').setValue('test.user@example.com');
        wrapper.find('button').trigger('click');

        setTimeout(() => {
            expect(mockAxios.history.post.length).toBe(1);
            expect(wrapper.find('.text-danger').text()).toBe("Error: could not add test.user@example.com");

            done();
        });
    });

    it('validates that email is one of available users on add', (done) => {
        mockAxios.onGet('http://app/users/report-readers/report1/')
            .reply(200, {"data": reportReaders});

        const wrapper = mount(AddPermissions, {
            propsData: {
                report: {
                    name: "report1"
                },
                availableItems: userEmails,
                placeholder: "email"
            }
        });

        wrapper.find('input').setValue('bad.user@example.com');
        wrapper.find('button').trigger('click');

        setTimeout(() => {
            expect(mockAxios.history.post.length).toBe(0);
            expect(wrapper.find('.text-danger').text()).toBe("Error: You must enter a valid email");
            done();
        });
    });

});