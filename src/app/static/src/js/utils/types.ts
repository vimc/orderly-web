export interface Parameter {
    name: string,
    value: string
}

export interface ReportLog extends Logs{
    gitBranch: string,
    gitCommit: string,
    gitInstance: string,
    status: string,
    reportVersion: string
}

export interface Logs {
    logs: Array<any>
}