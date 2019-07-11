<template>
    <div id="report-readers-global-list">
        <label class="font-weight-bold">
            Global read access
            <a href="#" class="small" data-toggle="tooltip" title="Coming soon!">
                <edit-icon></edit-icon>
                Edit roles</a>
        </label>
        <div>
           <role-list :roles="readers"></role-list>
        </div>
    </div>
</template>

<script>
    import {api} from "../../../utils/api";
    import EditIcon from './editIcon.vue';
    import Vue from "vue";
    import ReportReader from "./reportReader.vue";
    import RoleList from "./roleList.vue";

    export default {
        name: 'globalReadersList',
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
            },
            toggle: function (index, role) {
                Vue.set(this.readers, index, {...role, expanded: !role.expanded});
            }
        },
        components: {
            RoleList,
            ReportReader,
            EditIcon
        }
    };
</script>