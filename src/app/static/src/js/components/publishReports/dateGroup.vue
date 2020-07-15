<template>
    <div>
        <h6>{{date}}</h6>
        <div class="ml-4">
            <report-draft v-for="draft in drafts"
                          :draft="draft"
                          :expand="expand"
                          :collapse="collapse"></report-draft>
        </div>
    </div>
</template>

<script>
    import ReportDraft from "./reportDraft";

    export default {
        name: "dateGroup",
        components: {ReportDraft},
        props: ["date", "drafts", "expand", "collapse"]
    }
</script>
