export interface Parameter {
    name: string,
    value: string
}

export interface Instances {
    source: string,
    annexe: string
}

export type Dict<V> =  {[index: number]: V}

export interface ReportLog{
    email: string,
    date: String,
    report: string,
    instances: Dict<Instances>,
    params: Dict<Parameter>,
    git_branch: string,
    git_commit: string,
    status: string,
    logs: string,
    report_version: string
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
