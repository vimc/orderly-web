

export interface Parameters {
    report_version: string,
    name: string,
    type: string,
    value: string | number | boolean
}

export interface Report {
    name: string,
    latest: string
}
