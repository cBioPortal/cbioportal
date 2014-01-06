<!--div class="ppy" id="ppy2">
    <ul class="ppy-imglist">
        <li>
            <a href="images/previews/ova_survival.png">
                <img src="images/previews/thumb_ova_survival.png" alt="" />
            </a>
            <span class="ppy-extcaption">
                <strong>Survival Analysis of BRCA Mutated v. Non-BRCA Mutated in Serous Ovarian Cancer.</strong><br /><br/>BRCA-mutated cases show significantly better overall survival.<br />
                <br>Data Source:  TCGA Serous Ovarian Cancer Data Set.
            </span>
        </li>
        <li>
            <a href="images/previews/brca1_meth.png">
                <img src="images/previews/thumb_brca1_meth.png" alt="" />
            </a>
            <span class="ppy-extcaption">
                <strong>Epigenetic Silencing of BRCA1 in Serous Ovarian Cancer</strong><br /><br/>Example of Methylation v. mRNA Plot for BRCA1 in Serous Overian Cancer.<br />
                <br>Data Source:  TCGA Serous Ovarian Cancer Data Set.
            </span>
        </li>
        <li>
            <a href="images/previews/TP53_mutations_OV.png">
                <img src="images/previews/thumb_TP53_mutations_OV.png" alt="" />
            </a>
            <span class="ppy-extcaption">
                <strong>TP53 mutations in ovarian cancer </strong><br /><br/>TP53 is mutated in 95% of serous ovarian cancer patients. Some of these mutations are truncating, and
                most of the missense mutations are predicted to have a high functional impact (prediction by Mutation Assessor).<br />
                <br>Data Source:  TCGA Serous Ovarian Cancer Data Set.
            </span>
        </li>
    </ul>
    <div class="ppy-outer">
        <div class="ppy-stage">
            <div class="ppy-counter">
                <strong class="ppy-current"></strong> / <strong class="ppy-total"></strong>
            </div>
        </div>

        <div class="ppy-nav">
            <div class="nav-wrap">
                <a class="ppy-next" title="Next image">Next image</a>
                <a class="ppy-prev" title="Previous image">Previous image</a>
                <a class="ppy-switch-enlarge" title="Enlarge">Enlarge</a>
                <a class="ppy-switch-compact" title="Close">Close</a>
            </div>

        </div>
        <div class="ppy-caption">
            <span class="ppy-text"></span>
        </div>
    </div>

</div-->

<div id="previewContainer">
	<ul class="rslides" id="previewList">
		<li>
			<img src="images/previews/thumb_ova_survival.png"
			     alt="Survival Analysis of BRCA Mutated v. Non-BRCA Mutated in Serous Ovarian Cancer">
		</li>
		<li>
			<img src="images/previews/thumb_brca1_meth.png"
			     alt="Epigenetic Silencing of BRCA1 in Serous Ovarian Cancer">
		</li>
		<li>
			<img src="images/previews/thumb_TP53_mutations_OV.png"
			     alt="TP53 mutations in ovarian cancer">
		</li>
	</ul>
</div>

<script type="text/javascript">
        $(document).ready(function () {
            // TODO we have code duplication here! (see preview.jsp)
	        var options = {
		        maxWidth: 170
	        };

	        $('#previewList').responsiveSlides(options);
        });
</script>