function autocompleteSourcePage(response) {
	$.getJSON(
		"/autocomplete",
		{ query : $('#autocomplete-page').val(), type: "page" },
		response
	);
}

$(function() {
	if ($("#autocomplete-page").length > 0) {
		$("#autocomplete-page").autocomplete({
	      source: function(request, response) {
	      	autocompleteSourcePage(response);
	      },
	      minLength: 2,
	      select: function( event, ui ) {
	      	window.location.href = "/page/" + ui.item.id;
	      }
	    })
		.autocomplete("instance")._renderItem = render;
	}
});