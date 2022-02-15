import "@babel/polyfill";
import $ from "jquery";
import Vue from "vue";
import Vuex from "vuex";

global.$ = global.jQuery = $;
Vue.use(Vuex);

expect.extend({
    toBeIgnoringWhitespace(received, expected) {
        received = received.replace(/\s/g, '');
        expected = expected.replace(/\s/g, '');

        return {
            message: () =>
                `expected ${received} to be equal to ${expected}`,
            pass: received === expected,
        };
    },
});

