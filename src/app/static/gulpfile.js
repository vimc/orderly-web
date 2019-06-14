'use strict';

const gulp = require('gulp'),
    sass = require('gulp-sass'),
    rename = require('gulp-rename'),
    minify = require('gulp-clean-css'),
    webpack = require('webpack-stream'),
    through = require('through'),
    path = require('path'),
    uglify = require('gulp-uglify'),
    babel = require('gulp-babel'),
    webpackConfig = require('./webpack.config'),
    externals = require("./externals.config");

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

gulp.task('webpack', () => {
    return gulp.src('src/js/*.js')
        .pipe(through(function (file) {
            file.named = path.basename(file.path, path.extname(file.path));
            this.queue(file);
        }))
        .pipe(webpack(webpackConfig))
        .pipe(gulp.dest('public/js'));
});


gulp.task('js', function () {
    return gulp.src(externals)
        .pipe(babel({
            presets: ['@babel/preset-env']
        }))
        .pipe(uglify())
        .pipe(rename({
            extname: ".min.js"
        }))
        .pipe(gulp.dest('public/js/lib'));
});

gulp.task('build', gulp.parallel('sass', 'js', 'webpack'));
