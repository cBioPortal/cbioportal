/**
 * Created by suny1 on 11/18/15.
 */
var PlotlyCCplots = (function (Plotly) {

    var data = [], layout = {};

    var fetch_data = function() {

    }

    var define_tracks = function() {
        var non_mut = {
            x: [
                -2.02,
                -1.03,
                0.07,
                1.13,
                2.14,
                -2.09,
                -1.14,
                0.12,
                1.09,
                2.17,
                -2.14,
                -1.26,
                0.02,
                1.18,
                2.15
            ],
            y: [
                1.1,
                6.2,
                3.09,
                6.78,
                2.65,
                1.1,
                3.2,
                5.09,
                1.78,
                7.65,
                3.1,
                9.2,
                5.09,
                2.78,
                7.65
            ],
            mode: 'markers',
            type: 'scatter',
            name: 'Non Mut',
            text: [
                'A-1',
                'A-2',
                'A-3',
                'A-4',
                'A-5',
                'A-1',
                'A-2',
                'A-3',
                'A-4',
                'A-5',
                'A-1',
                'A-2',
                'A-3',
                'A-4',
                'A-5'
            ],
            marker: {
                size: 8,
                symbol: 'circle' },
            hoverinfo: "x+y"
        };

        var mut = {
            x: [
                -2,
                -1,
                0,
                1,
                2
            ],
            y: [
                4.64,
                1.15,
                7.12,
                1.98,
                4.35
            ],
            mode: 'markers',
            type: 'scatter',
            name: 'Muted',
            text: ['B-a', 'B-b', 'B-c', 'B-d', 'B-e'],
            marker: {
                size: 8,
                symbol: 'diamond' },
            hoverinfo: "x+y+text"
        };

        var box = {
            x: [],
            y: [],
            mode: 'markers'
        }

        data = [ non_mut, mut ];
    }

    var define_layout = function() {
        layout = {
            xaxis: {
                range: [ -2.3, 2.3 ],
                title: 'x axis title'
            },
            yaxis: {
                range: [ 0, 8 ],
                title: 'y axis title'
            },
            title:'Example Scatter Plots'
        };
    }

    var render = function() {
        fetch_data();
        define_tracks();
        define_layout();
        Plotly.newPlot('plotly_cc_plots_box', data, layout);
    }

    return {
        init: function() {
            render();
        }
    }

}(window.Plotly));

