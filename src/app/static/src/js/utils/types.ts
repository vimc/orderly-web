export interface Parameter {
    name: string,
    value: string
}

export type Dict<V> =  {[index: number]: V}
export type Params = Dict<Parameter>

export interface ReportLog{
    email: string,
    date: String,
    report: string,
    instances: string,
    params: Dict<Parameter>,
    git_branch: string,
    git_commit: string,
    status: string,
    log: string,
    report_version: string
}