<template>
    <ul v-if="docs.length > 0">
        <li v-for="doc in docs"
            v-bind:id="doc.path"
            v-bind:class="[{'has-children': doc.children.length > 0}, {'open':expanded[doc.path]}]">
            <div v-if="!doc.isFile" class="expander" v-on:click="toggle(doc.path)"></div>
            <span v-if="!doc.isFile" v-text="doc.displayName" v-on:click="toggle(doc.path)"></span>
            <span v-if="doc.isFile">{{doc.displayName}}:
                <a :href="doc.url + '?inline'">open</a>/<a :href="doc.url">download</a>
            </span>
            <ul v-if="doc.children.length > 0" v-show="expanded[doc.path]">
                <li v-for="child in doc.children">
                    <document-list :docs="doc.children"></document-list>
                </li>
            </ul>
        </li>
    </ul>
</template>

<script>
    import Vue from "vue";

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
        }
    }

</script>