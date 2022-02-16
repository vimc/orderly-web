<template>
    <div class="col-sm-6">
        <table class="table table-sm table-bordered">
            <tbody>
                <tr v-for="(params, index) in paramValues" :key="index">
                    <td><label :for="`param-control-${index}`"
                               class="col-sm-2 col-form-label text-right">
                        {{ params.name }}
                    </label>
                    </td>
                    <td><input :id="`param-control-${index}`" v-model="getValues[index].value"
                               type="text"
                               class="form-control"
                               @input="onParameterChanged">
                    </td>
                </tr>
            </tbody>
        </table>
        <div class="text-danger small">
            {{ error }}
        </div>
    </div>
</template>

<script lang="ts">
    import Vue from "vue";
    import {Parameter} from "../../utils/types";
    import {isEqual} from "lodash";

    interface Props {
        params: Parameter[]
    }

    interface Data {
        paramValues: Parameter[]
        valid: boolean,
        error: string
    }

    interface Computed {
        getValues: Parameter[]
    }

    interface Methods {
        onParameterChanged: () => void
        validate: () => void
    }

    export default Vue.extend<Data, Methods, Computed, Props>({
        name: "ParameterList",
        props: {
            params: Array
        },
        data(): Data {
            return {
                paramValues: this.params,
                valid: false,
                error: ""
            }
        },
        computed: {
            getValues: {
                set: function (values) {
                    values.forEach((value, key) => {
                        this.paramValues[key] = value
                    })
                },
                get: function () {
                    return this.paramValues
                }
            }
        },
        watch: {
            params(newVal, oldVal) {
                if (!isEqual(newVal, oldVal)) {
                    this.paramValues = this.params;
                    this.onParameterChanged()
                }
            }
        },
        mounted() {
            // run validation and emit event on initial values
            if (this.paramValues) {
                this.onParameterChanged()
            }
        },
        methods: {
            onParameterChanged: function () {
                this.validate()
                this.$emit("paramsChanged", this.paramValues, this.valid)
            },
            validate: function () {
                const validValues = this.paramValues.filter(param => param.value)
                this.error = ""
                this.valid = true
                if (validValues.length < this.paramValues.length) {
                    this.error = "Parameter value(s) required"
                    this.valid = false
                }
            },
        }
    })
</script>
