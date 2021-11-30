import {RunReportMetadata, RunWorkflowMetadata} from "../js/utils/types";

export const mockRunReportMetadata = (props: Partial<RunReportMetadata> = {}): RunReportMetadata => {
    return {
        metadata: {
            instances_supported: false,
            git_supported: true,
            instances: {"source": []},
            changelog_types: ["published", "internal"]
        },
        git_branches: ["master", "dev"],
        ...props
    }
};

export const mockRunWorkflowMetadata = (props: Partial<RunWorkflowMetadata> = {}): RunWorkflowMetadata => {
    return {
        name: "",
        reports: [],
        instances: {},
        git_branch: null,
        git_commit: null,
        changelog: null,
        ...props
    }
};