export interface Parameter {
    name: string,
    value: string
}

export interface ReportLog{
    email: string,
    date: String,
    report: string,
    instances: string,
    params: string,
    gitBranch: string,
    gitCommit: string,
    status: string,
    log: string,
    reportVersion: string
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
