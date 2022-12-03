$(function() {
	$('body').on('click', '.diff-row', function() {
		window.location.href = "#current-diff";
		
		$('.diff-row').each(function(i, v) {
			$(v).removeClass('diff-row-active');
		});
		$(this).addClass('diff-row-active');

		$('.diff-content').each(function(i, v) {
			$(v).hide();
		});
		$('#' + $(this).data('id') + "-html").show();

		$('.diff-placeholder').hide();
	});

	$('body').on('click', '.vote-diff', function() {
		var diffId = $(this).data('id');
		var e = $(this);

		$.post("/vote/" + diffId, function(data) {
			if (data["success"]) {
				e.text("Saved.");
			} else {
				e.text("Duplicate vote.");
			}
		});
	});
});