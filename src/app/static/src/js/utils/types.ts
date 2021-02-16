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