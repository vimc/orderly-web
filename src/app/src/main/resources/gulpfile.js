'use strict';

const gulp = require('gulp'),
    sass = require('gulp-sass'),
    minify = require('gulp-clean-css'),
    rename = require('gulp-rename');

sass.compiler = require('node-sass');

gulp.task('sass', function () {
    return gulp.src('scss/style.scss')
        .pipe(sass().on('error', sass.logError))
        .pipe(gulp.dest('./public/css'))
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

gulp.task('js', function () {
    return gulp.src(['node_modules/datatables.net/js/jquery.dataTables.min.js',
        'node_modules/datatables.net-bs4/js/dataTables.bootstrap4.min.js',
        'node_modules/jquery/dist/jquery.min.js'])
        .pipe(gulp.dest('public/js'));
});

gulp.task('build', gulp.parallel('sass', 'css', 'js'));