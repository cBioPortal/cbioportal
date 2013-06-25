var PlotsCustomMenu = (function(){
    function generateList(selectId, options) {
        var select = document.getElementById(selectId);
        options.forEach(function(option){
            var el = document.createElement("option");
            el.textContent = option;
            el.value = option;
            select.appendChild(el);
        });
    }
    return {
        init: function() {
            generateList("custom_gene1", gene_list);
            generateList("custom_gene2", gene_list);
        },
    };
}());
