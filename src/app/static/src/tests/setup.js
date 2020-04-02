import "@babel/polyfill";
import $ from "jquery";

global.$ = global.jQuery = $;

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

