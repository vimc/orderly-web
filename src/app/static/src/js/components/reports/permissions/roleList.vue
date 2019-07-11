<template>
    <ul class="list-unstyled roles">
        <li v-for="(role, index) in roles"
            v-bind:id="role.name"
            v-bind:class="['role', {'open':expanded[index]}]"
            v-on:click="toggle(index)">
            <div class="expander"></div>
            <span v-text="role.name" class="role-name"></span>
            <ul class="list-unstyled members report-readers"
                v-on:click="function(e){e.stopPropagation()}"
                v-show="expanded[index]">
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
    import Vue from "vue";
    import ReportReader from "./reportReader.vue";

    export default {
        name: 'roleList',
        props: ["roles", "canRemove"],
        data() {
            return {
                expanded: {}
            }
        },
        methods: {
            toggle: function (index) {
                Vue.set(this.expanded, index, !this.expanded[index]);
            }
        },
        components: {
            ReportReader
        }
    };
</script>