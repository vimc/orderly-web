<template>
    <div id="workflow-container" class="mt-3">
        <div class="row">
            <div class="col-12 col-md-4 col-xl-3">
                <div class="sidebar pb-4 pb-md-0">
                    <nav class="pl-0 pr-0 pr-md-4 navbar navbar-light">
                        <button type="button" class="d-md-none navbar-toggler" data-toggle="collapse"
                                data-target="#sidebar">
                            <span class="navbar-toggler-icon"></span>
                        </button>
                        <div class="d-md-block mt-4 mt-md-0 collapse navbar-collapse" id="sidebar">
                            <ul class="nav flex-column list-unstyled mb-0">
                                <li class="nav-item">
                                    <a id="run-workflow-link" class="nav-link" :class="{active: selectedTab == 'runWorkflow'}" data-toggle="tab" role="tab" href="#"
                                       @click="switchTab('runWorkflow')">Run workflow</a>
                                </li>
                                <li class="nav-item">
                                    <a id="workflow-progress-link" class="nav-link" :class="{active: selectedTab == 'runWorkflowProgress'}" data-toggle="tab" role="tab" href="#"
                                       @click="switchTab('runWorkflowProgress')">Workflow progress</a>
                                </li>
                            </ul>
                        </div>
                    </nav>
                </div>
            </div>
            <div class="col-12 col-md-8 tab-content">
                <div v-if="selectedTab === 'runWorkflow'" class="tab-pane active pt-4 pt-md-1" role="tabpanel" id="run-workflow-tab">
                    <div id="runWorkflow">
                        <run-workflow @view-progress="viewProgress"></run-workflow>
                    </div>
                </div>
                <div v-if="selectedTab === 'runWorkflowProgress'" class="tab-pane active pt-4 pt-md-1" role="tabpanel" id="workflow-progress-tab">
                    <div id="runWorkflowProgress">
                        <h2>Workflow progress</h2>
                       <run-workflow-progress :initial-selected-workflow="selectedWorkflow" @set-selected-workflow-key="setSelectedWorkflow"></run-workflow-progress>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import Vue from "vue";
import runWorkflow from './runWorkflow.vue'
import runWorkflowProgress from './runWorkflowProgress.vue'

export default Vue.extend({
    name: "runWorkflowTabs",
    data() {
        return {
            selectedTab: "runWorkflow",
            selectedWorkflow: ""
        }
    },
    methods: {
        switchTab(tab) {
            this.selectedTab = tab
        },
        viewProgress(workflowKey) {
            this.switchTab('runWorkflowProgress');
            this.selectedWorkflow = workflowKey;
        },
        setSelectedWorkflow(key){
            this.selectedWorkflow = key;
        },
    },
    components: {
        runWorkflow,
        runWorkflowProgress
    }
})
</script>