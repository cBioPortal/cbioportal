const CBIOPORTAL_URL = 'http://localhost:8080/cbioportal/';

// SELECTOR
const gene_list = '#gene_list';

casper.test.begin('starting cbioportal oql editor tests', function suite(test){
	casper.start(CBIOPORTAL_URL);
	/* Load webpage and test whether elements have loaded correctly */
	casper.then(function(){
		test.info('testing if resources have loaded');
		test.assertResourceExists(/handlebars.+\.js/,'handlebars.js loads');
		test.assertResourceExists('gene-symbol-validator.js', 'gene-symbol-validator.js loads');
		test.assertEval(function(){
			return (typeof(GeneSymbolValidator) === 'object');
		},'GeneSymbolValidator exists as an object');
		
		test.assertEval(function(){
			return (typeof(GeneSymbolValidator.initialize) === 'function');
		},'GeneSymbolValidator.initialize exists as a function');
		
		test.assertEval(function(){
			return (typeof(GeneSymbolValidator.validateGenes) === 'function');
		},'GeneSymbolValidator.validateGenes exists as a function');

		test.assertExists(gene_list, '#gene_list exists');
		
		this.sendKeys(gene_list, 'AS ASD');
		casper.evaluate(function(){
			GeneSymbolValidator.validateGenes()
		});
		test.info('AS has been entered and validateGenes() has been called')
	});
	
	/* Upon typing a symbol, test whether replacement elements appear correctly */
	casper.then(function(){
		casper.waitForSelector('.replace-span', function(){
			test.assertExists('.replace-span[data-index="0"]', 'replace span for AS appears');
			test.assertExists('.replace-state[data-index="0"]', 'replace state for AS appears')
			test.assertExists('.replace-div[data-index="0"]', 'replace div for AS appears');
			test.assertExists('.replace-span[data-index="1"]', 'replace span for ASD appears');
			test.assertExists('.replace-state[data-index="1"]', 'replace state for ASD appears')
			test.assertExists('.replace-div[data-index="1"]', 'replace div for ASD appears');
			test.assertEvalEquals(function(){return $(gene_list).text()}, 'AS ASD', '#gene_list visible text equals "AS ASD"');
		});
	});
	
	/* Test symbol replacement */
	casper.then(function(){
		test.info('testing replacement of "AS" with a gene');
		casper.click('.replace-div-link[val="HLA-B"]');
		test.info('"HLA-B" has been clicked in the AS replace div');
		casper.waitForSelector('.noreplace-span-valid', function(){
			test.assertExists('.noreplace-span-valid', 'gene replacement works (AS -> HLA-B)');
			test.assertEvalEquals(function(){return $(gene_list).text()}, 'HLA-B ASD', 'text in #gene_list is "HLA-B ASD"');
		});
	});

	/* Test symbol removal */
	casper.then(function(){
		test.info('testing removal of ASD');
		casper.click('.replace-div-remove[data-index="1"]');
		test.info('remove has been clicked in the ASD replace div');
		test.assertEvalEquals(function(){return $(gene_list).text().search('ASD');}, -1, 'gene removal works (ASD -> "")');
	});
	
	/* Test gene set loading and newlining */
	casper.then(function(){
		casper.click('#select_gene_set_chzn_o_5');
		test.info('Glioblastoma: TP53 Pathway gene set selected');
		casper.wait(2000, function(){
			test.assertEvalEquals(function(){return $(gene_list).text()}, 'CDKN2A MDM2 MDM4 TP53', 'gene set loads correctly');
			test.info('clicking on newline checkbox');
			casper.click('#newline_checkbox');
			test.assertEvalEquals(function(){return $(gene_list).text()}, 'CDKN2A \nMDM2 \nMDM4 \nTP53', 'symbol newlining works');
		});
	});
	
	/* OQL Helper Menu testing */
	casper.then(function(){
		casper.evaluate(function(){
			$(gene_list).html('');
		})
		test.info('Testing OQL helper menu');
		test.assertExists('#oql-menu', 'OQL menu exists');
		test.assertNotVisible('#oql-menu', 'OQL menu not visible by default');
		casper.sendKeys(gene_list, ':', {keepFocus:true});
		test.assertVisible('#oql-menu', 'OQL menu appears on semicolon');
		
		casper.evaluate(function(){
			$('#ui-id-4').click();
		})
		test.assertVisible('#ui-id-4 > .ui-menu', 'Submenu appears');
	})
	
	casper.then(function(){
		casper.evaluate(function(){
			
		})
	})
	casper.then(function(){
		casper.capture('sc.png')
	})
	
	casper.run(function(){
		test.done();
	});
});
