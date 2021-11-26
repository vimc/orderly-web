export const session = function () {

    function getItem(key) {
        return window.sessionStorage.getItem(key);
    }

    function setItem(key, value) {
        if (value) {
            window.sessionStorage.setItem(key, value);
        } else {
            removeItem(key);
        }
    }

    function removeItem(key) {
        window.sessionStorage.removeItem(key);
    }

    return {
        getSelectedTab: function (key) {
            return getItem(key);
        },
        setSelectedTab: function (key, value) {
            setItem(key, value);
        },
        getSelectedKey: function (key) {
            return getItem(key);
        },
        setSelectedKey: function (key, value) {
            setItem(key , value);
        }
    };
}();

export const SELECTED_RUNNING_REPORT_TAB = "selectedRunningReportTab";
export const SELECTED_RUNNING_REPORT_KEY = "selectedRunningReportKey";
export const SELECTED_RUNNING_WORKFLOW_TAB = "selectedRunningWorkflowTab";
export const SELECTED_RUNNING_WORKFLOW_KEY = "selectedRunningWorkflowKey";