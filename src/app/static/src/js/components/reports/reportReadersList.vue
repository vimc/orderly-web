<template>
    <div id="report-readers-list">
        <label class="font-weight-bold">Report readers</label>
        <div>
            <ul class="list-unstyled report-readers">
                <li v-for="reader in readers">
                    <span>{{reader.display_name}}</span>
                    <div class="text-muted small" style="margin-top: -1rem">{{reader.username}}</div>
                </li>
            </ul>
        </div>
    </div>
</template>

<script>

    export default {
        name: 'reportReadersList',
        props: ['report', 'readers'],
        data() {
            return {
                error: ""
            }
        },
        methods: {
            add: function () {
                //TODO!
            }
        }
    };
</script>