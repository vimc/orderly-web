<template>
    <ul v-if="docs.length > 0">
        <li v-for="doc in docs"
            :id="doc.path"
            :key="doc.path"
            :class="[{'has-children': doc.children.length > 0}, {'open':expanded[doc.path]}]">
            <div v-if="!doc.is_file"
                 class="expander"
                 @click="toggle(doc.path)"></div>
            <span v-if="!doc.is_file"
                  class="folder-name"
                  @click="toggle(doc.path)"
                  v-text="doc.display_name"></span>
            <file v-if="doc.is_file && !doc.external" :doc="doc"></file>
            <web-link v-if="doc.is_file && doc.external" :doc="doc"></web-link>
            <document-list v-show="expanded[doc.path]" :docs="doc.children"></document-list>
        </li>
    </ul>
</template>

<script>
    import Vue from "vue";
    import file from "./file.vue";
    import webLink from "./webLink.vue";

    export default {
        name: "DocumentList",
        components: {
            file,
            webLink
        },
        props: ["docs", "canManage"],
        data() {
            return {
                expanded: {}
            }
        },
        methods: {
            toggle(path) {
                Vue.set(this.expanded, path, !this.expanded[path]);
            }
        }
    }

</script>