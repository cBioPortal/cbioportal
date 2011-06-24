<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>

<%
    String networkSif = (String) request.getAttribute(QueryBuilder.NETWORK_SIF);
    networkSif = networkSif.replaceAll("\n","~n~");
    /*out.println ("<PRE>");
    out.println (networkSif);
    out.println ("</PRE>"); */
%>

<script type="text/javascript" src="js/cytoscape_web/json2.min.js"></script>
<script type="text/javascript" src="js/cytoscape_web/AC_OETags.min.js"></script>
<script type="text/javascript" src="js/cytoscape_web/cytoscapeweb.min.js"></script>

<script type="text/javascript">
            window.onload = function() {
                var div_id = "cytoscapeweb";
                var sif = '<%=networkSif%>';
                sif = sif.replace(new RegExp("~n~", 'g'), "\n");

                var visual_style = {
                    global: {
                        backgroundColor: "#eeeeee"
                    },
                    nodes: {
                        shape: "ELLIPSE",
                        borderWidth: 1,
                        borderColor: "#000000",
                        size: {
                            defaultValue: 25,
                            continuousMapper: { attrName: "weight", minValue: 25, maxValue: 75 }
                        },
                        color: {
                            discreteMapper: {
                                attrName: "id",
                                entries: [
                                    { attrValue: 1, value: "#0B94B1" },
                                    { attrValue: 2, value: "#9A0B0B" },
                                    { attrValue: 3, value: "#dddd00" }
                                ]
                            }
                        },
                        labelHorizontalAnchor: "center"
                    },
                    edges: {
                        width: 1,
                        color: "#0B94B1"
                    }
                };

                // initialization options
                var options = {
                    swfPath: "swf/CytoscapeWeb",
                    flashInstallerPath: "swf/playerProductInstall"
                };

                var vis = new org.cytoscapeweb.Visualization(div_id, options);

                vis.ready(function() {
                    // set the style programmatically
                    document.getElementById("color").onclick = function(){
                        vis.visualStyle(visual_style);
                    };
                });

                var draw_options = {
                    // your data goes here
                    network: sif,
                    edgeLabelsVisible: false,
                    layout: "ForceDirected",
                    visualStyle: visual_style,
                    panZoomControlVisible: true
                };

                vis.draw(draw_options);
            };
        </script>

        



<div class="section" id="network">
        <div id="cytoscapeweb">
            Cytoscape Web will replace the contents of this div with your graph.
        </div>
</div>