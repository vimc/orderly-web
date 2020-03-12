<template>
    <div>
        <file-icon></file-icon>
        <span>{{doc.display_name}}:</span>
        <a v-if="doc.can_open" :href="openUrl">open</a>
        <span v-if="doc.can_open">/</span>
        <a :href="doc.url">download</a>
    </div>
</template>

<script>
    import fileIcon from "./fileIcon";

    export default {
        props: ["doc"],
        name: "file",
        components: {
            fileIcon
        },
        computed: {
            openUrl() {
                return this.doc.url + "?inline=true"
            }
        }
    }
</script>
