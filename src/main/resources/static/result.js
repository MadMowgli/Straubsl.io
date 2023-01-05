$(document).ready(function () {

    // Style table rows since Thymeleaf won't load the CSS properly
    const rows = document.getElementsByTagName('tr');
    for(let i = 0; i < rows.length; i++) {
        rows[i].style.height = "5rem";
    }

    const data = document.getElementsByTagName('td');
    for(let i = 0; i < data.length; i++) {
        data[i].style.height = "5rem";
    }


})


