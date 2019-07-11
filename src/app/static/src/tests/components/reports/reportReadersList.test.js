import {mount} from '@vue/test-utils';
import ReportReader from "../../../js/components/reports/permissions/reportReader.vue"
import ReportReadersList from "../../../js/components/reports/permissions/reportReadersList.vue";
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
            error: "test error",
            readers: reportReaders
        });

        expect(wrapper.find('label').text()).toBe("Specific read access");

        expect(wrapper.find('input').attributes("placeholder")).toBe("email");
        expect(wrapper.find('button').text()).toBe("Add user");

        expect(wrapper.find('.text-danger').text()).toBe("test error");

        expectWrapperToHaveRenderedReaders(wrapper);
    });

    it('fetches readers on mount', (done) => {
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
            expect(mockAxios.history.get.length).toBe(1);

            expectWrapperToHaveRenderedReaders(wrapper);

            done();
        });
    });

    it('shows default error when fetching readers', (done) => {
        mockAxios.onGet('http://app/users/report-readers/report1/')
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

    it('shows error from response if available when fetching readers', (done) => {
        mockAxios.onGet('http://app/users/report-readers/report1/')
            .reply(404, {"errors": [{"message": "test error message"}]});

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

            expect(wrapper.find('.text-danger').text()).toBe("Error: test error message");

            done();
        });
    });

    it('add reader calls associate permission endpoint and refreshes list of readers', (done) => {

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
                expect(wrapper.findAll('.text-danger').length).toBe(0);

                expect(mockAxios.history.post.length).toBe(1);
                expect(mockAxios.history.get.length).toBe(2); //Initial fetch and after removedreader

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

    it('add reader shows default error and does not refresh reader list', (done) => {
        mockAxios.onPost(`http://app/user-groups/user1%40example.com/actions/associate-permission/`)
            .reply(500);

        mockAxios.onGet('http://app/users/report-readers/report1/')
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

    it('remove reader shows default error and does not refresh reader list', (done) => {
        mockAxios.onPost(`http://app/users/user1%40example.com/actions/associate-permission/`)
            .reply(500);

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
                expect(mockAxios.history.get.length).toBe(1); //Initial fetch only

                expect(wrapper.find('.text-danger').text()).toBe("Error: could not remove reader");

                done();
            });
        });
    });

    it('shows error from response if available on add reader', (done) => {
        mockAxios.onPost(`http://app/user-groups/user1%40example.com/actions/associate-permission/`)
            .reply(500, {"errors": [{"message": "test add reader error message"}]});

        mockAxios.onGet('http://app/users/report-readers/report1/')
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

            expect(wrapper.find('.text-danger').text()).toBe("Error: test add reader error message");

            done();
        });
    });

    it('shows error from response if available on remove reader', (done) => {
        mockAxios.onPost(`http://app/user-groups/user1%40example.com/actions/associate-permission/`)
            .reply(500, {"errors": [{"message": "test remove reader error message"}]});

        mockAxios.onGet('http://app/users/report-readers/report1/')
            .reply(200, {"data": reportReaders});

        const wrapper = mount(ReportReadersList, {
            propsData: {
                report: {
                    name: "report1"
                }
            }
        });

        setTimeout( () => {
            wrapper.find('span.remove-reader').trigger('click');

            setTimeout(() => {
                expect(mockAxios.history.post.length).toBe(1);
                expect(mockAxios.history.get.length).toBe(1); //Initial fetch only

                expect(wrapper.find('.text-danger').text()).toBe("Error: test remove reader error message");

                done();
            });
        });
    });

});