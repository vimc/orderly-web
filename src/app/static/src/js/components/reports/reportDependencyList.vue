<template>
    <ul v-if="dependencyList.length">
        <li v-for="dependency in dependencyList">
            <div>{{dependency.name}} (<a :href="dependencyLink(dependency)">{{dependency.id}}</a>)</div>
            <report-dependency-list :dependency-list="dependency.dependencies"></report-dependency-list>
        </li>
    </ul>
</template>

<script lang="ts">
    import Vue from "vue";
    import {ReportDependency} from "../../types";

    interface Props {
        dependencyList: ReportDependency[]
    }

    interface Methods {
        dependencyLink: (dep: ReportDependency) => string
    }

    export default Vue.extend<{}, Methods, {}, Props>({
        name: "report-dependency-list",
        props: {
            dependencyList: []
        },
        methods: {
            dependencyLink: function (dep: ReportDependency) {
                return `/report/${dep.name}/${dep.id}`
            }
        }
    })
</script>

