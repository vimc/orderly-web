'use strict';

import path from 'path'
import gulp from 'gulp';
import sass from 'gulp-sass';
import rename from 'gulp-rename';
import minify from 'gulp-clean-css';
import webpack from 'webpack-stream';
import through from 'through';

sass.compiler = require('node-sass');

gulp.task('sass', function () {
    return gulp.src('src/scss/style.scss')
        .pipe(sass().on('error', sass.logError))
        .pipe(gulp.dest('public/css'))
        .pipe(minify({compatibility: 'ie8'}))
        .pipe(rename({
            extname: ".min.css"
        }))
        .pipe(gulp.dest('public/css'));
});

gulp.task('css', function () {
    return gulp.src('node_modules/datatables.net-bs4/css/dataTables.bootstrap4.min.css')
        .pipe(rename({
            basename: "dataTables.min"
        }))
        .pipe(gulp.dest('public/css'));
});

gulp.task('js', () => {
    return gulp.src('src/js/*.js')
        .pipe(through(function(file) {
            file.named = path.basename(file.path, path.extname(file.path));
            this.queue(file);
        }))
        .pipe(webpack({
            output: {filename: '[name].bundle.js', path: path.resolve(__dirname, 'public/js')},
            resolve: {
                alias: {
                    'vue$': 'vue/dist/vue.esm.js'
                }
            },
            mode: 'production'
        }))
        .pipe(gulp.dest('public/js'));
});

gulp.task('build', gulp.parallel('sass', 'css', 'js'));