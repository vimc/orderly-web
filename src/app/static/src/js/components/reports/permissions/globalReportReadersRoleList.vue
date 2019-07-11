<template>
    <div id="report-readers-global-list">
        <label class="font-weight-bold">
            Global read access
            <a href="#" class="small" data-toggle="tooltip" title="Coming soon!">
                <edit-icon></edit-icon>
                Edit roles
            </a>
        </label>
        <div>
           <role-list :roles="readers" :can-remove="false"></role-list>
        </div>
    </div>
</template>

<script>
    import {api} from "../../../utils/api";
    import EditIcon from './editIcon.vue';
    import ReportReader from "./reportReader.vue";
    import RoleList from "./roleList.vue";

    export default {
        name: 'globalReadersRoleList',
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
                api.get(`/user-groups/report-readers/`)
                    .then(({data}) => {
                        this.readers = data.data
                    })
            }
        },
        components: {
            RoleList,
            ReportReader,
            EditIcon
        }
    };
</script>