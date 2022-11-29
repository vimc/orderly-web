const path = require('path');
const glob = require('glob');
const VueLoaderPlugin = require('vue-loader/lib/plugin');
const CopyPlugin = require("copy-webpack-plugin");
const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;
const externals = require("./externals.config");

module.exports = {
    entry: glob.sync('./src/js/**.{js,ts}').reduce(function (obj, el) {
        obj[path.parse(el).name] = el;
        return obj
    }, {}),
    amd: false,
    module: {
        rules: [
            {
                test: /\.vue$/,
                loader: 'vue-loader'
            },
            {
                test: /\.tsx?$/,
                loader: 'ts-loader',
                exclude: /node_modules/,
                options: {
                    appendTsSuffixTo: [/\.vue$/],
                }
            },
            {
                test: /\.css$/,
                use: [
                    'vue-style-loader',
                    'css-loader'
                ]
            }
        ]
    },
    externals: {
        jquery: 'jQuery'
    },
    plugins: [
        // make sure to include the plugin!
        new VueLoaderPlugin(),
        new CopyPlugin({
            patterns:
                externals.map(e => ({from: e, to: path.resolve(__dirname, 'public/js/lib')})),
        })
        // uncomment to see analysis of bundle size
        // new BundleAnalyzerPlugin({analyzerPort: 4000})
    ],
    output: {filename: '[name].bundle.js', path: path.resolve(__dirname, 'public/js')},
    resolve: {
        extensions: ['*', '.js', '.vue', '.json', '.ts', '.tsx'],
        alias: {
            'vue$': process.env.NODE_ENV === 'production' ?
                'vue/dist/vue.min.js' : 'vue/dist/vue.js'
        }
    },
    mode: process.env.NODE_ENV === 'production' ? 'production' : 'development'
};
