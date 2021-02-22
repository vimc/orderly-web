const urlParams = new URLSearchParams(window.location.search);
const reportLog = !urlParams.get('reportLog');
export const switches = {
    reportLog: reportLog
}
