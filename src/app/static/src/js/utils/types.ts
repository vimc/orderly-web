export interface Parameter {
    name: string,
    value: string
}

export interface ReportLog{
    email: string,
    date: string,
    report: string,
    instances: Map<string, string> | null,
    params: Map<string, string> | null,
    git_branch: string | null,
    git_commit: string | null,
    status: string | null,
    logs: string | null,
    report_version: string | null
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
