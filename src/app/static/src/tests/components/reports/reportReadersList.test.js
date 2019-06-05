import {mount} from '@vue/test-utils';
import ReportReadersList from "../../../js/components/reports/reportReadersList.vue";
import {mockAxios} from "../../mockAxios";

describe("reportReadersList", () => {

    beforeEach(() => {
        mockAxios.reset();
    });

    const reportReaders =  [
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
        const listItems = wrapper.findAll('li');
        expect(listItems.length).toBe(2);

        expect(listItems.at(0).find('span').text()).toBe("User One");
        expect(listItems.at(0).find('div').text()).toBe("user1@example.com");

        expect(listItems.at(1).find('span').text()).toBe("User Two");
        expect(listItems.at(1).find('div').text()).toBe("user2@example.com");
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
            error: "test error",
            readers: reportReaders
        });

        expect(wrapper.find('label').text()).toBe("Report readers");

        expect(wrapper.find('input').attributes("placeholder")).toBe("user email");
        expect(wrapper.find('button').text()).toBe("Add reader");

        expect(wrapper.find('.text-danger').text()).toBe("test error");

        expectWrapperToHaveRenderedReaders(wrapper);
    });

    it('fetches readers on mount', (done) => {
        mockAxios.onGet('/users/report-readers/report1/')
            .reply(200, {"data": reportReaders});

        const wrapper = mount(ReportReadersList, {
            propsData: {
                report: {
                    name: "report1"
                }
            }
        });

        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(1);

            expectWrapperToHaveRenderedReaders(wrapper);

            done();
        });
    });

    it('shows error when fetching readers', (done) => {
        mockAxios.onGet('/users/report-readers/report1/')
            .reply(500);

        const wrapper = mount(ReportReadersList, {
            propsData: {
                report: {
                    name: "report1"
                }
            }
        });

        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(1);

            const listItems = wrapper.findAll('li');
            expect(listItems.length).toBe(0);

            expect(wrapper.find('.text-danger').text()).toBe("Error: could not fetch list of readers");

            done();
        });
    });

    it('add reader calls associate permission endpoint and refreshes list of readers', (done) => {

        mockAxios.onPost(`/users/user1%40example.com/actions/associate-permission/`)
            .reply(200);

        mockAxios.onGet('/users/report-readers/report1/')
            .reply(200, {"data": reportReaders});

        const wrapper = mount(ReportReadersList, {
            propsData: {
                report: {
                    name: "report1"
                }
            }
        });

        wrapper.find('input').setValue('user1@example.com');
        wrapper.find('button').trigger('click');

        setTimeout(() => {
            expect(wrapper.findAll('.text-danger').length).toBe(0);

            expect(mockAxios.history.post.length).toBe(1);
            expect(mockAxios.history.get.length).toBe(2); //Initial fetch and after added reader

            const postData = JSON.parse(mockAxios.history.post[0].data);
            expect(postData.name).toBe("reports.read");
            expect(postData.action).toBe("add");
            expect(postData.scope_prefix).toBe("report");
            expect(postData.scope_id).toBe("report1");

            expectWrapperToHaveRenderedReaders(wrapper);

            done();
        });

    });

    it('add reader shows error and does not refresh reader list', (done) => {
        mockAxios.onPost(`/users/user1%40example.com/actions/associate-permission/`)
            .reply(500);

        mockAxios.onGet('/users/report-readers/report1/')
            .reply(200, {"data": reportReaders});

        const wrapper = mount(ReportReadersList, {
            propsData: {
                report: {
                    name: "report1"
                }
            }
        });

        wrapper.find('input').setValue('user1@example.com');
        wrapper.find('button').trigger('click');

        setTimeout(() => {
            expect(mockAxios.history.post.length).toBe(1);
            expect(mockAxios.history.get.length).toBe(1); //Initial fetch only

            expect(wrapper.find('.text-danger').text()).toBe("Error: could not add reader");

            done();
        });
    });

});