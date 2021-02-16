<template>
    <div class="col-sm-6">
        <table class="table table-sm table-bordered">
            <tbody>
            <tr v-for="(params, index) in paramValues" :key="index">
                <td><label :for="`param-control-${index}`"
                           class="col-sm-2 col-form-label text-right">
                    {{ params.name }}
                </label></td>
                <td><input type="text" class="form-control"
                           v-model="getValues[index].value"
                           @input="parameters"
                           :id="`param-control-${index}`"/></td>
            </tr>
            </tbody>
        </table>
        <div class="text-danger small">{{error}} </div>
    </div>
</template>

<script lang="ts">
    import Vue from "vue";
    import {Parameter} from "../../utils/types";

    interface Props {
        params: Parameter[],
        error: string
    }

    interface Data {
        paramValues: Parameter[]
        isValid: boolean
    }

    interface Computed {
        getValues: Parameter[]
    }

    interface Methods {
        parameters: () => void
        validate: () => void
    }
    export default Vue.extend<Data, Methods, Computed, Props>({
        name: "parameterList",
        props: {
            params: [],
            error: {
                type: String
            }
        },
        data(): Data {
            return {
                paramValues: this.params,
                isValid: false
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
        methods: {
            parameters: function () {
                this.validate()
                this.$emit("getParams", this.paramValues, this.isValid)
            },
            validate: function () {
                const validValues = this.paramValues.filter(param => param.value)
                this.error = ""
                this.isValid = true
                if (validValues.length < this.paramValues.length) {
                    this.error = "Parameter value(s) required"
                    this.isValid = false
                }
            },
        },
        mounted() {
            // run validation and emit event on initial values
            if(this.paramValues) {
                this.parameters()
            }
        }
    })
</script>