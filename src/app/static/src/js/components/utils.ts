import {ActionMethod, mapActions} from "vuex";

export const mapActionByName = (namespace: string | null, name: string): ActionMethod => {
    return (!!namespace && mapActions(namespace, [name])[name]) || mapActions([name])[name]
};
