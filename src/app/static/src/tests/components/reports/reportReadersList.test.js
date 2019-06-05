import {mount} from '@vue/test-utils';
import ReportReadersList from "../../../js/components/reports/reportReadersList.vue";

describe("reportReadersList", () => {

    it('renders report readers as list', () => {

        const wrapper = mount(ReportReadersList, {
            propsData: {
                report: {
                    name: "report1"
                },
                readers: [
                    {
                        username: "user1",
                        display_name: "User One"
                    },
                    {
                        username: "user2",
                        display_name: "User Two"
                    }
                ]
            }
        });

        expect(wrapper.find('label').text()).toBe("Report readers");
        const listItems = wrapper.findAll('li');
        expect(listItems.length).toBe(2);

        expect(listItems.at(0).find('span').text()).toBe("User One");
        expect(listItems.at(0).find('div').text()).toBe("user1");

        expect(listItems.at(1).find('span').text()).toBe("User Two");
        expect(listItems.at(1).find('div').text()).toBe("user2");
    });

});