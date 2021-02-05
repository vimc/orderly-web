<template>
  <div class="col-sm-6">
    <table class="table table-sm table-bordered">
      <tbody>
      <tr v-for="(params, index) in paramValues" :key="index">
        <th><label :for="`param-control-${index}`"
                   class="col-sm-2 col-form-label text-right">
          {{ params.name }}
        </label></th>
        <td><input type="text" class="form-control"
                   v-model="getValues[index].default" ref="foo"
                   :id="`param-control-${index}`"/></td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import {Parameter} from "../../utils/types";

interface Props {
  params: Parameter[]
}

interface Data {
  paramValues: Parameter[]
}

interface Computed {
  getValues: Parameter[]
}

export default Vue.extend<Data, Computed, unknown, Props>({
  name: "parameterList",
  props: {
    params: []
  },
  data(): Data {
    return {
      paramValues: this.params
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
  }
})
</script>