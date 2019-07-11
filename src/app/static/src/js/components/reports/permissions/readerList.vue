<template>
    <ul class="list-unstyled report-readers">
        <li v-for="reader in readers" v-bind:id="reader.email">
            <report-reader :email="reader.email"
                           :display-name="reader.display_name"
                           :can-remove="canRemove"
                           @remove="$emit('remove', reader.email)"></report-reader>
        </li>
    </ul>
</template>

<script>
    import ReportReader from "./reportReader.vue";

    export default {
        name: "readerList",
        components: {ReportReader},
        props: ["readers", "canRemove"]
    }
</script>