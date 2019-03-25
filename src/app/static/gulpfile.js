'use strict';

const gulp = require('gulp'),
    sass = require('gulp-sass'),
    rename = require('gulp-rename'),
    minify = require('gulp-clean-css');

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
