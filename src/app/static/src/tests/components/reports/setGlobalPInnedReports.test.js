import SetGlobalPinnedReports from "../../../js/components/reports/setGlobalPinnedReports.vue";
import {shallowMount} from '@vue/test-utils';

describe("setGlobalPinnedReports", () => {
    const getWrapper = function() {
        return shallowMount(SetGlobalPinnedReports, {
            propsData: {

            }
        });
    }

});