<template>
    <div id="report-readers-list">
        <label class="font-weight-bold">
            Global read access
            <a href="#" class="small">
                <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
                     aria-hidden="true"
                     focusable="false"
                     width="0.88em"
                     height="1em"
                     style="-ms-transform: rotate(360deg); -webkit-transform: rotate(360deg); transform: rotate(360deg);"
                     preserveAspectRatio="xMidYMid meet" viewBox="0 0 14 16">
                    <path fill-rule="evenodd"
                          d="M0 12v3h3l8-8-3-3-8 8zm3 2H1v-2h1v1h1v1zm10.3-9.3L12 6 9 3l1.3-1.3a.996.996 0 0 1 1.41 0l1.59 1.59c.39.39.39 1.02 0 1.41z"
                          fill="#007bff"/>
                    <rect x="0" y="0" width="14" height="16" fill="rgba(0, 0, 0, 0)"></rect>
                </svg>
                Edit roles</a>
        </label>
        <div>
            <ul class="list-unstyled roles">
                <li v-for="(role, index) in readers" v-bind:id="role.name"
                    v-bind:class="['role',
                    {'open':role.expanded},
                    {'has-children': role.hasChildren}]"
                    v-on:click="toggle(index, role)">
                    <div class="expander"></div>
                    <span v-text="role.name" class="role-name"></span>
                    <ul class="list-unstyled members report-readers"
                        v-if="role.hasChildren"
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
                api.get(`/user-groups/report-readers/`)
                    .then(({data}) => {
                        this.readers = data.data.map((r) => ({
                            ...r,
                            hasChildren: r.members.length > 0
                        }));
                    })
            },
            toggle: function (index, role) {
                Vue.set(this.readers, index, {...role, expanded: !role.expanded});
            }
        }
    };
</script>