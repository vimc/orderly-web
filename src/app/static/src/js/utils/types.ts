export interface Parameter {
    name: string,
    value: string
}

export interface ReportLog{
    email: string,
    date: String,
    report: string,
    instances: string,
    params: Object,
    git_branch: string,
    git_commit: string,
    status: string,
    log: string,
    report_version: string
}