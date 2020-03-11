<template>
    <div>
        <web-icon></web-icon>
        <a :href="doc.url">{{doc.display_name}}</a>
    </div>
</template>

<script>
    import webIcon from "./webIcon";

    export default {
        props: ["doc"],
        name: "webLink",
        components: {
            webIcon
        }
    }
</script>
