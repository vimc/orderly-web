<template>
    <div>
        <div class="row">
            <div class="col-4" v-if="canManage">
                <label class="font-weight-bold">Manage project docs</label>
                <refresh-documents></refresh-documents>
            </div>
        </div>
        <ul v-if="docs.length > 0">
            <li v-for="doc in docs"
                v-bind:id="doc.path"
                v-bind:class="[{'has-children': doc.children.length > 0}, {'open':expanded[doc.path]}]">
                <div v-if="!doc.is_file"
                     class="expander"
                     v-on:click="toggle(doc.path)"></div>
                <span v-if="!doc.is_file"
                      class="folder-name"
                      v-text="doc.display_name"
                      v-on:click="toggle(doc.path)"></span>
                <file v-if="doc.is_file && !doc.external" :doc="doc"></file>
                <web-link v-if="doc.is_file && doc.external" :doc="doc"></web-link>
                <document-list v-show="expanded[doc.path]" :docs="doc.children"></document-list>
            </li>
        </ul>
    </div>
</template>

<script>
    import Vue from "vue";
    import file from "./file.vue";
    import webLink from "./webLink.vue";
    import refreshDocuments from "../admin/refreshDocuments";

    export default {
        name: "document-list",
        props: ["docs", "canManage"],
        data() {
            return {
                expanded: {},
                users: []
            }
        },
        methods: {
            toggle(path) {
                Vue.set(this.expanded, path, !this.expanded[path]);
            }
        },
        components: {
            file,
            webLink,
            refreshDocuments
        }
    }

</script>