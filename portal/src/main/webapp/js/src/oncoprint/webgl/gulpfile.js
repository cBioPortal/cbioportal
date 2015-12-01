var gulp = require('gulp');
var shell = require('gulp-shell');

gulp.task('default', shell.task(['browserify test.js -o oncoprint-bundle.js']));
