var app = angular.module('queryPage', []);

app.controller('queryController', function($scope) {
	$scope.metadata = {};
});

app.directive('queryform', function() {
	return {
		restrict: 'E',
		template: '<selectcancerstudy></selectcancerstudy>\
					<selectgenomicprofiles></selectgenomicprofiles>\
					<selectcaseset></selectcaseset>\
					<entergeneset></entergeneset>\
					<submitbutton></submitbutton>'
	};
});

app.directive('selectcancerstudy', function() {
	return {
		restrict: 'E',
		template: '<div id="select_cancer_study_section">Select Cancer Study:\
					<select id="select_cancer_study">\
					<option value="all">All Cancer Studies</option>\
					</select>\
					</div>'
	};
});

app.directive('selectgenomicprofiles', function() {
	return {
		restrict: 'E',
		template: '<div id="select_genomic_profiles_section">Select Genomic Profiles:\
					</div>'
	};
});

app.directive('selectcaseset', function() {
	return {
		restrict: 'E',
		template: '<div id="select_case_set_section">Select Patient/Case Set:\
					<select id="select_case_set">\
					<option value="all">All Tumors</option>\
					</select>\
					</div>'
	};
});

app.directive('entergeneset', function() {
	return {
		restrict: 'E',
		template: '<div id="enter_gene_set_section">Enter Gene Set:\
					<select id="enter_gene_set_section">\
					<option value="all">All Tumors</option>\
					</select>\
					</div>'
	};
});

app.directive('submitbutton', function() {
	return {
		restrict: 'E',
		template: '<button>Submit</button>'
	};
});
// CLASS DIRECTIVES