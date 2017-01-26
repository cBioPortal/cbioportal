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

<%@ page import="org.mskcc.cbio.portal.stats.OddsRatio" %>
<%@ page import="org.mskcc.cbio.portal.model.GeneWithScore" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="org.apache.commons.lang.math.DoubleRange" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.io.IOException" %>

<script type="text/javascript" src="js/src/mutex/dataProxy.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutex/view.js?<%=GlobalProperties.getAppVersion()%>"></script>


<div class="section" id="mutex" class="mutex">
    <div id='mutex-wrapper' style='width: 800px; margin-top: 10px; margin-left: 10px;'>
        <div id="mutex-info-div">
            <p>The query contains <span id='num_of_mutex' class='stat_num'></span> gene pair<span id='plu_s_mutex'></span> with mutually exclusive alterations<span id='stat_sig_mutex'> (<span id='num_of_sig_mutex' class='stat_num'></span> significant)</span>,
            and <span id='num_of_co_oc' class='stat_num'></span> gene pair<span id='plu_s_co_oc'></span> with co-occurrent alterations<span id='stat_sig_co_oc'> (<span id='num_of_sig_co_oc' class='stat_num'></span> significant)</span>.
        </div>
        <div id='mutex-loading-image'>
            <img style='padding:200px;' src='images/ajax-loader.gif' alt='loading'>
        </div>
        <div id="mutex-table-div" style='margin-top:10px;'></div>
    </div>
</div>

<script>
    $(document).ready( function() {
    	//whether this tab has already been initialized or not:
    	var tab_init = false;
    	//function that will listen to tab changes and init this one when applicable:
    	function tabsUpdate() {
    		if ($("#mutex").is(":visible")) {
	    		if (tab_init === false) {
	    			//calling asynch to ensure loading gif is shown:
	    		    window.setTimeout(MutexData.init, 0); 
                            window.setTimeout(MutexView.resize, 0);
		            tab_init = true;
		        } else {
	                MutexView.resize();
	            }
	    	}
    	}
        //this is for the scenario where the tab is open by default (as part of URL >> #tab_name at the end of URL),
        tabsUpdate();
        
        //this is for the scenario where the user navigates to this tab:
        $("#tabs").bind("tabsactivate", function(event, ui) {
        	tabsUpdate();
        });
    });    
    
</script>

<style type="text/css">
    #mutex-info-div {
        font-size: 10px;
        margin-top: 10px;
    }
    #mutex table.dataTable thead th div.DataTables_sort_wrapper {
        font-size: 150%;
        position: relative;
        padding-right: 20px;
    }
    #mutex table.dataTable thead th div.DataTables_sort_wrapper span {
        position: absolute;
        top: 50%;
        margin-top: -8px;
        right: 0;
    }
    #mutex .classMutexTable { 
        text-align: right; 
    }
    #mutex .stat_num {
        font-weight:bold;
    }
    #mutex .label-mutex-significant {
        background-color: #58ACFA;
    }
</style>