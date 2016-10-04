$(document).ready(function() {

    $('#pintoInput').blur(updateData);
    $('#pintoInput').submit(updateData);
    $('#pintoInput').keypress(function (e) {
  		if (e.which == 13) {
  			updateData();
    		return false;    //<---- Add this line
  		}
	});

});

function updateData() {
   	$.ajax({
			type: "GET",
			url: "/pinto",
			data: { statement: $('#pintoInput').val(),
						numbers_as_string : true },
			dataType: "json"
		}).done(function (response) {
			if(response.responseType == "data") {
				outputResponseTable(response);
			} else if(response.responseType == "error") {
				outputError(response);
			} else if(response.responseType == "messages") {
				outputMessages(response);
			}
		});

}

function outputResponseTable(response) {
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
	$('#pintoResponse').html(table);


}

function outputMessages(response) {
	var messages = "";
	for(var i = 0; i < response.messages.length; i++) {
		messages += response.messages[i];
	}
	$('#pintoResponse').html(messages);
}

function outputError(response) {
	var error = "<h2>Pinto error</h2>";
	if("cause" in response.exception) {
		error += response.exception.cause.detailMessage;
	} else {
		error += response.exception.detailMessage;
	}
	$('#pintoResponse').html(error);
}
