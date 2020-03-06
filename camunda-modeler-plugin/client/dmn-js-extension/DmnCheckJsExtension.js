function DmnCheckJsExtension(eventBus, drd, elementRegistry, moddle, overlays, canvas) {

    const messages = document.createElement("div");
    messages.classList.add('message-box');
    messages.id = "message-box";
    messages.style.display = "none";
    canvas.getContainer().parentNode.appendChild(messages);

    eventBus.on('import.done', function() {
        validate();
    });


    eventBus.on('elements.changed', function() {
        validate();
    });

    function validate() {
        const map = {};

        moddle.toXML(drd._definitions, {}, (err, xml) => {
            log("Start validation.");

            fetch('http://localhost:42000/validate', {
                method: "POST",
                body: xml
            }).then(res => {
                res.text().then(function (results) {

                    log("Request complete! response:", results);

                    messages.textContent = "";

                    JSON.parse(results).items.forEach(result => {
                        const shape = elementRegistry.get(result.drgElementId);

                        if (typeof shape !== "undefined") {
                            overlays.add(shape, 'badge', {
                                position: {
                                    bottom: 0,
                                    left: 21 * map[result.drgElementId]
                                },
                                html: '<div title="' + result.message + '" class="badge badge-' + result.severity.toLowerCase() + '"></div>'
                            });

                            map[result.drgElementId] = ~~map[result.drgElementId] + 1;
                        } else {
                            messages.textContent = result.message;
                        }
                    });

                    if (messages.textContent !== "") {
                        messages.style.display = "block";
                    }
                })
            });

            log("Validation finished.");
        });
    }
}



function log(...args) {
    console.log('[DmnCheckJsExtension]', ...args);
}

DmnCheckJsExtension.$inject = ['eventBus', 'drd', 'elementRegistry', 'moddle', 'overlays', 'canvas'];

module.exports = DmnCheckJsExtension;

