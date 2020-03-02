<template>
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
            <file v-if="doc.is_file" :doc="doc"></file>
            <document-list v-show="expanded[doc.path]" :docs="doc.children"></document-list>
        </li>
    </ul>
</template>

<script>
    import Vue from "vue";
    import file from "./file.vue";

    export default {
        name: "document-list",
        props: ["docs"],
        data() {
            return {
                expanded: {}
            }
        },
        methods: {
            toggle(path) {
                Vue.set(this.expanded, path, !this.expanded[path]);
            }
        },
        components: {
            file
        }
    }

</script>