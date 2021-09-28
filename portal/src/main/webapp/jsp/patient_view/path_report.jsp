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


<object data="<%=pathReportUrl%>" type="application/pdf" width="100%" height="600px">
 
  <p>It appears you don't have a PDF plugin for this browser.</p>
  <p id="missing-pdf-plugin">You can <a href="<%=pathReportUrl%>">click here to
  download the pathology report PDF file</a>.</p>
  
</object>

<script type="text/javascript">
$(document).ready(function(){
    if (cbio.util.browser.mozilla) { // firefox
        var msg = "You can <ul><li>either install a PDF plugin such as <a href='https://addons.mozilla.org/en-US/firefox/addon/pdfjs/'>this one</a> and refresh this page,</li>\n\
                   <li>or <a href='<%=pathReportUrl%>'>click here to download the pathology report PDF file</a>.</li></ul>";
        $("#missing-pdf-plugin").html(msg);
    }
});



</script>
