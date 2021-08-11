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

function workflowRunDetailsToMetadata(details: WorkflowRun) {
    return {
        name: details.name,
        instances: details.instances,
        git_branch: details.git_branch,
        git_commit: details.git_commit,
        changelog: null,
        reports: {}
    }
}

/*
export interface WorkflowRun {
    name: string,
    key: string,
    email: string,
    date: string,
    reports: WorkflowRunReport[],
    instances: Record<string, string>,
    git_branch: string | null,
    git_commit: string | null
}

export interface RunWorkflowMetadata {
    name: string,
    reports: WorkflowReportWithParams[],
    instances: Record<string, string>,
    git_branch: string | null,
    git_commit: string | null,
    changelog: {
        message: string,
        type: string
    } | null
}
 */


