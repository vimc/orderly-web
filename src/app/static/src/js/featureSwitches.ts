const urlParams = new URLSearchParams(window.location.search);
const workFlowReport = !!urlParams.get('workFlowReport');

export const switches = {
    workFlowReport
};