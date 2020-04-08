<template>
    <div>
        <div class="row">
            <div class="col-8" v-if="canManage">
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
        name: "document-app",
        props: ["canManage"],
        data() {
            return {
                docs: []
            }
        },
        components: {
            documentList,
            refreshDocuments
        },
        methods: {
            getDocs() {
                api.get("/documents/")
                    .then(({data}) => {
                        this.docs = data.data;
                    })
            }
        },
        created() {
            this.getDocs()
        }
    }

</script>