var gulp = require('gulp');
var shell = require('gulp-shell');

gulp.task('default', shell.task(['browserify main.js -o oncoprint-bundle.js']));
gulp.task('test', shell.task(['browserify test.js -o oncoprint-test-bundle.js']));
