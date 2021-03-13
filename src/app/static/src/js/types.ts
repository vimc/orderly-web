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
    out_of_date: boolean,
    dependencies: ReportDependency[]
}

export interface ReportDependencies {
    direction: string, //upstream or downstream
    dependency_tree: ReportDependency
}
