global._ = require('underscore')._;
var assert = require('chai').assert;
var cbioStat = require('./cbio-stat.js');

describe('cBio Stat Library', function() {
    
    describe('function to calculate mean of an input array', function() {
        it ('odd numbered elements array', function() {
            assert.equal(cbioStat.mean([1, 2, 3]), 2);
        });
        it ('even numbered elements array', function() {
            assert.equal(cbioStat.mean([1, 2, 3, 4]), 2.5);    
        });
        it ('arrays contain negative numbers', function() {
            assert.equal(cbioStat.mean([-2, -6, 5.5, 7]), 1.125);    
        });
        it ('arrays contain only float numbers', function() {
            assert.equal(cbioStat.mean([1.263, -19.286, -0.563, 27.263]), 2.16925);
        });
    });
    
    describe('function to calculate standard deviation of an input array', function() {
        it ('arrays contain only positive numbers', function() {
            assert.equal(cbioStat.stDev([10, 2, 38, 23, 38, 23, 21]).toFixed(3), 12.258);
        });
        it ('arrays contain negative numbers', function() {
            assert.equal(cbioStat.stDev([-12, 2, 38, -23, 38, -23, 21]).toFixed(3), 24.750);
        });
        it ('array contains only float numbers', function() {
            assert.equal(cbioStat.stDev([-12.1827, 2.4719, 38.1827, -23.172, 38.461, -23.16234, 21.2763]).toFixed(3), 24.958);
        });
    });    
    
    describe('function to calculate zscores (array) from an input array', function() {
        it ('arrays contain only positive numbers', function() {
            assert.deepEqual(cbioStat.zscore(
                [10, 2, 38, 23, 38, 23, 21], [10, 2, 34]),
                [-0.9905844938969603, -1.6432048663467225, 0.9672766234523261]);
        });
        it ('arrays contain negative numbers', function() {
            assert.deepEqual(cbioStat.zscore(
                [-10, -2, 38, -23, -38, 23, 21], [10, 2, 34]),
                [0.3447538369625852, 0.02825851122644141, 1.2942398141710167]);
        });
        it ('array contains only float numbers', function() {
            assert.deepEqual(cbioStat.zscore(
                [-10.2837, -2.1825, 38.70398, -23.287587, -38.271, 23.703, 21.917], [10, 2, 34]),
                [0.33142208694041647, 0.020544599661838775, 1.2640545487761494]);
        });
    });

});
