const path = require('path');
const VueLoaderPlugin = require('vue-loader/lib/plugin');

module.exports = {
    module: {
        rules: [
            {
                test: /\.vue$/,
                loader: 'vue-loader'
            },
            {
                test: /\.js$/,
                loader: 'babel-loader',
                options: {
                    presets: ['@babel/preset-env']
                }
            },
            {
                parser: {amd: false}
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
        new VueLoaderPlugin()
    ],
    output: {filename: '[name].bundle.js', path: path.resolve(__dirname, 'public/js')},
    resolve: {
        alias: {
            'vue$': process.env.NODE_ENV === 'production' ?
                'vue/dist/vue.min.js' : 'vue/dist/vue.js'
        }
    },
    mode: process.env.NODE_ENV === 'production' ? 'production' : 'development'
};
