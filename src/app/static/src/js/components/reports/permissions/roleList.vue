<template>
    <ul class="list-unstyled roles">
        <li v-for="(role, index) in roles"
            v-bind:id="role.name"
            v-bind:class="['role', {'open':role.expanded}]"
            v-on:click="toggle(index, role)">
            <div class="expander"></div>
            <span v-text="role.name" class="role-name"></span>
            <ul class="list-unstyled members report-readers"
                v-on:click="function(e){e.stopPropagation()}"
                v-show="role.expanded">
                <li v-for="member in role.members">
                    <report-reader :email="member.email"
                                   :display-name="member.display_name"
                                   :can-remove="false">
                    </report-reader>
                </li>
            </ul>
        </li>
    </ul>
</template>

<script>
    import EditIcon from './editIcon.vue';
    import Vue from "vue";
    import ReportReader from "./reportReader.vue";

    export default {
        name: 'roleList',
        props: ["roles"],
        methods: {
            toggle: function (index, role) {
                Vue.set(this.roles, index, {...role, expanded: !role.expanded});
            }
        },
        components: {
            ReportReader,
            EditIcon
        }
    };
</script>