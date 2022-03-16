<template>
    <ul class="report-dependency-list">
        <li v-for="dependency in dependencyList"
            :key="dependency.id"
            :class="[{'has-children': hasChildren(dependency)}, {'open':expanded[dependency.id]}]">
            <div v-if="hasChildren(dependency)"
                 class="expander"
                 @click="toggle(dependency.id)"></div>
            <span class="report-dependency-item"
                  @click="toggle(dependency.id)">
                {{ dependency.name }} (<a :href="dependencyLink(dependency)">{{ dependency.id }}</a>)
            </span>
            <report-dependency-list v-if="hasChildren(dependency)"
                                    v-show="expanded[dependency.id]"
                                    :dependency-list="dependency.dependencies"></report-dependency-list>
        </li>
    </ul>
</template>

<script lang="ts">
    import Vue from "vue";
    import {ReportDependency} from "../../utils/types";
    import {buildFullUrl} from "../../utils/api";

    interface Props {
        dependencyList: ReportDependency[]
    }

    interface Data {
        expanded: Record<string, boolean>
    }

    interface Methods {
        dependencyLink: (dep: ReportDependency) => string,
        hasChildren: (dep: ReportDependency) => boolean,
        toggle: (id: string) => void
    }

    export default Vue.extend<Data, Methods, Record<string, never>, Props>({
        name: "ReportDependencyList",
        props: {
            dependencyList: Array
        },
        data() {
            return {
                expanded: {}
            }
        },
        methods: {
            dependencyLink: function (dep: ReportDependency) {
                return buildFullUrl(`/report/${dep.name}/${dep.id}`);
            },
            hasChildren: function(dep: ReportDependency) {
                return dep.dependencies.length > 0;
            },
            toggle(id: string) {
                Vue.set(this.expanded, id, !this.expanded[id]);
            }
        }
    })
</script>

