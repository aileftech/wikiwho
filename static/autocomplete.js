function autocompleteSource(response) {
	$.getJSON(
		"/autocomplete",
		{ query : $('#autocomplete').val() },
		response
	);
}

var render = function( ul, item ) {
	return $( "<li>" )
		.append( "<div>" + item["name"] + "<span style='float: right; font-weight: bold;'>(" + item["edit_count"] + " edits)</span></div>" )
		.appendTo( ul );
};

$(function() {
	if ($("#autocomplete").length > 0) {
		$("#autocomplete").autocomplete({
	      source: function(request, response) {
	      	autocompleteSource(response);
	      },
	      minLength: 2,
	      select: function( event, ui ) {
	      	window.location.href = "/organization/" + ui.item.id;
	      }
	    })
		.autocomplete("instance")._renderItem = render;
	}
});