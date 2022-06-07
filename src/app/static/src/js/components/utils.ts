import {ActionMethod, mapActions, mapMutations, mapState, MutationMethod} from "vuex";
import {ComputedWithType, Dict} from "../utils/types";

export const mapActionByName = (namespace: string | null, name: string): ActionMethod => {
    return (!!namespace && mapActions(namespace, [name])[name]) || mapActions([name])[name]
}

export const mapMutationByName = (namespace: string | null, name: string): MutationMethod => {
    return (!!namespace && mapMutations(namespace, [name])[name]) || mapMutations([name])[name]
};

export const mapStateProp = <S, T>(namespace: string | null, func: (s: S) => T): ComputedWithType<T> => {
    return namespace && (mapState<S>(namespace, {prop: (state: S) => func(state)}) as Dict<ComputedWithType<T>>)["prop"]
        || (mapState<S>({prop: (state: S) => func(state)}) as Dict<ComputedWithType<T>>)["prop"]
};

export const mapStatePropByName = <T>(namespace: string | null, name: string): ComputedWithType<T> => {
    return (namespace && mapState(namespace, [name])[name]) || mapState([name])[name]
};
