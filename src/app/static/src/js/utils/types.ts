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
    name: string
    date: string
    email: string
    reports: Record<string, any>[],
    instances: Record<string, string>,
    git_branch: string | null
    git_commit: string | null
    key: string
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

export interface ChangelogStyle {
    label: { size: number, justify: string },
    control: { size: number }
}