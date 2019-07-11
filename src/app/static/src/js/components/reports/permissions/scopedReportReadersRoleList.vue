<template>
    <div>
        <div class="input-group mb-3">
            <input v-model="new_role" class="form-control form-control-sm" type="text" placeholder="role name" value/>
            <div class="input-group-append">
                <button type="submit" class="btn btn-sm">Add role</button>
            </div>
        </div>
        <role-list :roles="roles" :can-remove="true"></role-list>
    </div>
</template>

<script>
    import {api} from "../../../utils/api";
    import RoleList from "./roleList.vue";

    export default {
        name: 'scopedReadersRoleList',
        props: ["report"],
        mounted() {
            this.getRoles();
        },
        data() {
            return {
                new_role: "",
                roles: []
            }
        },
        methods: {
            getRoles: function () {
                api.get(`/user-groups/report-readers/${this.report.name}/`)
                    .then(({data}) => {
                        this.roles = data.data
                    })
            }
        },
        components: {
            RoleList
        }
    };
</script>