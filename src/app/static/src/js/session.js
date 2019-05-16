export const session = function(){
   const RUNNING_REPORT_STATUS_PREFIX = "runningReportStatus_";

   const RUNNING_STATUS = "runningStatus";
   const RUNNING_KEY = "runningKey";
   const NEW_VERSION_FROM_RUN = "newVersionFromRun";
   const NEW_VERSION_DISPLAY_NAME = "newVersionDisplayName";

   const RUNNING_REPORT_STATUS_KEYS = [RUNNING_STATUS, RUNNING_KEY, NEW_VERSION_FROM_RUN, NEW_VERSION_DISPLAY_NAME];

   function getItem(key) {
       return window.sessionStorage.getItem(key);
   }

   function setItem(key, value) {
       window.sessionStorage.setItem(key, value);
   }

   function removeItem(key) {
       window.sessionStorage.removeItem(key);
   }

    function buildStorageKey(reportName, valueKey) {
        return RUNNING_REPORT_STATUS_PREFIX + reportName + "_" + valueKey;
    }

    return {
       getRunningReportStatus: function(reportName) {
           const result = {};
           RUNNING_REPORT_STATUS_KEYS.forEach(x => result[x] = getItem(buildStorageKey(reportName, x)))
           return result;
       },
       setRunningReportStatus: function(reportName, runningReportStatus) {
           RUNNING_REPORT_STATUS_KEYS.forEach(x => setItem(buildStorageKey(reportName, x), runningReportStatus[x]))
       },
       removeRunningReportStatus: function(reportName) {
           RUNNING_REPORT_STATUS_KEYS.forEach(x => removeItem(buildStorageKey(reportName, x)));
       }
   };
}();