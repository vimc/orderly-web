<template>
    <div>
        <div :class="sizeClasses">
            <div v-if="$slots.prepend || prepend" ref="prependDiv" class="input-group-prepend">
                <slot name="prepend">
                    <span class="input-group-text">{{ prepend }}</span>
                </slot>
            </div>
            <input ref="input"
                   type="search"
                   :class="`form-control ${inputClass}`"
                   :placeholder="placeholder"
                   :aria-label="placeholder"
                   :value="inputValue"
                   autocomplete="off"
                   @focus="isFocused = true"
                   @blur="handleBlur"
                   @input="handleInput($event.target.value)"
                   @keyup.down="$emit('keyup.down', $event.target.value)"
                   @keyup.up="$emit('keyup.up', $event.target.value)"
                   @keyup.enter="$emit('keyup.enter', $event.target.value)"/>
            <div v-if="$slots.append || append" class="input-group-append">
                <slot name="append">
                    <span class="input-group-text">{{ append }}</span>
                </slot>
            </div>
        </div>
        <vue-bootstrap-typeahead-list
            v-show="isFocused && data.length > 0"
            ref="list"
            class="vbt-autcomplete-list"
            :query="inputValue"
            :data="formattedData"
            :background-variant="backgroundVariant"
            :text-variant="textVariant"
            :max-matches="maxMatches"
            :min-matching-chars="minMatchingChars"
            @hit="handleHit">
            <!-- pass down all scoped slots -->
            <template v-for="(slot, slotName) in $scopedSlots" :slot="slotName" slot-scope="{ data, htmlText }">
                <slot :name="slotName" v-bind="{ data, htmlText }"></slot>
            </template>
            <!-- below is the right solution, however if the user does not provide a scoped slot, vue will still set $scopedSlots.suggestion to a blank scope
            <template v-if="$scopedSlots.suggestion" slot="suggestion" slot-scope="{ data, htmlText }">
              <slot name="suggestion" v-bind="{ data, htmlText }" />
            </template>-->
        </vue-bootstrap-typeahead-list>
    </div>
</template>

<script>
    import VueBootstrapTypeaheadList from './typeaheadList.vue'

    export default {
        name: 'VueBootstrapTypehead',

        components: {
            VueBootstrapTypeaheadList
        },

        props: {
            size: {
                type: String,
                default: null,
                validator: size => ['lg', 'sm'].indexOf(size) > -1
            },
            value: {
                type: String
            },
            data: {
                type: Array,
                required: true,
                validator: d => d instanceof Array
            },
            serializer: {
                type: Function,
                default: (d) => d,
                validator: d => d instanceof Function
            },
            backgroundVariant: String,
            textVariant: String,
            inputClass: {
                type: String,
                default: ''
            },
            maxMatches: {
                type: Number,
                default: 10
            },
            minMatchingChars: {
                type: Number,
                default: 2
            },
            placeholder: String,
            prepend: String,
            append: String
        },

        data() {
            return {
                isFocused: false,
                inputValue: ''
            }
        },

        computed: {
            sizeClasses() {
                return this.size ? `input-group input-group-${this.size}` : 'input-group'
            },

            formattedData() {
                if (!(this.data instanceof Array)) {
                    return []
                }
                return this.data.map((d, i) => {
                    return {
                        id: i,
                        data: d,
                        text: this.serializer(d)
                    }
                })
            }
        },

        methods: {
            handleHit(evt) {
                if (typeof this.value !== 'undefined') {
                    this.$emit('input', evt.text)
                }

                this.inputValue = evt.text
                this.$emit('hit', evt.data)
                this.$refs.input.blur()
                this.isFocused = false
            },

            handleBlur(evt) {
                const tgt = evt.relatedTarget
                if (tgt && tgt.classList.contains('vbst-item')) {
                    return
                }
                this.isFocused = false
            },

            handleInput(newValue) {
                this.inputValue = newValue

                // If v-model is being used, emit an input event
                if (typeof this.value !== 'undefined') {
                    this.$emit('input', newValue)
                }
            }
        }
    }
</script>

<style scoped>
.vbt-autcomplete-list {
    padding-top: 5px;
    position: absolute;
    max-height: 350px;
    overflow-y: auto;
    z-index: 999;
}
</style>