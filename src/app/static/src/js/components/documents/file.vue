<template>
    <div>
        <file-icon v-if="!doc.external"></file-icon>
        <web-icon v-if="doc.external"></web-icon>
        <span>{{doc.display_name}}:</span>
        <a v-if="doc.can_open" :href="openUrl">open</a>
        <span v-if="canOpenAndDownload">/</span>
        <a v-if="!doc.external" :href="doc.url">download</a>
    </div>
</template>

<script>
    import fileIcon from "./fileIcon";
    import webIcon from "./webIcon";

    export default {
        props: ["doc"],
        name: "file",
        components: {
            fileIcon,
            webIcon
        },
        computed: {
            openUrl() {
                return this.doc.external ? this.doc.url : this.doc.url + "?inline=true"
            },
            canOpenAndDownload() {
                return this.doc.can_open && !this.doc.external
            }
        }
    }
</script>
