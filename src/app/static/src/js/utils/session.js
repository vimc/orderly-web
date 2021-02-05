export const session = function () {
    const RUNNING_REPORT_STATUS_PREFIX = "runningReportStatus_";

    const RUNNING_STATUS = "runningStatus";
    const RUNNING_KEY = "runningKey";
    const NEW_VERSION_FROM_RUN = "newVersionFromRun";

    const RUNNING_REPORT_STATUS_KEYS = [RUNNING_STATUS, RUNNING_KEY, NEW_VERSION_FROM_RUN];
    const RUNNING_REPORTS_KEY = "runningReports";

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

    function buildStorageKey(reportName, valueKey) {
        return RUNNING_REPORT_STATUS_PREFIX + reportName + "_" + valueKey;
    }

    return {
        getRunningReportStatus: function (reportName) {
            const result = {};
            RUNNING_REPORT_STATUS_KEYS.forEach(x => result[x] = getItem(buildStorageKey(reportName, x)));
            return result;
        },
        setRunningReportStatus: function (reportName, runningReportStatus) {
            RUNNING_REPORT_STATUS_KEYS.forEach(x => setItem(buildStorageKey(reportName, x), runningReportStatus[x]))
        },
        removeRunningReportStatus: function (reportName) {
            RUNNING_REPORT_STATUS_KEYS.forEach(x => removeItem(buildStorageKey(reportName, x)));
        },
        getRunningReports() {
            return getItem(RUNNING_REPORTS_KEY) ? JSON.parse(getItem(RUNNING_REPORTS_KEY)) : [];
        },
        addRunningReport(key, name, date, instances, params, git_branch, git_commit) {
            setItem(RUNNING_REPORTS_KEY, JSON.stringify(this.getRunningReports().concat({key, name, date, instances, params, git_branch, git_commit})));
        }
    };
}();