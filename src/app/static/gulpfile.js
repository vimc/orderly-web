'use strict';

const gulp = require('gulp'),
    sass = require('gulp-sass'),
    rename = require('gulp-rename'),
    minify = require('gulp-clean-css'),
    webpack = require('webpack-stream'),
    through = require('through'),
    VueLoaderPlugin = require('vue-loader/lib/plugin');

sass.compiler = require('node-sass');

gulp.task('sass', function () {
    return gulp.src('src/scss/*.scss')
        .pipe(sass().on('error', sass.logError))
        .pipe(gulp.dest('public/css'))
        .pipe(minify({compatibility: 'ie8'}))
        .pipe(rename({
            extname: ".min.css"
        }))
        .pipe(gulp.dest('public/css'));
});

gulp.task('js', () => {
    return gulp.src('src/js/*.js')
        .pipe(through(function (file) {
            file.named = path.basename(file.path, path.extname(file.path));
            this.queue(file);
        }))
        .pipe(webpack({
            output: {filename: '[name].bundle.js', path: path.resolve(__dirname, 'public/js')},
            resolve: {
                alias: {
                    'vue$': process.env.NODE_ENV === 'production' ?
                        'vue/dist/vue.min.js' : 'vue/dist/vue.js'
                }
            },
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
            plugins: [
                // make sure to include the plugin!
                new VueLoaderPlugin()
            ],
            mode: process.env.NODE_ENV === 'production' ? 'production' : 'development'
        }))
        .pipe(gulp.dest('public/js'));
});

gulp.task('build', gulp.parallel('sass', 'js'));