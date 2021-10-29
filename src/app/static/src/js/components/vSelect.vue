<script lang="ts">
import vSelect from "vue-select";

vSelect.props.components["default"] = () => ({
    OpenIndicator: {
        render: createElement => createElement("span", "🞃")
    },
    Deselect: {
        render: createElement => createElement("span", "×")
    }
});
export default vSelect;
</script>

<style>
.vs__open-indicator {
    font-size: small;
}
</style>
