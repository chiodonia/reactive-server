jQuery(function ($) {
    "use strict";
    var ractive = new Ractive({
      el: 'container',
      template: '#template',
      data: { result: '....' }
    });

    var socket = new WebSocket("ws://localhost:7070/modulo/10/5");

    var modulo = $('#modulo').asEventStream("blur").map(function(event) { return $(event.target).val() });
    //.onValue(function(mod) { console.log(mod) });
    var nr = $('#nr').asEventStream("blur").map(function(event) { return $(event.target).val() });
    //.onValue(function(mod) { console.log(mod) });

    var co = nr.combine(modulo.toProperty(), function(mod, nr) {console.log(mod + " " + nr)});
    co.onValue(function() { console.log("calll") });

    var w = $('#modulo').asEventStream("blur").awaiting($('#nr').asEventStream("blur")).assign($("#my-button"), "attr", "disabled");

    var x = $('#modulo').asEventStream("click").onValue(function(event) { socket.close() });

    var observable = Bacon.fromEventTarget(socket, "message")
        .filter(function(data) {
            return true;
        })
        .map(function(event) {
            return event.data * 2;
        })
        .doAction(function(data) {
             console.log("WEBSOCKET MESSAGE: " + data);
         });

    observable.onValue(function(data) {
         ractive.set('result', data);
    });

    observable.onError(function(data) {
         ractive.set('result', "Error!");
    });

    observable.onEnd(function(data) {
         ractive.set('result', "Done!");
    });

});