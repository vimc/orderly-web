<template>
    <div v-if="showInstances">
        <div v-for="(options, name) in instancesWithMultipleOptions"
             :key="name" class="form-group row instance-div">
            <label :for="name"
                   :class="customStyle.label"
                   class="col-form-label">Database "{{ name }}"</label>
            <div class="instance-control" :class="customStyle.control">
                <select :id="name" v-model="selectedInstances[name]"
                        class="form-control"
                        @change="handleSelectedInstances">
                    <option v-for="option in options" :key="option" :value="option">
                        {{ option }}
                    </option>
                </select>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
    import Vue from "vue"
    import {ChildCustomStyle} from "../../utils/types";

    interface Props {
        instances: Record<string, any>
        customStyle: ChildCustomStyle
        initialSelectedInstances: Record<string, string>
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
        instancesWithMultipleOptions: Record<string, any>
    }

    export default Vue.extend<Data, Methods, Computed, Props>({
        name: "SelectInstances",
        props: {
            instances: {
                required: true,
                type: Object
            },
            customStyle: {
                required: true,
                type: Object
            },
            initialSelectedInstances: {
                required: false,
                type: Object
            }
        },
        data(): Data {
            return {
                selectedInstances: {}
            }
        },
        computed: {
            showInstances() {
                return !!this.instances;
            },
            instancesWithMultipleOptions() {
                return Object.keys(this.instances)
                    .filter(key => this.instances[key] && this.instances[key].length > 1)
                    .reduce((res, key) => (res[key] = this.instances[key], res), {});
            }
        },
        watch: {
            instances() {
                this.selectInitialInstance();
            }
        },
        mounted() {
            this.selectInitialInstance();
        },
        methods: {
            handleSelectedInstances: function () {
                this.$emit("selectedValues", this.selectedInstances);
            },
            selectInitialInstance: function () {
                if (this.showInstances) {
                    const instances = this.instances;
                    const initialInstances = this.initialSelectedInstances || {};
                    let updated = false;
                    for (const key in instances) {
                        if (instances[key].length > 0) {
                            let initialValue;
                            if (initialInstances[key]) {
                                initialValue = initialInstances[key];
                            } else {
                                initialValue = instances[key][0];
                                updated = true;
                            }
                            this.$set(this.selectedInstances, key, initialValue);
                        }
                    }
                    if (updated) {
                        this.$emit("selectedValues", this.selectedInstances);
                    }
                }
            }
        }
    })
</script>
