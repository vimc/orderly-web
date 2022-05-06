import {RunReportMetadata, RunWorkflowMetadata, GitCommit} from "../js/utils/types";
import {GitState} from "../js/store/git/git";
import {RunReportRootState} from "../js/store/runReport/store";
import {ActionContext} from "vuex";

export type RecursivePartial<T> = {
    [P in keyof T]?:
    T[P] extends (infer U)[] ? RecursivePartial<U>[] :
        T[P] extends object ? RecursivePartial<T[P]> :
            T[P];
};

export const mockRunReportMetadata = (props: RecursivePartial<RunReportMetadata> = {}): RunReportMetadata => {
    return {
        git_branches: ["master", "dev"],
        ...props,
        metadata: {
            instances_supported: false,
            git_supported: true,
            instances: {"source": []},
            changelog_types: ["published", "internal"],
            ...props.metadata
        }
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

export const mockCommit = (props: Partial<GitCommit> = {}): GitCommit => {
    const date = new Date()
    return {
        id: "id",
        date_time: date.toTimeString(),
        age: 10,
        ...props
    }
};

export const mockGitState = (props: RecursivePartial<GitState> = {}): GitState => {
    return {
        branches: ["master", "dev"],
        selectedBranch: "",
        selectedCommit: "",
        ...props,
        commits: props?.commits && props.commits !== null ? props.commits.map((c) => mockCommit(c)) : [],
        metadata: props.metadata !== null ? {
            instances_supported: false,
            git_supported: true,
            instances: {"source": []},
            changelog_types: ["published", "internal"],
            ...props.metadata
        } : null
    }
}

export const mockRunReportRootState = (props: RecursivePartial<RunReportRootState> = {}): RunReportRootState => {
    return {
        selectedTab: "RunReport",
        ...props,
        git: mockGitState(props.git)
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
