import {shallowMount} from '@vue/test-utils';
import GlobalReportReadersList from "../../../js/components/reports/globalReportReadersList.vue";
import ErrorInfo from "../../../js/components/errorInfo.vue";
import UserList from "../../../js/components/permissions/userList.vue";
import {mockAxios} from "../../mockAxios";
import Vue from "vue";

describe("globalReportReadersList", () => {

    beforeEach(() => {
        mockAxios.reset();
    });

    const reportReaders = [
        {
            email: "globaluser1@example.com",
            username: "globaluser1",
            display_name: "Global User One"

        },
        {
            email: "globaluser2@example.com",
            username: "globaluser2",
            display_name: "Global User Two"
        }
    ];

    function expectWrapperToHaveRenderedReaders(wrapper) {
        const listItems = wrapper.findComponent(UserList);
        expect(listItems.props().users).toEqual(expect.arrayContaining(reportReaders));
    }

    it('renders data', async () => {

        const wrapper = shallowMount(GlobalReportReadersList);
        wrapper.setData({
            error: "test error",
            defaultMessage: "default error",
            readers: reportReaders
        });

        await Vue.nextTick();

        expect(wrapper.findComponent(UserList).props().canRemove).toBe(false);
        expect(wrapper.findComponent(ErrorInfo).props().apiError).toBe("test error");
        expect(wrapper.findComponent(ErrorInfo).props().defaultMessage).toBe("default error");

        expectWrapperToHaveRenderedReaders(wrapper);
    });

    it('fetches readers on mount', (done) => {
        mockAxios.onGet('http://app/users/report-readers/')
            .reply(200, {"data": reportReaders});

        const wrapper = shallowMount(GlobalReportReadersList);

        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(1);

            expectWrapperToHaveRenderedReaders(wrapper);

            done();
        });
    });

    it('sets error when fetching readers', (done) => {

        const testError = {test: "something"};
        mockAxios.onGet('http://app/users/report-readers/')
            .reply(500, testError);

        const wrapper = shallowMount(GlobalReportReadersList);

        setTimeout(() => {
            expect(mockAxios.history.get.length).toBe(1);
            expect(wrapper.findComponent(UserList).props().users.length).toBe(0);
            expect(wrapper.findComponent(ErrorInfo).props().defaultMessage).toBe("could not fetch list of users");
            expect(wrapper.findComponent(ErrorInfo).props().apiError.response.data).toStrictEqual(testError);

            done();
        });
    });
});