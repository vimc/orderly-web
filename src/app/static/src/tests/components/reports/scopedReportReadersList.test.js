import {mount, shallowMount} from '@vue/test-utils';
import ReportReader from "../../../js/components/reports/permissions/reportReader.vue"
import ReportReadersList from "../../../js/components/reports/permissions/scopedReportReadersList.vue";
import ReaderList from "../../../js/components/reports/permissions/readerList.vue";
import AddPermissions from "../../../js/components/reports/permissions/addPermissions.vue";
import ErrorInfo from "../../../js/components/errorInfo.vue"
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

    function expectWrapperToHaveRenderedReaders(wrapper) {
        const listItems = wrapper.findAll(ReportReader);
        expect(listItems.length).toBe(2);

        expect(listItems.at(0).props().displayName).toBe("User One");
        expect(listItems.at(0).props().email).toBe("user1@example.com");
        expect(listItems.at(0).props().canRemove).toBe(true);

        expect(listItems.at(1).props().displayName).toBe("User Two");
        expect(listItems.at(1).props().email).toBe("user2@example.com");
        expect(listItems.at(1).props().canRemove).toBe(true);
    }

    it('renders data', () => {

        const wrapper = mount(ReportReadersList, {
            propsData: {
                report: {
                    name: "report1"
                }
            }
        });
        wrapper.setData({
            readers: reportReaders
        });

        expect(wrapper.find("input").attributes("placeholder")).toBe("email");
        expect(wrapper.find('button').text()).toBe("Add user");

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
            expect(wrapper.find(AddPermissions).props().availableItems).toEqual(expect.arrayContaining(userEmails));
            expect(wrapper.find(ReaderList).props().readers).toEqual(expect.arrayContaining(reportReaders));

            done();
        });
    });

    it('shows error from response if available when fetching readers', (done) => {

        const error = {"errors": [{"message": "test error message"}]};
        mockAxios.onGet('http://app/users/report-readers/report1/')
            .reply(404, error);

        const wrapper = shallowMount(ReportReadersList, {
            propsData: {
                report: {
                    name: "report1"
                }
            }
        });

        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(2);
            expect(wrapper.find(ReaderList).props().readers.length).toBe(0);

            const errorInfo = wrapper.find(ErrorInfo);
            expect(errorInfo.props().error.response.data).toStrictEqual(error);
            expect(errorInfo.props().defaultMessage).toBe("Could not fetch users");

            done();
        });
    });

    it('remove reader calls associate permission endpoint and refreshes list of readers', (done) => {

        mockAxios.onPost(`http://app/user-groups/user1%40example.com/actions/associate-permission/`)
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

        setTimeout(() => {
            wrapper.find('span.remove-reader').trigger('click');

            setTimeout(() => {
                expect(wrapper.findAll(ErrorInfo).at(1).props().error).toBe(null);

                expect(mockAxios.history.post.length).toBe(1);
                expect(mockAxios.history.get.length).toBe(3); //Initial fetch and after removedreader

                const postData = JSON.parse(mockAxios.history.post[0].data);
                expect(postData.name).toBe("reports.read");
                expect(postData.action).toBe("remove");
                expect(postData.scope_prefix).toBe("report");
                expect(postData.scope_id).toBe("report1");

                expectWrapperToHaveRenderedReaders(wrapper);

                done();
            });
        });

    });

    it('remove reader shows error and does not refresh reader list', (done) => {

        const error = {"errors": [{"message": "test remove reader error message"}]};
        mockAxios.onPost(`http://app/user-groups/user1%40example.com/actions/associate-permission/`)
            .reply(500, error);

        mockAxios.onGet('http://app/users/report-readers/report1/')
            .reply(200, {"data": reportReaders});

        const wrapper = mount(ReportReadersList, {
            propsData: {
                report: {
                    name: "report1"
                }
            }
        });

        setTimeout(() => {
            wrapper.find('span.remove-reader').trigger('click');

            setTimeout(() => {
                expect(mockAxios.history.post.length).toBe(1);
                expect(mockAxios.history.get.length).toBe(2); //Initial fetches only

                expect(wrapper.findAll(ErrorInfo).at(1).props().error.response.data).toStrictEqual(error);
                expect(wrapper.findAll(ErrorInfo).at(1).props().defaultMessage)
                    .toBe("Could not remove user1@example.com");

                done();
            });
        });
    });

    it('available users are those that are not already readers', () => {

        const readers = [{
            email: "test.user@example.com",
            username: "user1",
            display_name: "User One"
        }];

        const userEmails = [
            "another.user@example.com",
            "test.user@example.com"
        ];

        mockAxios.onGet('http://app/users/report-readers/report1/')
            .reply(200, {"data": readers});

        const wrapper = mount(ReportReadersList, {
            propsData: {
                report: {
                    name: "report1"
                }
            }
        });

        wrapper.setData({allUsers: userEmails});
        wrapper.setData({readers: readers});

        const availableItems = wrapper.find(AddPermissions).props().availableItems;
        expect(availableItems.length).toBe(1);
        expect(availableItems[0]).toBe("another.user@example.com");
    });

});