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

<div class='section' id='mutation_details'>
	<img src='images/ajax-loader.gif'/>
</div>

<style type="text/css" title="currentStyle">
	@import "css/data_table_jui.css?<%=GlobalProperties.getAppVersion()%>";
	@import "css/data_table_ColVis.css?<%=GlobalProperties.getAppVersion()%>";
	@import "css/mutationMapper.min.css?<%=GlobalProperties.getAppVersion()%>";
</style>

<script type="text/javascript">

// TODO 3d Visualizer should be initialized before document get ready
// ...due to incompatible Jmol initialization behavior
var _mut3dVis = null;
_mut3dVis = new Mutation3dVis("default3dView");
_mut3dVis.init();

// Set up Mutation View
$(document).ready(function() {
	var sampleArray = _.keys(PortalGlobals.getPatientSampleIdMap());
	var mutationProxy = DataProxyFactory.getDefaultMutationDataProxy();

	// init default mutation details view

	var options = {
		el: "#mutation_details",
		data: {
			sampleList: sampleArray
		},
		proxy: {
			mutationProxy: {
				instance: mutationProxy
			}
		}
	};

	var defaultView = MutationViewsUtil.initMutationMapper("#mutation_details",
		options,
		"#tabs",
		"Mutations",
		_mut3dVis);
});

</script>