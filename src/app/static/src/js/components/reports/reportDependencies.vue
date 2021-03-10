<template>
    <div class="row">
        Dependencies go here!
        {{JSON.stringify(dependencies)}}
    </div>
</template>

<script lang="ts">
    import {api} from "../../utils/api";

    export default {
        name: "reportDependencies",
        props: ['report'],
        data: () => {
            return {
                dependencies: {}
            }
        },
        mounted() {
            const params = {id: this.report.id, direction: "upstream"};
            api.get(`/api/v1/report/${this.report.name}/dependencies/`, {params})
                .then(({data}) => {
                    this.dependencies = data
                })
                .catch((error) => {
                    //TODO
                });
        }
    }
</script>
