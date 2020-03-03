<template>
    <div>
        <file-icon></file-icon>
        <span>{{doc.display_name}}:</span>
        <span v-if="canOpen(doc.path)"><a :href="doc.url + '?inline=true'">open</a>/</span><a :href="doc.url">download</a>
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
        methods: {
            canOpen: (path) => {
                  const parts = path.split(".");
                  return ["pdf", "html", "htm", "bmp", "jpg", "jpeg", "png", "gif", "svg"]
                      .includes(parts[parts.length-1].toLowerCase());
            }
        }
    }
</script>
