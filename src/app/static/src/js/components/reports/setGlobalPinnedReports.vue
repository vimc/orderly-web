<template>
    <div>
        <a href="#" @click="expand">
            <edit-icon></edit-icon>
            Edit pinned reports
        </a>
        <div v-if="expanded">
            <div class="font-weight-bold">
                Select up to three reports to pin:
            </div>
            <div class="ml-2">
                <ul v-if="selected.length > 0" class="list-unstyled children mt-1">
                    <li v-for="selectedReport in selected">
                        <span class="name" :id="selectedReport">{{displayName(selectedReport)}}</span>
                        <span @click="function() {remove(selectedReport)}" class="remove d-inline-block ml-2 large">×</span>
                    </li>
                </ul>
                <div class="mb-3 mt-2">
                    <typeahead
                            size="sm"
                            v-model="newPinnedReport"
                            placeholder="report name"
                            :data="availableDisplayNames">
                        <template slot="append">
                            <button @click="add" type="submit" class="btn btn-sm">Add</button>
                        </template>
                    </typeahead>
                    <error-info :default-message="defaultMessage" :api-error="error"></error-info>
                </div>
                <error-info :default-message="defaultMessage" :api-error="error"></error-info>
                <div>
                    <button class="btn btn-sm btn-default float-right" @click="cancel">Cancel</button>
                    <button class="btn btn-sm float-right" type="submit" @click="save">Save changes</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    import EditIcon from "./editIcon";
    import ErrorInfo from "../errorInfo";
    import Typeahead from "../typeahead/typeahead";
    import {api} from "../../utils/api";

    export default {
        name: "setGlobalPinnedReports",
        props: ["available", "current"],
        data() {
            return this.initialState();
        },
        computed: {
            availableDisplayNames: function() {
                return Object.keys(this.available)
                    .filter(r => this.selected.indexOf(r) < 0)
                    .map(r => this.available[r]);
            }
        },
        methods: {
            expand: function() {
                this.expanded = true;
            },
            cancel: function() {
                Object.assign(this, this.initialState());
            },
            displayName: function(name) {
                return this.available[name] || name;
            },
            remove: function(name) {
                this.selected = this.selected.filter(r => r !== name)
            },
            add: function() {
                //Find the report name which corresponds to the display name from the typeahead (newPinnedReport)
                const name = Object.keys(this.available).filter(r => this.available[r] === this.newPinnedReport);
                if (name.length > 0) {
                    this.selected.push(name[0]);
                }
            },
            save: function() {
                const data = {
                    reports: this.selected,
                };

                api.post(`/global-pinned-reports/`, data)
                    .then(() => {
                        //TODO: refresh page
                    })
                    .catch((error) => {
                        this.error = error;
                        this.defaultMessage = `could not save pinned reports`;
                    });
            },
            initialState: function() {
                return {
                    expanded: false,
                    selected: [...this.current],
                    defaultMessage: "",
                    error: null,
                    newPinnedReport: ""
                }
            }
        },
        components: {
            EditIcon,
            ErrorInfo,
            Typeahead
        }
    }
</script>
