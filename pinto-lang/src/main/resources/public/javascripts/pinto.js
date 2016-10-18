var cellCount = 0;

$(document).ready(function() {

});

function addCell(showLabel) {
	var cellNumber = cellCount++;
	var inputName = "pinto_input_" + cellNumber;
	var radioName = "pinto_radio_" + cellNumber;
	var responseName = "pinto_response_" + cellNumber;
	var legendName = "pinto_legend_" + cellNumber;
	var cellHTML = "<div class=\"pintoCell\"><form>\n";
	if(showLabel) {
		cellHTML += "<label for=\"" + inputName + "\">Expression #" + cellNumber +" </label><br/>\n"
	}
	cellHTML += "<textarea id=\"" + inputName + "\" cols=\"75\" rows=\"5\"></textarea><br/>\n"
	cellHTML += "<input type=\"radio\" name=\"" + radioName + "\" value=\"table\" checked=\"checked\">Table&nbsp;&nbsp;";
	cellHTML += "<input type=\"radio\" name=\"" + radioName + "\" value=\"graph\">Graph";
	cellHTML += "</form>\n<br/>\n<div id=\"" + responseName + "\"></div>";
	cellHTML += "\n<div id=\"" + legendName + "\"></div>\n</div>\n";
	$("#pintoWorksheet").append(cellHTML);
	inputName = "#" + inputName;
    $(inputName).blur(updateData(cellNumber));
    $(inputName).submit(updateData(cellNumber));
    $(inputName).keypress(function (e) {
  		if (e.which == 13) {
  			updateData(cellNumber);
    		return false;
  		}
	});
	$('input[name=' + radioName + ']').change(function() {
		updateData(cellNumber);
    });

}

function updateData(cellNumber) {
   	$.ajax({
			type: "GET",
			url: "/pinto",
			data: { statement: "reset " + $('#pinto_input_' + cellNumber).val(),
						numbers_as_string : true },
			dataType: "json"
		}).done(function (response) {
			if(response.responseType == "data") {
				var type = $('input[name=pinto_radio_'+ cellNumber + ']:checked').val();	
				if(type == "table") {
					outputResponseTable(cellNumber, response);
				} else {
					outputResponseGraph(cellNumber, response);
				}
			} else if(response.responseType == "error") {
				outputError(cellNumber, response);
			} else if(response.responseType == "messages") {
				outputMessages(cellNumber, response);
			}
		});

}

function outputResponseGraph(cellNumber, response) {
	$('#pinto_response_' + cellNumber).html("");
	var data = new Array();
	var min = Number.MAX_VALUE;
	var max = -1 * Number.MAX_VALUE;
	var n = 0;
	var sumOfSquares = 0;
	var sum = 0;
	var mean = 0;
	for(var i = 0; i < response.columns.length; i++) {
		for(var j = 0; j < response.index.length; j++) {
			if(response.data[i][j] != "NaN") {
				var numberValue = parseFloat(response.data[i][j]);
				data.push({date: response.index[j], value: numberValue, label: response.columns[i] });
				n++;
				max = numberValue > max ? numberValue : max;
				min = numberValue < min ? numberValue : min;
				sum += numberValue;
				sumOfSquares += numberValue * numberValue;
				mean += (numberValue - mean) / n;
			}
		}
	}
	
	var std = Math.sqrt((sumOfSquares - sum * mean) / n);
	
	var numberFormat = 'g';
	if(max < 5 && min > -5) {
		numberFormat = 'p';
	}
	
	var vlSpec = {
		"config": {
			"numberFormat": numberFormat,
			"cell": {"width": 300, "height": 200}
		},
		"description": "Pinto graph",
 		"data": {"values": data},
  		"mark": "line",
  		"encoding": {
    		"x": { "field": "date", "type": "temporal"},
    		"y": {
    			"field": "value", 
    			"type": "quantitative",
    			"scale": {
    				"type": "linear",
    				"domain": [min - std / 2, max + std / 2]
    			}
    		},
    		"color": {"field": "label", "type": "nominal"}
  		}
	}
	
	var embedSpec = {
    	mode: "vega-lite",  // Instruct Vega-Embed to use the Vega-Lite compiler
    	spec: vlSpec
  	};

  // Embed the visualization in the container with id `vis`
  vg.embed('#pinto_response_' + cellNumber, embedSpec, function(error, result) {
    // Callback receiving the View instance and parsed Vega spec
    // result.view is the View, which resides under the '#vis' element
  });
	
}

function outputResponseTable(cellNumber, response) {
	var table = "<table class=\"pintoDataTable\">";
	var colCount = response.columns.length;
	table += "<colgroup>";
	for(var i = 0; i < colCount + 1; i++) {
		table += "<col>";
	}
	table += "</colgroup>";
	table += "<thead><tr>";
	table += "<th>Date</th>";
	for(var i = 0; i < colCount; i++) {
		table += "<th>" + response.columns[i] + "</th>";
	}
	table += "</tr></thead>"
	
	table += "<tbody>";
	for(var i = 0; i < response.index.length; i++) {
		table += "<tr><td>" + response.index[i] + "</td>";
		for(var j = 0; j < response.data.length; j++) {
			table += "<td>" + response.data[j][i] + "</td>";
		}
		table += "</tr>";
	}
	table += "</tbody></table>";
	$('#pinto_response_' + cellNumber).html(table);


}

function outputMessages(cellNumber, response) {
	var messages = "";
	for(var i = 0; i < response.messages.length; i++) {
		messages += response.messages[i];
	}
	$('#pinto_response_' + cellNumber).html(messages);
}

function outputError(cellNumber, response) {
	var error = "<h2>Pinto error</h2>";
	if("cause" in response.exception) {
		error += response.exception.cause.detailMessage;
	} else {
		error += response.exception.detailMessage;
	}
	$('#pinto_response_' + cellNumber).html(error);
}
