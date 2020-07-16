<template>
<div>
    <h6>{{date}}</h6>
    <div class="ml-4">
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
