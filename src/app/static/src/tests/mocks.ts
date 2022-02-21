import {GitState, ReportsState, RunReportMetadata, RunWorkflowMetadata} from "../js/utils/types";
import {RunReportRootState} from "../js/store/runReport/store";
import {ActionContext} from "vuex";

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

export const mockGitState = (props: Partial<GitState> = {}): GitState => {
    return {
        metadata: {
            instances_supported: false,
            git_supported: true,
            instances: {"source": []},
            changelog_types: ["published", "internal"]
        },
        gitBranches: ["master", "dev"],
        ...props
    }
}

export const mockReportsState = (props: Partial<ReportsState> = {}): ReportsState => {
    return {
        runnableReports: [],
        ...props
    }
}

export const mockRunReportRootState = (props: Partial<RunReportRootState> = {}): RunReportRootState => {
    return {
        git: mockGitState(),
        reports: mockReportsState(),
        ...props
    }
}

export const mockActionContext = <S, R>(context: Partial<ActionContext<S, R>> = {}): ActionContext<S, R> => {
    return {
        commit: jest.fn(),
        dispatch: jest.fn(),
        state: {} as S,
        rootState: {} as R,
        getters: {},
        rootGetters: {},
        ...context
    }
}
