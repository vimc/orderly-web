export const session = function () {
    // const RUNNING_REPORT_STATUS_PREFIX = "runningReportStatus_";

    // const RUNNING_STATUS = "runningStatus";
    // const RUNNING_KEY = "runningKey";
    // const NEW_VERSION_FROM_RUN = "newVersionFromRun";

    // const RUNNING_REPORT_STATUS_KEYS = [RUNNING_STATUS, RUNNING_KEY, NEW_VERSION_FROM_RUN];

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

    // function buildStorageKey(reportName, valueKey) {
    //     return RUNNING_REPORT_STATUS_PREFIX + reportName + "_" + valueKey;
    // }

    return {
        getSelectedTab: function () {
            return getItem(SELECTED_TAB);
        },
        setSelectedTab: function (tab) {
            setItem(SELECTED_TAB, tab);
        },
        // removeSelectedTab: function () {
        //     removeItem(SELECTED_TAB);
        // },
        getSelectedRunningReportKey: function () {
            return getItem(SELECTED_RUNNING_REPORT_KEY);
        },
        setSelectedRunningReportKey: function (key) {
            setItem(SELECTED_RUNNING_REPORT_KEY, key);
        },
        removeSelectedRunningReportKey: function () {
            removeItem(SELECTED_RUNNING_REPORT_KEY);
        }
        // getRunningReportStatus: function (reportName) {
        //     const result = {};
        //     RUNNING_REPORT_STATUS_KEYS.forEach(x => result[x] = getItem(buildStorageKey(reportName, x)));
        //     return result;
        // },
        // setRunningReportStatus: function (reportName, runningReportStatus) {
        //     RUNNING_REPORT_STATUS_KEYS.forEach(x => setItem(buildStorageKey(reportName, x), runningReportStatus[x]))
        // },
        // removeRunningReportStatus: function (reportName) {
        //     RUNNING_REPORT_STATUS_KEYS.forEach(x => removeItem(buildStorageKey(reportName, x)));
        // }
    };
}();