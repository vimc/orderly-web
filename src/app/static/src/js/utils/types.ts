export interface Parameter {
    name: string,
    value: string
}

export interface ReportLog{
    email: string,
    date: Date,
    report: string,
    instances: string,
    params: string,
    gitBranch: string,
    gitCommit: string,
    status: string,
    log: string,
    reportVersion: string
}