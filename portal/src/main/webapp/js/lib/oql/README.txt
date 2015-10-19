.pegjs files are grammar specifications

run $ python generateParser.py <pegjs file> [-test]
if the test flag is up, then it is prepared for command line testing with node.js, i.e. it is written with "module.exports = " instead of the production version which is "oql_parser = "

To run tests, run $ node test-oql-parser.js
