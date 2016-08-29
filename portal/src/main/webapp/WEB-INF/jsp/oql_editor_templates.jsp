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


<!-- Recursively generate OQL menu items -->
<script id="oql-menu-template" type="text/handlebars-template">
    {{#each items}}
    <li data-val="{{val}}">{{name}}<desc>{{desc}}</desc>
        {{#if items}}
        <ul>
            {{> oql-menu-template}}
        </ul>
        {{/if}}
    </li>
    {{/each}}
</script>

<!-- OQL Menu base -->
<script id="oql-placeholder-template" type="text/handlebars-template">
    <ul class="hidden-menu" id="oql-menu">
        {{> oql-menu-template}}
        <li class="oql-menu-close">Press &lt;ESC&gt; to close</li>
    </ul>
</script>

<!-- Gene symbol replace div base: List -->
<script id="hb_replace_list" type="text/handlebars-template">
	<div class="replace-div" data-index="{{index}}">
		<div class="replace-div-bottom">
		{{#each replacement}}
		{{#if @first}}
			<div><span style="color: {{../color}}; text-decoration: line-through;">{{../symbol}}</span><i class="fa fa-long-arrow-right" aria-hidden="true" style="margin: 0 1rem;"></i><span class="replace-div-link" data-index="{{../index}}" val="{{this}}">{{this}}</span></div>
		{{else}}
			<div><span style="opacity: 0">{{../symbol}}</span><i class="fa fa-long-arrow-right" aria-hidden="true" style="margin: 0 1rem;"></i><span class="replace-div-link" data-index="{{../index}}" val="{{this}}">{{this}}</span></div>
		{{/if}}
		{{/each}}
			<div><span style="opacity: 0">{{symbol}}</span><i class="fa fa-long-arrow-right" aria-hidden="true" style="margin: 0 1rem;"></i><span class="replace-div-remove" data-index="{{index}}" val="">remove</span></div>
		</div>
	</div>
</script>

<!-- Gene symbol replace div base: Dropdown -->
<script id="hb_replace_dropdown" type="text/handlebars-template">
	<div class="replace-div" data-index="{{index}}">
		<div class="replace-div-bottom">
			<div><span style="color: {{color}}; text-decoration: line-through;">{{symbol}}</span><i class="fa fa-long-arrow-right" aria-hidden="true" style="margin: 0 1rem;"></i>
				<span><select class="replace-div-select" data-index="{{index}}">
					{{#each replacement}}
					<option>{{this}}</option>
					{{/each}}
				</select></span>
			</div>
			<div><span style="opacity: 0">{{symbol}}</span><i class="fa fa-long-arrow-right" aria-hidden="true" style="margin: 0 1rem;"></i><span class="replace-div-remove" data-index="{{index}}" val="">remove</span></div>
		</div>
	</div>
</script>


<!-- Symbol replace: State Indicator -->
<script id="hb_state" type="text/handlebars-template">
	<li class="{{class}}" data-index="{{index}}">
		<i class="fa {{icon}}" aria-hidden="true"></i>
		<span class="text">{{{text}}}</span>
	</li>
</script>

<!-- Symbol replace: Symbol Span -->
<script id="hb_span" type="text/handlebars-template"><span class="{{class}}" data-index="{{index}}">{{symbol}}</span></script>

