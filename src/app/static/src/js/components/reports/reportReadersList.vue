<template>
    <div id="report-readers-list">
       Report readers for {{ report.name }}
    </div>
</template>

<script>

    export default {
        name: 'reportReadersList',
        props: ['report'],
        data() {
            return {
                error: ""
            }
        },
        methods: {
            add: function () {
            }
        }
    };
</script>