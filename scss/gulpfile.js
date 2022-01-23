const gulp = require('gulp')
const sass = require('gulp-sass')(require('sass'))
const cat = require('gulp-concat')

function scss() {
  return gulp.src('./src/main.scss')
    .pipe(sass.sync({outputStyle: 'expanded'}).on('error', sass.logError))
    .pipe(cat('style.css'))
    .pipe(gulp.dest('./dist/'))
}

function minify() {
  return gulp.src('./src/main.scss')
    .pipe(sass.sync({outputStyle: 'compressed'})).on('error', sass.logError)
    .pipe(cat('styles.min.css'))
    .pipe(gulp.dest('./dist/'))
    .pipe(gulp.dest('../src/main/webapp/resources/css/'))
}

exports.scss = scss
exports.minify = minify