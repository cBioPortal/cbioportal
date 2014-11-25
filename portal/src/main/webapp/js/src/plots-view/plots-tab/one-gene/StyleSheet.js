var mutationStyle = {  //Key and "typeName" are always identical
            frameshift : {
                typeName : "frameshift",
                symbol : "triangle-down",
                fill : "#1C1C1C",
                stroke : "#B40404",
                legendText : "Frameshift"
            },
            nonsense : {
                typeName: "nonsense",
                symbol : "diamond",
                fill : "#1C1C1C",
                stroke : "#B40404",
                legendText : "Nonsense"
            },
            splice : {
                typeName : "splice",
                symbol : "triangle-up",
                fill : "#A4A4A4",
                stroke : "#B40404",
                legendText : "Splice"
            },
            in_frame : {
                typeName : "in_frame",
                symbol : "square",
                fill : "#DF7401",
                stroke : "#B40404",
                legendText : "In_frame"
            },
            nonstart : {
                typeName : "nonstart",
                symbol : "cross",
                fill : "#DF7401",
                stroke : "#B40404",
                legendText : "Nonstart"
            },
            nonstop : {
                typeName : "nonstop",
                symbol : "triangle-up",
                fill : "#1C1C1C",
                stroke : "#B40404",
                legendText : "Nonstop"
            },
            missense : {
                typeName : "missense",
                symbol : "circle",
                fill : "#DF7401",
                stroke : "#B40404",
                legendText : "Missense"
            },
            other: {
                typeName: "other",
                symbol: "square",
                fill : "#1C1C1C",
                stroke : "#B40404",
                legendText : "Other"
            },
            non : {
                typeName : "non",
                symbol : "circle",
                fill : "#00AAF8",
                stroke : "#0089C6",
                legendText : "No mutation"
            }
        },
        gisticStyle = {
            Amp : {
                stroke : "#FF0000",
                fill : "none",
                symbol : "circle",
                legendText : "Amp"
            },
            Gain : {
                stroke : "#FF69B4",
                fill : "none",
                symbol : "circle",
                legendText : "Gain"
            },
            Diploid : {
                stroke : "#000000",
                fill : "none",
                symbol : "circle",
                legendText : "Diploid"
            },
            Hetloss : {
                stroke : "#00BFFF",
                fill : "none",
                symbol : "circle",
                legendText : "Hetloss"
            },
            Homdel : {
                stroke : "#00008B",
                fill : "none",
                symbol : "circle",
                legendText : "Homdel"
            },
            Unknown : {
                stroke : "#A8A8A8",
                fill : "none",
                symbol : "circle",
                legendText : "No CNA data"
            }
        }