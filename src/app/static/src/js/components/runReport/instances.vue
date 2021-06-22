<template>
    <div v-if="showInstances">
        <div v-for="(options, name) in instances" v-if="options.length > 1"
             id="instances-div" class="form-group row">
            <label :for="name"
                   :class="`col-sm-${changelogStyle.label.size} ${changelogStyle.label.justify}`"
                   class="col-form-label">Database "{{ name }}"</label>
            <div :class="`col-sm-${changelogStyle.control.size}`">
                <select class="form-control" :id="name"
                        @change="handleSelectedInstances"
                        v-model="selectedInstances[name]">
                    <option v-for="option in options" :value="option">
                        {{ option }}
                    </option>
                </select>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    import {ChangelogStyle} from "../../utils/types";

    interface Props {
        instances: Record<string, any>,
        changelogStyle: ChangelogStyle
    }

    interface Data {
        selectedInstances: Record<string, any>
    }

    interface Methods {
        handleSelectedInstances: () => void
        selectInitialInstance: () => void
    }

    interface Computed {
        showInstances: boolean
    }

    export default Vue.extend<Data, Methods, Computed, Props>({
        name: "instances",
        props: {
            instances: {
                required: true,
                type: Object
            },
            changelogStyle: {
                required: true,
                type: Object
            }
        },
        data(): Data {
            return {
                selectedInstances: {}
            }
        },
        methods: {
            handleSelectedInstances: function () {
                this.$emit("selectedValues", this.selectedInstances)
            },
            selectInitialInstance: function () {
                if (this.instances && this.showInstances) {
                    const instances = this.instances;
                    for (const key in instances) {
                        if (instances[key].length > 0) {
                            this.$set(this.selectedInstances, key, instances[key][0]);
                        }
                    }
                }
                this.$emit("selectedValues", this.selectedInstances)
            }
        },
        computed: {
            showInstances() {
                return !!this.instances
            }
        },
        mounted() {
            this.selectInitialInstance()
        },
        watch: {
            instances() {
                this.selectInitialInstance()
            },
            selectedInstances: {
                deep: true,
                handler() {
                    this.$emit("clearRun")
                }
            }
        }
    })
</script>