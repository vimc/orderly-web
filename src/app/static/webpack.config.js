const path = require('path');
const config = require('./webpack.common.config');

module.exports = Object.assign({
    output: {filename: '[name].bundle.js', path: path.resolve(__dirname, 'public/js')},
    resolve: {
        alias: {
            'vue$': process.env.NODE_ENV === 'production' ?
                'vue/dist/vue.min.js' : 'vue/dist/vue.js'
        }
    },
    mode: process.env.NODE_ENV === 'production' ? 'production' : 'development'
}, config);
