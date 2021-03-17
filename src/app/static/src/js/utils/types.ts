export interface Parameter {
    name: string,
    value: string
}

export interface Instances {
    database: string,
    instance: string
}

export type Dict<V> =  {[index: number]: V}

export interface ReportLog{
    email: string,
    date: string,
    report: string,
    instances: Dict<Instances> | {},
    params: Dict<Parameter> | {},
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
