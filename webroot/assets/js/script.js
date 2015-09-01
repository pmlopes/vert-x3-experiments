$(function () {

    // This demo depends on the canvas element
    if (!('getContext' in document.createElement('canvas'))) {
        alert('Sorry, it looks like your browser does not support canvas!');
        return false;
    }

    var doc = $(document),
        canvas = $('#paper'),
        ctx = canvas[0].getContext('2d');

    // Generate an unique ID
    var id = Math.round($.now() * Math.random());

    // A flag for drawing activity
    var drawing = false;

    var clients = {};

    var eb = new vertx.EventBus(window.location.protocol + '//' + window.location.hostname + ':' + window.location.port + '/eventbus');

    eb.onopen = function () {

        eb.registerHandler('draw', function (data) {
            // Is the user drawing?
            if (data.drawing && clients[data.id]) {

                // Draw a line on the canvas. clients[data.id] holds
                // the previous position of this user's mouse pointer

                drawLine(clients[data.id].x, clients[data.id].y, data.x, data.y);
            }

            // Saving the current client state
            clients[data.id] = data;
            clients[data.id].updated = $.now();
        });
    };

    var prev = {};

    canvas.on('mousedown', function (e) {
        e.preventDefault();
        drawing = true;
        prev.x = e.pageX;
        prev.y = e.pageY;
    });

    doc.bind('mouseup mouseleave', function () {
        drawing = false;
    });

    var lastEmit = $.now();

    doc.on('mousemove', function (e) {
        if ($.now() - lastEmit > 30) {
            eb.publish('draw', {
                'x': e.pageX,
                'y': e.pageY,
                'drawing': drawing,
                'id': id
            });
            lastEmit = $.now();
        }

        // Draw a line for the current user's movement, as it is
        // not received in the eventbus

        if (drawing) {

            drawLine(prev.x, prev.y, e.pageX, e.pageY);

            prev.x = e.pageX;
            prev.y = e.pageY;
        }
    });

    // Remove inactive clients after 10 seconds of inactivity
    setInterval(function () {

        for (var ident in clients) {
            if (clients.hasOwnProperty(ident)) {
                if ($.now() - clients[ident].updated > 10000) {
                    // Last update was more than 10 seconds ago.
                    // This user has probably closed the page
                    delete clients[ident];
                }
            }
        }

    }, 10000);

    function drawLine(fromx, fromy, tox, toy) {
        ctx.moveTo(fromx, fromy);
        ctx.lineTo(tox, toy);
        ctx.stroke();
    }

});