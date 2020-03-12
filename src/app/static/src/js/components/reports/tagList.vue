<template>
    <div>
        <h6>{{header}}</h6>
        <div class="text-muted tag-list-description">{{description}}</div>
        <p>
            <span v-for="tag in value" :class="'badge mr-1 badge-' + (editable ? 'primary' : 'secondary')">{{tag}}</span>
        </p>
        <div v-if="editable" class="input-group input-group-sm">
            <input v-model="roleToAdd" class="form-control" type="input"/>
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
                roleToAdd: ""
            }
        },
        methods: {
            addTag: function() {
                this.$emit("input", [...this.value, this.roleToAdd]);
            }
        }
    });
</script>
