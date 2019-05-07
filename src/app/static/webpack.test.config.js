const nodeExternals = require('webpack-node-externals');
const config = require('./webpack.common.config');

config.externals = [nodeExternals()]; // in order to ignore all modules in node_modules folder

module.exports = config;
