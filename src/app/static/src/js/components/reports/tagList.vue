<template>
    <div>
        <h6>{{header}}</h6>
        <div class="text-muted tag-list-description">{{description}}</div>
        <p class="tags">
            <span v-for="tag in value" :class="'badge mr-1 mb-1 badge-' + (editable ? 'primary' : 'secondary')">
                {{tag}}<span v-if="editable"> |
                    <span class="remove-tag" @click="removeTag(tag)">×</span>
                </span>
            </span>
        </p>
        <div v-if="editable" class="input-group input-group-sm">
            <input v-model="tagToAdd" class="form-control" type="input"/>
            <div class="input-group-append">
                <button class="btn btn-sm submit" @click="addTag">Add tag</button>
            </div>
        </div>
    </div>
</template>

<script>
    import Vue from "vue";

    export default Vue.extend({
        props: ["value", "editable", "header", "description"],
        data() {
            return {
                tagToAdd: ""
            }
        },
        methods: {
            addTag: function() {
                if ((this.tagToAdd.trim() != "") && (this.value.indexOf(this.tagToAdd) < 0)) {
                    this.$emit("input", [...this.value, this.tagToAdd]);
                    this.tagToAdd = "";
                }
            },
            removeTag: function(tag) {
                const index = this.value.indexOf(tag);
                const removed = [...this.value];
                removed.splice(index, 1);
                this.$emit("input", removed);
            }
        }
    });
</script>
