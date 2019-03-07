const nodeExternals = require('webpack-node-externals');

module.exports = {
  //  target: 'node', // in order to ignore built-in modules like path, fs, etc.
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
            }
        ]
    },
    externals: [nodeExternals()] // in order to ignore all modules in node_modules folder
};
