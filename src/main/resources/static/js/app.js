// ================================
// 👥 LOAD USERS INTO SLIDER
// ================================
$.ajax({
	url: "/api/users",
	success: function (response) {
		$.each(response, function (i, item) {

			var userCard = `
				<div class="user-slide-card">
					<div class="user-avatar">
						${item.firstName ? item.firstName.charAt(0).toUpperCase() : "U"}
					</div>

					<h4>${item.firstName} ${item.lastName}</h4>

					<p><strong>Email:</strong></p>
					<p>${item.email}</p>

					<small>UserID:</small>
					<small>${item.id}</small>
				</div>
			`;

			$("#usersSlider").append(userCard);
		});
	}
});

// ================================
// ⬅️➡️ USERS SLIDER CONTROLS
// ================================
$("#slideRight").click(function () {
	$("#usersSlider").animate({
		scrollLeft: "+=350"
	}, 400);
});

$("#slideLeft").click(function () {
	$("#usersSlider").animate({
		scrollLeft: "-=350"
	}, 400);
});
