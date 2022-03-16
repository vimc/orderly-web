module.exports = {
    root: true,
    parser: "vue-eslint-parser",
    parserOptions: {
        "parser": "@typescript-eslint/parser"
    },
    plugins: [
        "@typescript-eslint"
    ],
    extends: [
        "eslint:recommended",
        "plugin:@typescript-eslint/recommended",
        "plugin:vue/essential",
        "plugin:vue/recommended",
        "plugin:vue/strongly-recommended"
    ],
    rules: {
        "no-prototype-builtins": "off",
        // this is just a rule to enforce nesting script tags in vue templates
        // unfortunately it doesn't understand typescript AST so won't enforce any other
        // code indentation rules in Vue script tags
        // https://eslint.vuejs.org/rules/script-indent.html
        "vue/script-indent": ["error", 4, {
            "baseIndent": 1,
            "ignores": [
                // nested objects, excluding top level of exported object (data, methods, computed, etc.)
                "[value.type='ObjectExpression']:not(:matches(ExportDefaultDeclaration, [left.property.name='exports']) > * > [value.type='ObjectExpression'])",
                // nested arrays
                "[value.type='ArrayExpression']"
            ]
        }],
        "vue/html-closing-bracket-newline": ["warn", { singleline: "never", multiline: "never" }],
        "vue/multiline-html-element-content-newline": 0,
        "vue/max-attributes-per-line": ["error", {
            "singleline": {
                "max": 10
            },
            "multiline": {
                "max": 10
            }
        }],
        "vue/html-indent": ["error", 4, {
            "attribute": 1,
            "baseIndent": 1,
            "closeBracket": 0,
            "alignAttributesVertically": true,
            "ignores": []
        }],
        "vue/first-attribute-linebreak": ["error", {
            "singleline": "ignore",
            "multiline": "ignore"
        }],
        "vue/require-default-prop": 1,
        "vue/html-self-closing": ["error", {
            "html": {
                "void": "any",
                "normal": "any",
                "component": "any"
            },
            "svg": "any",
            "math": "any"
        }],
        "vue/html-closing-bracket-spacing": ["error", {
            "startTag": "never",
            "endTag": "never",
            "selfClosingTag": "never"
        }],
        "vue/multi-word-component-names": ["warn", {
            "ignores": []
        }]
    }
};
