import { RunWorkflowMetadata, WorkflowRun, WorkflowReportWithDependencies } from "./types"

export function reportVersionToLongTimestamp(versionId) {
    const regex = /(\d{4})(\d{2})(\d{2})-(\d{2})(\d{2})(\d{2})-([0-9a-f]{8})/;
    const match = versionId.match(regex);
    if (match) {
        const [, year, month, day, hours, minutes, seconds, hash] = match;

        const date = new Date(`${year}-${month}-${day}T${hours}:${minutes}:${seconds}`);

        return longTimestamp(date)
    } else {
        throw Error(`Unable to parse ${versionId} as version identifier: Did not match regex`);
    }
}

export function longTimestamp(date) {
    const hours = padZero(date.getHours());
    const minutes = padZero(date.getMinutes());
    return `${longDate(date)}, ${hours}:${minutes}`;
}

// We use this format as it is unambiguous between USA and UK
export function longDate(date) {
    return date.toDateString();
}

function padZero(number) {
    // This always sticks a zero on the front and then takes the last two digits
    return ('0' + number).slice(-2);
}

export function workflowRunDetailsToMetadata(details: WorkflowRun): RunWorkflowMetadata {
    return {
        name: details.name,
        instances: details.instances,
        git_branch: details.git_branch,
        git_commit: details.git_commit,
        changelog: null,
        reports: details.reports.map(report => {
            return {name: report.report, params: report.params}
        })
    }
}

export function formatDate(date) {
    return longTimestamp(new Date(date));
}

export const runninReportStates = {
    failStates: ["error", "orphan", "impossible", "missing", "interrupted"],
    notStartedStates: ["queued", "deferred", "impossible", "missing"],
    nonFailStateMessages: {
        "success": "Complete",
        "impossible": "Dependency failed",
        "deferred": "Waiting for dependency"
    }
}

export function interpretStatus(status) {
    const { nonFailStateMessages, failStates } = runninReportStates
    if (Object.keys(nonFailStateMessages).includes(status)) {
        return nonFailStateMessages[status]
    } else if (
        failStates.includes(status)
    ) {
        return "Failed";
    } else {
        return status.charAt(0).toUpperCase() + status.slice(1);
    }
}

export function hasParams(report: WorkflowReportWithDependencies) {
    return (report.param_list && report.param_list.length > 0) ||
        (report.default_param_list && report.default_param_list.length > 0)
}
