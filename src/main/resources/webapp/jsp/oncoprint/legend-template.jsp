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

<script type="text/template" id="glyph_template">
    <svg height="23" width="6">
        <rect fill="{{bg_color}}" width="5.5" height="23"></rect>

        <rect display="{{display_mutation}}" fill="#008000" y="7.666666666666667" width="5.5" height="7.666666666666667"></rect>
        <path display="{{display_fusion}}" d="M0,0L0,23 5.5,11.5Z"></path>

        <path display="{{display_down_rppa}}" d="M0,2.672958956142353L3.0864671457232173,-2.672958956142353 -3.0864671457232173,-2.672958956142353Z" transform="translate(2.75,20.909090909090907)"></path>
        <path display="{{display_up_rppa}}" d="M0,-2.672958956142353L3.0864671457232173,2.672958956142353 -3.0864671457232173,2.672958956142353Z" transform="translate(2.75,2.3000000000000003)"></path>

        <rect display="{{display_down_mrna}}" height="23" width="5.5" stroke-width="2" stroke-opacity="1" stroke="#6699CC" fill="none"></rect>
        <rect display="{{display_up_mrna}}" height="23" width="5.5" stroke-width="2" stroke-opacity="1" stroke="#FF9999" fill="none"></rect>
    </svg>
    <span style="position: relative; bottom: 6px;">{{text}}</span>
</script>
