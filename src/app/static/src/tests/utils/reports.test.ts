import {mapParameterArrayToRecord, mapRecordToParameterArray} from "../../js/utils/reports";

describe("Reports utils", () => {
    it("mapParameterArrayToRecord returns expected result", () => {
        const result = mapParameterArrayToRecord([
            {name: "p1", value: "v1"},
            {name: "p2", value: "v2"}
        ]);
        expect(result).toStrictEqual({p1: "v1", p2: "v2"});
    });

    it("mapParameterArrayToRecord handles empty array", () => {
        expect(mapParameterArrayToRecord([])).toStrictEqual({});
    });

    it("mapRecordToParameterArray returns expected result", () => {
        const result = mapRecordToParameterArray({p1: "v1", p2: "v2"});
        expect(result).toStrictEqual([
            {name: "p1", value: "v1"},
            {name: "p2", value: "v2"}
        ]);
    });

    it("mapRecordToParameterArray handles empty record", () => {
        expect(mapRecordToParameterArray({})).toStrictEqual([]);
    });
});
