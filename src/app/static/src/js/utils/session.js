export const session = function () {
    const SELECTED_TAB = "runReport";
    const SELECTED_RUNNING_REPORT_KEY = "";

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
        getSelectedTab: function () {
            return getItem(SELECTED_TAB);
        },
        setSelectedTab: function (tab) {
            setItem(SELECTED_TAB, tab);
        },
        getSelectedRunningReportKey: function () {
            return getItem(SELECTED_RUNNING_REPORT_KEY);
        },
        setSelectedRunningReportKey: function (key) {
            setItem(SELECTED_RUNNING_REPORT_KEY, key);
        },
        removeSelectedRunningReportKey: function () {
            removeItem(SELECTED_RUNNING_REPORT_KEY);
        }
    };
}();