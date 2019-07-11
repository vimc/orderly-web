import {mount} from '@vue/test-utils';
import ReportReadersList from "../../../js/components/reports/reportReadersList.vue";
import {mockAxios} from "../../mockAxios";
import VueBootstrapTypeahead from 'vue-bootstrap-typeahead'

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
        const listItems = wrapper.findAll('li');
        expect(listItems.length).toBe(2);

        expect(listItems.at(0).find('span.reader-display-name').text()).toBe("User One");
        expect(listItems.at(0).find('div').text()).toBe("user1@example.com");
        expect(listItems.at(0).findAll('span.remove-reader').length).toBe(1);

        expect(listItems.at(1).find('span.reader-display-name').text()).toBe("User Two");
        expect(listItems.at(1).find('div').text()).toBe("user2@example.com");
        expect(listItems.at(1).findAll('span.remove-reader').length).toBe(1);
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

    it('fetches users and readers on mount', (done) => {
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
            expect(mockAxios.history.get.length).toBe(2);
            expect(wrapper.find(VueBootstrapTypeahead).props().data).toEqual(expect.arrayContaining(userEmails));

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
            expect(mockAxios.history.get.length).toBe(2);

            const listItems = wrapper.findAll('li');
            expect(listItems.length).toBe(0);

            expect(wrapper.find('.text-danger').text()).toBe("Error: could not fetch list of users");

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
            expect(mockAxios.history.get.length).toBe(2);

            const listItems = wrapper.findAll('li');
            expect(listItems.length).toBe(0);

            expect(wrapper.find('.text-danger').text()).toBe("Error: test error message");

            done();
        });
    });

    it('add reader calls associate permission endpoint and refreshes data', (done) => {

        mockAxios.onPost(`http://app/user-groups/test.user%40example.com/actions/associate-permission/`)
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

        wrapper.find("input").setValue('test.user@example.com');
        wrapper.find('button').trigger('click');

        setTimeout(() => {
            expect(wrapper.findAll('.text-danger').length).toBe(0);

            expect(mockAxios.history.post.length).toBe(1);
            expect(mockAxios.history.get.length).toBe(3); //Initial fetch and after added reader

            const postData = JSON.parse(mockAxios.history.post[0].data);
            expect(postData.name).toBe("reports.read");
            expect(postData.action).toBe("add");
            expect(postData.scope_prefix).toBe("report");
            expect(postData.scope_id).toBe("report1");

            expectWrapperToHaveRenderedReaders(wrapper);
            expect(wrapper.vm.$data["newUser"]).toBe("");

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

    it('add reader shows default error and does not refresh reader list', (done) => {
        mockAxios.onPost(`http://app/user-groups/test.user%40example.com/actions/associate-permission/`)
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

        wrapper.setData({allUsers: userEmails});

        wrapper.find('input').setValue('test.user@example.com');
        wrapper.find('button').trigger('click');

        setTimeout(() => {
            expect(mockAxios.history.post.length).toBe(1);
            expect(mockAxios.history.get.length).toBe(2); //Initial fetches only

            expect(wrapper.find('.text-danger').text()).toBe("Error: could not add user");

            done();
        });
    });

    it('remove reader shows default error and does not refresh reader list', (done) => {
        mockAxios.onPost(`http://app/users/test.user%40example.com/actions/associate-permission/`)
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
                expect(mockAxios.history.get.length).toBe(2); //Initial fetches only

                expect(wrapper.find('.text-danger').text()).toBe("Error: could not remove user");

                done();
            });
        });
    });

    it('shows error from response if available on add reader', (done) => {
        mockAxios.onPost(`http://app/user-groups/test.user%40example.com/actions/associate-permission/`)
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

        wrapper.setData({allUsers: userEmails});

        wrapper.find('input').setValue('test.user@example.com');
        wrapper.find('button').trigger('click');

        setTimeout(() => {
            expect(mockAxios.history.post.length).toBe(1);
            expect(mockAxios.history.get.length).toBe(2); //Initial fetches only

            expect(wrapper.find('.text-danger').text()).toBe("Error: test add reader error message");

            done();
        });
    });

    it('validates that email is one of available users on add', (done) => {
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

        wrapper.find('input').setValue('bad.user@example.com');
        wrapper.find('button').trigger('click');

        setTimeout(() => {
            expect(mockAxios.history.post.length).toBe(0);
            expect(wrapper.find('.text-danger').text()).toBe("You must enter a valid user email");
            done();
        });
    });

    it('available users are those that are not already readers', (done) => {
        
        const readers = [{
            email: "test.user@example.com",
            username: "user1",
            display_name: "User One"

        }];
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

        wrapper.find('input').setValue('test.user@example.com');
        wrapper.find('button').trigger('click');

        setTimeout(() => {
            expect(mockAxios.history.post.length).toBe(0);
            expect(wrapper.find('.text-danger').text()).toBe("You must enter a valid user email");
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

        setTimeout(() => {
            wrapper.find('span.remove-reader').trigger('click');

            setTimeout(() => {
                expect(mockAxios.history.post.length).toBe(1);
                expect(mockAxios.history.get.length).toBe(2); //Initial fetches only

                expect(wrapper.find('.text-danger').text()).toBe("Error: test remove reader error message");

                done();
            });
        });
    });

});