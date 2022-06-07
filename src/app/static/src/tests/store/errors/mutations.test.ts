import {mockErrorState} from "../../mocks";
import {ErrorsMutation, mutations} from "../../../js/store/errors/mutations";

describe("errors mutations", () => {

    const error = [{code: "ERROR", message: "TEST ERROR"}]
    const state = mockErrorState({errors: error})

    it("can set errors", () => {
        mutations[ErrorsMutation.ErrorAdded](state, {payload: error})
        expect(state.errors).toStrictEqual(error)
    })
})