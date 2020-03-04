<template>
    <div>
        <file-icon></file-icon>
        <span>{{doc.display_name}}:</span>
        <span v-if="doc.can_open"><a :href="doc.url + '?inline=true'">open</a>/</span><a :href="doc.url">download</a>
    </div>
</template>

<script>
    import fileIcon from "./fileIcon";

    export default {
        props: ["doc"],
        name: "file",
        components: {
            fileIcon
        }
    }
</script>
