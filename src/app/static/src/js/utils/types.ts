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

export interface WorkflowRunSummary {
    name: string,
    key: string,
    email: string,
    date: string
}

export interface WorkflowRunStatus {
    status: "queued" | "running" | "success" | "error" | "cancelled",
    reports: WorkflowRunReportStatus[]
}

export interface WorkflowRunReportStatus {
    name: string,
    key: string,
    status: "queued" | "running" | "success" | "error" | "orphan" | "interrupted" | "deferred" | "impossible" | "missing",
    version: string | null
    date?: string
}

export interface WorkflowSummary {
    date: string,
    email: string,
    key: string,
    name: string
}

export interface WorkflowRunReport {
    workflow_key: string,
    key: string,
    report: string,
    params: Record<string, string>
}

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

export interface RunReportMetadata {
    git_branches: string[],
    metadata: RunReportMetadataDependency
}

export interface RunReportMetadataDependency {
    changelog_types: string[],
    git_supported: boolean,
    instances: Record<string, string[]>,
    instances_supported: boolean
}

export interface ChildCustomStyle {
    label: string,
    control: string
}

export interface Error {
    code: string;
    message: string | null;
}