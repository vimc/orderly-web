export default {
    data() {
        return {
            selected: false
        }
    },
    methods: {
        handleChangeFromChild(value) {
            if (!value.value) {
                // don't want the parent checkbox selected if any child is not
                this.selected = false
            }
            this.$emit("change", value)
        }
    }
}
