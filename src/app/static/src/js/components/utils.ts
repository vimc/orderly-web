import {ActionMethod, mapActions, mapMutations, MutationMethod} from "vuex";

export const mapActionByName = (namespace: string | null, name: string): ActionMethod => {
    return (!!namespace && mapActions(namespace, [name])[name]) || mapActions([name])[name]
}

export const mapMutationByName = (namespace: string | null, name: string): MutationMethod => {
    return (!!namespace && mapMutations(namespace, [name])[name]) || mapMutations([name])[name]
};
