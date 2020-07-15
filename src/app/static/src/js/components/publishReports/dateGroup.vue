<template>
<div>
    <div class="date">{{date}}</div>
    <div class="ml-3">
        <report-draft v-for="draft in drafts" :draft="draft"></report-draft>
    </div>
</div>
</template>

<script>
    import ReportDraft from "./reportDraft";
    export default {
        name: "dateGroup",
        components: {ReportDraft},
        props: ["date", "drafts"]
    }
</script>
