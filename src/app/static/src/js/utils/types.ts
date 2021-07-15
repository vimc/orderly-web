export interface Parameter {
    name: string,
    value: string
}

export interface Step {
    name: string,
    component: string
}

export interface ReportWithDate {
    name: string,
    date: Date | null
}

export interface ReportLog{
    email: string,
    date: string,
    report: string,
    instances: Record<string, string> | null,
    params: Record<string, string> | null,
    git_branch: string | null,
    git_commit: string | null,
    status: string | null,
    logs: string | null,
    report_version: string | null
}

export interface RunReportMetadata {
    instances_supported: boolean,
    git_supported: boolean,
    instances: Record<string, string[]>,
    changelog_types: string[]
}

export interface WorkflowMetadata {
    name: string,
    reports: WorkflowReportWithParams[],
    instances: Record<string, string>,
    git_branch: string | null
    git_commit: string | null
}

export interface RunWorkflowMetadata extends WorkflowMetadata {
    changelog: {
        message: string,
        type: string
    } | null
}

export interface WorkflowRun extends WorkflowMetadata {
    date: string
    email: string
    key: string
}

export interface WorkflowReportWithParams {
    name: string,
    params?: Record<string, string>
}

export interface Error {
    response?: {
        data?: {
            errors?: any[]
        }
    }
}

export interface ReportDependency {
    id: string,
    name: string,
    dependencies: ReportDependency[]
}

export interface ReportDependencies {
    direction: "upstream" | "downstream",
    dependency_tree: ReportDependency
}

export interface WorkflowSummary {
    date: string,
    email: string,
    key: string,
    name: string
}

export interface RunReportMetadata {
    git_branches: [],
    metadata: RunReportMetadataDependency
}

export interface RunReportMetadataDependency {
    changelog_types: [] | null,
    git_supported: boolean,
    instances: {} | null,
    instances_supported: boolean
}

export interface ChildCustomStyle {
    label: string,
    control: string
}
