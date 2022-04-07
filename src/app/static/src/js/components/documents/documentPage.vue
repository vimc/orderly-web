<template>
    <div>
        <div class="row">
            <div v-if="canManage" class="col-8">
                <label class="font-weight-bold">Manage project docs</label>
                <refresh-documents @refreshed="getDocs"></refresh-documents>
            </div>
        </div>
        <document-list :docs="docs"></document-list>
    </div>
</template>

<script>
    import documentList from "./documentList";
    import {api} from "../../utils/api";
    import refreshDocuments from "./refreshDocuments";

    export default {
        name: "DocumentApp",
        components: {
            documentList,
            refreshDocuments
        },
        props: ["canManage"],
        data() {
            return {
                docs: []
            }
        },
        created() {
            this.getDocs()
        },
        methods: {
            getDocs() {
                api.get("/documents/")
                    .then(({data}) => {
                        this.docs = data.data;
                    })
            }
        }
    }

</script>