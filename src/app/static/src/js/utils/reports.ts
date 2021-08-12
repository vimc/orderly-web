import {Parameter} from "./types";

export function mapParameterArrayToRecord(params: Parameter[]): Record<string, string> {
    return params.reduce(function (result, param) {
        result[param.name] = param.value;
        return result;
    }, {});
}

export function mapRecordToParameterArray(record: Record<string, string>): Parameter[] {
    return Object.keys(record).map((k: string) => {
        return {name: k, value: record[k]}
    });
}
