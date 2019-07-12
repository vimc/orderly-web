<template>
    <div id="report-readers-global-list">
        <label class="font-weight-bold">
            Global read access
            <a href="#" class="small" data-toggle="tooltip" title="Coming soon!">
                <edit-icon></edit-icon>
                Edit roles</a>
        </label>
        <div>
            <ul class="list-unstyled roles">
                <li v-for="(role, index) in readers" v-bind:id="role.name"
                    v-bind:class="['role',
                    {'open':role.expanded}]"
                    v-on:click="toggle(index, role)">
                    <div class="expander"></div>
                    <span v-text="role.name" class="role-name"></span>
                    <ul class="list-unstyled members report-readers"
                        v-show="role.expanded">
                        <li v-for="member in role.members">
                            <span class="reader-display-name">{{member.display_name}}</span>
                            <div class="text-muted small email">{{member.email}}</div>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>
    </div>
</template>

<script>
    import {api} from "../../utils/api";
    import EditIcon from './editIcon.vue';
    import Vue from "vue";

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
                api.get(`/roles/report-readers/`)
                    .then(({data}) => {
                        this.readers = data.data
                    })
            },
            toggle: function (index, role) {
                Vue.set(this.readers, index, {...role, expanded: !role.expanded});
            }
        },
        components: {
            EditIcon
        }
    };
</script>