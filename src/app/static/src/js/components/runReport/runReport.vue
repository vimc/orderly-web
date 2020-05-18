<template>
    <div>
        <form class="mt-3">
            <div v-if="metadata.git_supported" id="gitBranchFormGroup" class="form-group row">
                <label for="gitBranch" class="col-sm-2 col-form-label text-right">Git branch</label>
                <div class="col-sm-6">
                    <select class="form-control" id="gitBranch">
                        <option disabled selected value> -- Select a branch -- </option>
                        <option v-for="branch in gitBranches" :value="branch">{{branch}}</option>
                    </select>
                </div>
            </div>
         </form>
    </div>
</template>

<script>
    export default {
        name: "runReport",
        props: ['metadata', 'gitBranches']
    }
</script>
