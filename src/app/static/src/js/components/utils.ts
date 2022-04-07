import {mapMutations, MutationMethod} from "vuex";

export const mapMutationByName = (namespace: string | null, name: string): MutationMethod => {
    return (!!namespace && mapMutations(namespace, [name])[name]) || mapMutations([name])[name]
};
