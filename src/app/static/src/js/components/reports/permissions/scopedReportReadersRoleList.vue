<template>
    <div>
        <role-list :roles="readers" :can-remove="true"></role-list>
    </div>
</template>

<script>
    import {api} from "../../../utils/api";
    import RoleList from "./roleList.vue";

    export default {
        name: 'scopedReadersRoleList',
        props: ["report"],
        mounted() {
            this.getReaders();
        },
        data() {
            return {
                readers: []
            }
        },
        methods: {
            getReaders: function () {
                api.get(`/user-groups/report-readers/${this.report}/`)
                    .then(({data}) => {
                        this.readers = data.data
                    })
            }
        },
        components: {
            RoleList
        }
    };
</script>