import Vue from "vue";
import {mount, shallowMount} from '@vue/test-utils';
import ReportReadersList from "../../../js/components/reports/reportReadersList.vue";
import ErrorInfo from "../../../js/components/errorInfo.vue";
import UserList from "../../../js/components/permissions/userList.vue";
import AddPermission from "../../../js/components/permissions/addPermission.vue";
import {mockAxios} from "../../mockAxios";

describe("reportReadersList", () => {

    const userEmails = [
        "another.user@example.com",
        "test.user@example.com"
    ];

    beforeEach(() => {
        mockAxios.reset();
        mockAxios.onGet('http://app/typeahead/emails/')
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

    function expectWrapperToHaveRenderedReaders(wrapper) {
        const listItems = wrapper.find(UserList);
        expect(listItems.props().users).toEqual(expect.arrayContaining(reportReaders));
    }

    function expectPostDataCorrect(action) {
        const postData = JSON.parse(mockAxios.history.post[0].data);
        expect(postData.name).toBe("reports.read");
        expect(postData.action).toBe(action);
        expect(postData.scope_prefix).toBe("report");
        expect(postData.scope_id).toBe("report1");
    }

    it('renders data', async () => {

        const wrapper = shallowMount(ReportReadersList, {
            propsData: {
                report: {
                    name: "report1"
                }
            }
        });
        wrapper.setData({
            error: "test error",
            defaultMessage: "default error",
            readers: reportReaders
        });

        await Vue.nextTick();

        expect(wrapper.find(AddPermission).props().type).toBe("user");
        expect(wrapper.find(UserList).props().canRemove).toBe(true);
        expect(wrapper.find(ErrorInfo).props().apiError).toBe("test error");
        expect(wrapper.find(ErrorInfo).props().defaultMessage).toBe("default error");

        expectWrapperToHaveRenderedReaders(wrapper);
    });

    it('fetches users and readers on mount', (done) => {
        mockAxios.onGet('http://app/users/report-readers/report1/')
            .reply(200, {"data": reportReaders});

        const wrapper = shallowMount(ReportReadersList, {
            propsData: {
                report: {
                    name: "report1"
                }
            }
        });

        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(2);
            expect(wrapper.find(AddPermission).props().availableUserGroups)
                .toEqual(expect.arrayContaining(userEmails));

            expectWrapperToHaveRenderedReaders(wrapper);

            done();
        });
    });

    it('sets error when fetching readers', (done) => {

        const testError = {test: "something"};
        mockAxios.onGet('http://app/users/report-readers/report1/')
            .reply(500, testError);

        const wrapper = shallowMount(ReportReadersList, {
            propsData: {
                report: {
                    name: "report1"
                }
            }
        });

        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(2);
            expect(wrapper.find(UserList).props().users.length).toBe(0);
            expect(wrapper.find(ErrorInfo).props().defaultMessage).toBe("could not fetch list of users");
            expect(wrapper.find(ErrorInfo).props().apiError.response.data).toStrictEqual(testError);

            done();
        });
    });

    it('refreshes data when added event is emitted', async (done) => {

        mockAxios.onPost(`http://app/users/test.user%40example.com/actions/associate-permission/`)
            .reply(200);

        mockAxios.onGet('http://app/users/report-readers/report1/')
            .reply(200, {"data": reportReaders});

        const wrapper = mount(ReportReadersList, {
            propsData: {
                report: {
                    name: "report1"
                }
            }
        });

        wrapper.setData({allUsers: userEmails});

        await Vue.nextTick();

        wrapper.find("input").setValue('test.user@example.com');
        wrapper.find('button').trigger('click');

        setTimeout(() => {
            expect(wrapper.findAll('.text-danger').length).toBe(0);

            expect(mockAxios.history.post.length).toBe(1);
            expect(mockAxios.history.get.length).toBe(3); //Initial fetch and after added reader

            expectPostDataCorrect("add");
            expectWrapperToHaveRenderedReaders(wrapper);

            done();
        });

    });

    it('removes user and refreshes list of readers', (done) => {

        mockAxios.onPost(`http://app/users/bob/permissions/reports.read/?scopePrefix=report&scopeId=report1`)
            .reply(200);

        mockAxios.onGet('http://app/users/report-readers/report1/')
            .reply(200, {"data": reportReaders});

        const wrapper = shallowMount(ReportReadersList, {
            propsData: {
                report: {
                    name: "report1"
                }
            }
        });

        setTimeout(() => {
            wrapper.find(UserList).vm.$emit("removed", "bob");

            setTimeout(() => {

                expect(mockAxios.history.get.length).toBe(3); //Initial fetch and after removedreader

                expectWrapperToHaveRenderedReaders(wrapper);

                done();
            });
        });

    });

    it('sets error if removing user fails', (done) => {

        mockAxios.onPost("http://app/users/bob/permissions/reports.read/?scopePrefix=report&scopeId=report1")
            .reply(500);

        mockAxios.onGet('http://app/users/report-readers/report1/')
            .reply(200, {"data": reportReaders});

        const wrapper = shallowMount(ReportReadersList, {
            propsData: {
                report: {
                    name: "report1"
                }
            }
        });

        setTimeout(() => {
            wrapper.find(UserList).vm.$emit("removed", "bob");

            setTimeout(() => {
                expect(wrapper.find(ErrorInfo).props("apiError")).toBeDefined();
                expect(wrapper.find(ErrorInfo).props("defaultMessage")).toBe("could not remove bob");
                done();
            });
        });

    });

    it('available users are those that are not already readers', async () => {

        const readers = [{
            email: "test.user@example.com",
            username: "user1",
            display_name: "User One"

        }];

        const emails = [
            "test.user@example.com",
            "another.user@example.com"
        ];

        const wrapper = shallowMount(ReportReadersList, {
            propsData: {
                report: {
                    name: "report1"
                }
            }
        });

        wrapper.setData({allUsers: emails});
        wrapper.setData({readers: readers});

        await Vue.nextTick();

        expect(wrapper.find(AddPermission).props().availableUserGroups.length).toBe(1);
        expect(wrapper.find(AddPermission).props().availableUserGroups[0]).toBe("another.user@example.com");
    });

});