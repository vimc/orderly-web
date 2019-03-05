const HardSourceWebpackPlugin = require('hard-source-webpack-plugin');
const nodeExternals = require('webpack-node-externals');

module.exports = {
    mode: 'development',
    target: 'node', // in order to ignore built-in modules like path, fs, etc.
    resolve: {
        alias: {
            'vue$': 'vue/dist/vue.esm.js'
        }
    },
    plugins: [
        new HardSourceWebpackPlugin()
    ],
    externals: [nodeExternals()] // in order to ignore all modules in node_modules folder
};