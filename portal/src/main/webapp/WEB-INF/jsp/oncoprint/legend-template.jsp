<script type="text/template" id="glyph_template">
    <svg height="23" width="6">
        <rect fill="{{bg_color}}" width="5.5" height="23"></rect>

        <rect display="{{display_mutation}}" fill="#008000" y="7.666666666666667" width="5.5" height="7.666666666666667"></rect>
        <rect display="{{display_fusion}}" fill="black" y="7.666666666666667" width="5.5" height="7.666666666666667"></rect>

        <path display="{{display_down_rppa}}" d="M0,2.182461848650375L2.5200898716287647,-2.182461848650375 -2.5200898716287647,-2.182461848650375Z" transform="translate(2.75,2.3000000000000003)"></path>
        <path display="{{display_up_rppa}}" d="M0,-2.182461848650375L2.5200898716287647,2.182461848650375 -2.5200898716287647,2.182461848650375Z" transform="translate(2.75,20.909090909090907)" aria-describedby="ui-tooltip-838"></path>

        <rect display="{{display_down_mrna}}" height="23" width="5.5" stroke-width="2" stroke-opacity="1" stroke="#6699CC" fill="none" aria-describedby="ui-tooltip-732"></rect>
        <rect display="{{display_up_mrna}}" height="23" width="5.5" stroke-width="2" stroke-opacity="1" stroke="#FF9999" fill="none" aria-describedby="ui-tooltip-576"></rect>
    </svg>
    <span style="position: relative; bottom: 6px;">{{text}}</span>
</script>
