<template>
    <div class="col-sm-6">
      <table class="table table-sm table-bordered">
        <tbody>
        <tr v-for="(param, index) in params" :key="index">
          <th><label :for="`param-control-${index}`"
                     class="col-sm-2 col-form-label text-right">
                     {{param.name}}
          </label></th>
          <th><input type="text" class="form-control"
                     v-model="param.default"
                     :id="`param-control-${index}`"/></th></tr>
        </tbody>
      </table>
    </div>
</template>

<script lang="ts">
import Vue from "vue";
import {Parameters} from "../../utils/types";

interface Props {
  params: Parameters[]
}

export default Vue.extend<unknown, unknown, unknown, Props>({
  name: "parameterList",
  props: {
    params: []
  }
})
</script>