<%--
 - Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 -
 - This library is distributed in the hope that it will be useful, but WITHOUT
 - ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 - FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 - is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 - obligations to provide maintenance, support, updates, enhancements or
 - modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 - liable to any party for direct, indirect, special, incidental or
 - consequential damages, including lost profits, arising out of the use of this
 - software and its documentation, even if Memorial Sloan-Kettering Cancer
 - Center has been advised of the possibility of such damage.
 --%>

<%--
 - This file is part of cBioPortal.
 -
 - cBioPortal is free software: you can redistribute it and/or modify
 - it under the terms of the GNU Affero General Public License as
 - published by the Free Software Foundation, either version 3 of the
 - License.
 -
 - This program is distributed in the hope that it will be useful,
 - but WITHOUT ANY WARRANTY; without even the implied warranty of
 - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 - GNU Affero General Public License for more details.
 -
 - You should have received a copy of the GNU Affero General Public License
 - along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>

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