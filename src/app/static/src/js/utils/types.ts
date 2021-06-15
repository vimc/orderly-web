export interface Parameter {
    name: string,
    value: string
}

export interface Step {
    name: string,
    component: string
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
    placeholder: string
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
    vemail: string,
    date: string
}

export interface WorkflowRunStatus {
    status: string,
    reports: WorkflowRunReportStatus[]
}

export interface WorkflowRunReportStatus {
    name: string,
    key: string,
    status: string,
    version: string | null
    date?: string
}