function drawMutationDiagram(data)
{
    // TODO init dataTable...
    $("#mutation_table_" + data.hugoGeneSymbol).append(data);
}